@file:Suppress("PackageName")

package com.example.epower.Services

import android.annotation.SuppressLint
import android.app.*
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.epower.R
import com.example.epower.UserData.ListSQL
import com.example.epower.UserData.RequestsSQL
import com.example.epower.UserData.RequestsSQL.Companion.MODE_READ_SINGLE
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import android.app.PendingIntent
import android.os.Handler
import com.example.epower.StartApp.AdminPermissionReceiver

@SuppressLint("MissingPermission", "NewApi")
@Suppress("DEPRECATION", "UNUSED_VARIABLE", "NAME_SHADOWING")
class ControllerSensors : Service()
{
    private var timeWorkWifi: Int = 0
    private var timeWorkMNetwork: Int = 0
    private var timeWorkGPS: Int = 0
    private var timeWorkBluetooth: Int = 0
    private var timeWorkDisplay: Int = 0

    private var FINISH_TIME_BLUETOOTH = DEFAULT_FINISH_TIME
    private var FINISH_TIME_GPS = DEFAULT_FINISH_TIME
    private var FINISH_TIME_MNETWORK = DEFAULT_FINISH_TIME
    private var FINISH_TIME_WIFI = DEFAULT_FINISH_TIME
    private var FINISH_TIME_DISPLAY = DEFAULT_FINISH_TIME

    private lateinit var enabledAppInfo: HashMap<String, Int>
    private lateinit var screenLock: DevicePolicyManager
    private lateinit var requestsSQL: RequestsSQL
    private lateinit var listSQL: ArrayList<ListSQL>


    override fun onBind(intent: Intent) = TODO("Return the communication channel to the service.")

    override fun onCreate()
    {
        super.onCreate()
        getSensorsInfo()
    }

    private fun getSensorsInfo()
    {
        initialInitialization()

        val locationListener: LocationListener = object : LocationListener
        {
            override fun onLocationChanged(location: Location) { if (location.speed > 4f) setZeroTimeGPS() }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) { }
            override fun onProviderDisabled(provider: String) { setZeroTimeGPS() }
        }

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var prevDataDownload: Long = 0
        var setPrevDataDownload = true

