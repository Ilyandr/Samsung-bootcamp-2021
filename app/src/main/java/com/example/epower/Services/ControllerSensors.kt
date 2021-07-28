@file:Suppress("PackageName")

package com.example.epower.Services

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.epower.R
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class ControllerSensors : Service()
{
    private var timeWorkNetwork: Int = 0
    private var timeWorkGPS: Int = 0
    private var timeWorkBluetooth: Int = 0

    private val timeUpdateInfo = 10
    private val defaultFinishTime = (18 * 100000)

    private val notificationGPS = 1
    private val channelGPS = "channelGPS"

    private val notificationWIFI = 2
    private val channelWIFI = "channelWIFI"

    private val notificationMNETWORK = 3
    private val channelMNETWORK= "channelMNETWORK"

    private val notificationBluetooth = 4
    private val channelBluetooth = "channelBluetooth "


    override fun onBind(intent: Intent) = TODO("Return the communication channel to the service.")
    override fun onCreate() { super.onCreate(); getSensorsInfo() }

    @SuppressLint("MissingPermission", "NewApi")
    private fun getSensorsInfo()
    {
        val locationListener: LocationListener = object : LocationListener
        {
            override fun onLocationChanged(location: Location) { if (location.speed > 5.5f) setZeroTimeGPS()
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) { }
            override fun onProviderDisabled(provider: String) { setZeroTimeGPS()
            }
        }

        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var prevDataDownload: Long = 0
        var setPrevDataDownload = true

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
       {
           val connectivityManager: ConnectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

           val network = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
           val bluetooth = BluetoothAdapter.getDefaultAdapter()

           if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) this.timeWorkGPS += timeUpdateInfo
           else timeWorkGPS = 0

           if (network != null)
           {
               if (setPrevDataDownload)
               {
                   prevDataDownload = TrafficStats.getTotalRxBytes()
                   this.timeWorkNetwork += timeUpdateInfo
                   setPrevDataDownload = false
               }
               else
               {
                   timeWorkNetwork = when
                   {
                      (((TrafficStats.getTotalRxBytes() - prevDataDownload) / 1024) < 50) -> timeWorkNetwork + timeUpdateInfo
                      (timeWorkNetwork > 0) -> timeWorkNetwork - timeUpdateInfo - 10
                      else -> 0
                   }
                   setPrevDataDownload = true
               }
           }
           else timeWorkNetwork = 0

           if(bluetooth != null && bluetooth.isEnabled)
           {
               if (bluetooth.getProfileConnectionState(BluetoothHeadset.HEADSET) != BluetoothHeadset.STATE_CONNECTED)
                   timeWorkBluetooth += timeUpdateInfo
           }
           else timeWorkBluetooth = 0

           analysisSensors(connectivityManager)
       }, 0, timeUpdateInfo.toLong(), TimeUnit.SECONDS)
    }

    @SuppressLint("SetTextI18n")
    private fun analysisSensors(connectivityManager: ConnectivityManager)
    {
        Log.e("ServiceSensors Info", "Network Time: " + this.timeWorkNetwork + " || GPS Time: " + this.timeWorkGPS + " || Bluetooth Time: " + this.timeWorkBluetooth)

        if (this.timeWorkGPS >= defaultFinishTime)
        {
            notificationAction(
                "Обнаружена неиспользуемая функция 'GPS'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию."
                , this.channelGPS
                , this.notificationGPS)

            this.timeWorkGPS = 0
        }

        if (this.timeWorkNetwork >= defaultFinishTime)
        {
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.isConnected)
            notificationAction(
                "Обнаружена неиспользуемая функция 'WIFI'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию."
                , this.channelWIFI
                , this.notificationWIFI)

            else
            notificationAction(
                "Обнаружена неиспользуемая функция 'Мобильный интернет'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию."
                , this.channelMNETWORK
                , this.notificationMNETWORK)

            this.timeWorkNetwork = 0
        }

        if (this.timeWorkBluetooth >= defaultFinishTime)
        {
            try
            {
                if (!BluetoothAdapter.getDefaultAdapter().disable())
                    notificationAction(
                        "Обнаружена неиспользуемая функция 'Bluetooth'. Во избежании быстрой разрядки аккумулятора - отключите данную функцию.",
                        this.channelBluetooth,
                        this.notificationBluetooth
                    )
            }
            catch (deviceOff: Exception) { }
            finally { this.timeWorkBluetooth = 0 }
        }
    }

    fun setZeroTimeGPS() { this.timeWorkGPS = 0 }

    private fun notificationAction(infoText: String, channelID: String, notificationID: Int)
    {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val listenerIntent: Intent? = when (channelID)
        {
            this.channelGPS -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            this.channelWIFI -> Intent(Settings.ACTION_WIFI_SETTINGS)
            this.channelMNETWORK -> Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            this.channelBluetooth -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            else -> null
        }

        val vibrate = longArrayOf(1000, 1000, 1000, 1000, 1000)

        val builder = NotificationCompat.Builder(this, channelID)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setVibrate(vibrate)
            .setTimeoutAfter(6 * 100000)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Оповещение системы контроля EPower")
            .setStyle(NotificationCompat.BigTextStyle().bigText(infoText))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_launcher_foreground, "Отключить", PendingIntent.getActivity(this, 100, listenerIntent, PendingIntent.FLAG_CANCEL_CURRENT))
            .setPriority(NotificationCompat.PRIORITY_MAX)

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
}