        var connectWIFI: Boolean
        var connectMNetwork: Boolean
        var trafficNetwork: Double

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
       {
           val connectivityManager: ConnectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
           val bluetooth = BluetoothAdapter.getDefaultAdapter()

           connectWIFI = activeNetwork(connectivityManager, ConnectivityManager.TYPE_WIFI)
           connectMNetwork = activeNetwork(connectivityManager, ConnectivityManager.TYPE_MOBILE)

           if (connectMNetwork || connectWIFI)
           {
               if (setPrevDataDownload)
               {
                   prevDataDownload = TrafficStats.getTotalRxBytes()

                   this.timeWorkWifi += if (connectWIFI) TIME_UPDATE else 0
                   this.timeWorkMNetwork += if (connectMNetwork) TIME_UPDATE else 0
                   setPrevDataDownload = false
               }
               else
               {
                   trafficNetwork = ((TrafficStats.getTotalRxBytes() - prevDataDownload) / 1024).toDouble()

                   timeWorkWifi += when
                   {
                      (trafficNetwork < 50) -> TIME_UPDATE
                      timeWorkWifi > 0 -> -(TIME_UPDATE + 10)
                      else -> -timeWorkWifi
                   }

                   timeWorkMNetwork += when
                   {
                       (trafficNetwork < 50) -> TIME_UPDATE
                       timeWorkMNetwork > 0 -> -(TIME_UPDATE + 10)
                       else -> -timeWorkMNetwork
                   }

                   setPrevDataDownload = true
               }
           }
           else
           {
               if (this.timeWorkWifi != 0) requestsSQL.writeToDB(CHANNEL_WIFI, timeWorkWifi)
               if (this.timeWorkMNetwork != 0) requestsSQL.writeToDB(CHANNEL_WIFI, timeWorkMNetwork)

               timeWorkWifi = 0
               timeWorkMNetwork = 0

               setTimeFinishAllSensors()
           }

           if(bluetooth != null && bluetooth.isEnabled)
           {
               if (bluetooth.getProfileConnectionState(BluetoothHeadset.HEADSET) != BluetoothHeadset.STATE_CONNECTED)
                   timeWorkBluetooth += TIME_UPDATE
           }
           else
           {
               if (this.timeWorkBluetooth != 0) requestsSQL.writeToDB(CHANNEL_BLUETOOTH, timeWorkBluetooth)
               timeWorkBluetooth = 0

               setTimeFinishAllSensors()
           }

           screenActivity()
           analysisSensors(connectivityManager)
       }, 0, TIME_UPDATE.toLong(), TimeUnit.SECONDS)
    }

    @SuppressLint("SetTextI18n")
    private fun analysisSensors(connectivityManager: ConnectivityManager)
    {
        Log.i("Sensors info seconds", "|| WIFI Time: $timeWorkWifi, MNetwork Time: $timeWorkMNetwork, GPS Time: $timeWorkGPS, Bluetooth Time: $timeWorkBluetooth ||")

        if (this.timeWorkGPS >= FINISH_TIME_GPS)
        {
            notificationAction(
                "Обнаружена неиспользуемая функция 'GPS'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию."
                , "Отключить"
                , CHANNEL_GPS
                , NOTIFY_GPS)

            this.timeWorkGPS = 0
        }

        if (this.timeWorkWifi >= FINISH_TIME_WIFI)
        {
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.isConnected)
            notificationAction(
                "Обнаружена неиспользуемая функция 'WIFI'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию."
                , CHANNEL_WIFI
                , "Отключить"
                , NOTIFY_WIFI)

            this.timeWorkWifi = 0
        }

        if (this.timeWorkMNetwork >= FINISH_TIME_MNETWORK)
        {
            notificationAction(
                "Обнаружена неиспользуемая функция 'Мобильный интернет'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию."
                , CHANNEL_MNETWORK
                , "Отключить"
                , NOTIFY_MNETWORK)

            this.timeWorkMNetwork = 0
        }
        if (this.timeWorkBluetooth >= FINISH_TIME_BLUETOOTH)
        {
            try
            {
                if (!BluetoothAdapter.getDefaultAdapter().disable())
                    notificationAction(
                        "Обнаружена неиспользуемая функция 'Bluetooth'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию.",
                          CHANNEL_BLUETOOTH
                        , "Отключить"
                        , NOTIFY_BLUETOOTH
                    )
            }
            catch (deviceOff: Exception) { }
            finally { this.timeWorkBluetooth = 0 }
        }
    }

    fun setZeroTimeGPS()
    {
        this.requestsSQL.writeToDB(CHANNEL_GPS, timeWorkGPS)
        setTimeFinishAllSensors()

        this.timeWorkGPS = 0
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun notificationAction(infoText: String, channelID: String, btnName: String, notificationID: Int)
    {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val listenerIntent: Intent? = when (channelID)
        {
            CHANNEL_GPS -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            CHANNEL_WIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
            CHANNEL_MNETWORK -> Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            CHANNEL_BLUETOOTH -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

            else -> null
        }

        val intentCancer = Intent(this, NotificationCancer::class.java)
        intentCancer.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intentCancer.putExtra(channelID, notificationID)

        val vibrate = longArrayOf(1000, 1000, 1000, 1000, 1000)
        val builder: NotificationCompat.Builder

        if (channelID != CHANNEL_DISPLAY)
        {
          builder = NotificationCompat.Builder(this, channelID)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setVibrate(vibrate)
            .setTimeoutAfter(600000L)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Оповещение системы контроля EPower")
            .setStyle(NotificationCompat.BigTextStyle().bigText(infoText))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, btnName, PendingIntent.getActivity(this, 100, listenerIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(R.drawable.ic_launcher_foreground, "Всё впорядке", PendingIntent.getActivity(this, 0, intentCancer, PendingIntent.FLAG_CANCEL_CURRENT))
            .setPriority(NotificationCompat.PRIORITY_MAX)
        }

        else
        {
            builder = NotificationCompat.Builder(this, channelID)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setVibrate(vibrate)
                .setTimeoutAfter(22000)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Оповещение системы контроля EPower")
                .setStyle(NotificationCompat.BigTextStyle().bigText(infoText))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_launcher_foreground, btnName, PendingIntent.getActivity(this, 0, intentCancer, PendingIntent.FLAG_CANCEL_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)

            Handler().postDelayed(
            {
                val allNotifyId: ArrayList<Int>
                val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                allNotifyId = ArrayList()
                for (i in nManager.activeNotifications.indices) allNotifyId.add(nManager.activeNotifications[i].id)

                if (allNotifyId.contains(NOTIFY_DISPLAY)) actionDisplayOff(true)
                allNotifyId.clear()
            }, 20000)
        }

        createChannelIfNeeded(notificationManager, channelID)
        notificationManager.notify(notificationID, builder.build())
    }

    private fun createChannelIfNeeded(manager: NotificationManager, channelID: String)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            manager.createNotificationChannel(
                NotificationChannel(channelID
                , channelID
                , NotificationManager.IMPORTANCE_DEFAULT))
    }

    @SuppressLint("NewApi", "QueryPermissionsNeeded", "WrongConstant", "InvalidWakeLockTag")
    private fun screenActivity()
    {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val usageStatsManager = this.getSystemService(getString(R.string.Usage_Stats)) as UsageStatsManager

        val isScreenOn = powerManager.isInteractive
        var appName: String

        if (isScreenOn)
        {
            this.timeWorkDisplay += TIME_UPDATE
            val packList = packageManager.getInstalledPackages(0)

            for (i in packList.indices)
            {
                appName = packList[i].applicationInfo.loadLabel(packageManager).toString()

                if (((packList[i].applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
                            && (packList[i].applicationInfo.category == 2)
                            && (appName != "EPower"))
                    || (appName == "YouTube"))
                {
                    if (!usageStatsManager.isAppInactive(packList[i].applicationInfo.packageName))
                    {
                        if (this.enabledAppInfo.contains(appName))
                        this.enabledAppInfo[appName] = (this.enabledAppInfo[appName]!! + 10)

                        else
                        this.enabledAppInfo[appName] = TIME_UPDATE
                    }
                }
            }

            for ((key, value) in this.enabledAppInfo.entries)
            {
                Log.i("Active app info", "|| $key - $value ||")
                if (value >= FINISH_TIME_DISPLAY)
                {
                    notificationAction(
                        "Вы сейчас пользуетесь устройством? В случае игнорироввание произойдёт блокировка экрана воизбежании расхода аккумулятора."
                        , CHANNEL_DISPLAY
                        , "Подтвердить"
                        , NOTIFY_DISPLAY)
                    break
                }
            }
        }
        else actionDisplayOff(false)
    }

    private fun actionDisplayOff(displayActive: Boolean)
    {
        this.requestsSQL.writeToDB(CHANNEL_DISPLAY, timeWorkDisplay)
        this.timeWorkDisplay = 0

        for ((key, _) in this.enabledAppInfo.entries) enabledAppInfo[key] = 0

        if (displayActive)
        {
            if (this.screenLock.isAdminActive(ComponentName(this, AdminPermissionReceiver::class.java))) screenLock.lockNow()
            else Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
        }

        setTimeFinishAllSensors()
    }

    private fun analyzeUsageSensor(sensorName: String)
    {
        this.requestsSQL.readFromDB(this.listSQL, MODE_READ_SINGLE, sensorName)

        if (listSQL.size >= 5)
        {
            var averageValue = 0

            for (i in this.listSQL.indices) averageValue += listSQL[i].sensorInfo
            averageValue /= listSQL.size

            when (sensorName)
            {
                CHANNEL_GPS -> this.FINISH_TIME_GPS = averageValue
                CHANNEL_MNETWORK -> this.FINISH_TIME_MNETWORK = averageValue
                CHANNEL_WIFI -> this.FINISH_TIME_WIFI = averageValue
                CHANNEL_BLUETOOTH -> this.FINISH_TIME_BLUETOOTH = averageValue
            }
        }
    }

    private fun setTimeFinishAllSensors()
    {
        val allSensors = arrayListOf(CHANNEL_GPS, CHANNEL_WIFI, CHANNEL_MNETWORK, CHANNEL_BLUETOOTH, CHANNEL_DISPLAY)
        for (i in allSensors.indices) analyzeUsageSensor(allSensors[i])
    }

    @SuppressLint("NewApi")
    private fun activeNetwork(connectivityManager: ConnectivityManager, internetMode: Int): Boolean
    {
        return if (connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null)
        connectivityManager.getNetworkInfo(internetMode)!!.isConnected
        else false
    }

    private fun initialInitialization()
    {
        this.enabledAppInfo = HashMap()
        this.listSQL = ArrayList()

        this.screenLock = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        this.requestsSQL = RequestsSQL(this)

        setTimeFinishAllSensors()
    }

    companion object
    {
        private const val TIME_UPDATE = 10
        private const val DEFAULT_FINISH_TIME = (36 * 100000)

        private const val NOTIFY_GPS = 1
        private const val CHANNEL_GPS = "GPS"

        private const val NOTIFY_WIFI = 2
        private const val CHANNEL_WIFI = "WIFI"

        private const val NOTIFY_MNETWORK = 3
        private const val CHANNEL_MNETWORK = "MNETWORK"

        private const val NOTIFY_BLUETOOTH = 4
        private const val CHANNEL_BLUETOOTH = "BLUETOOTH"

        private const val NOTIFY_DISPLAY = 5
        private const val CHANNEL_DISPLAY = "DISPLAY"
    }
}