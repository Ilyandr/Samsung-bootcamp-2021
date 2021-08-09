package com.example.epower


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.epower.AdapterPackage.DataSingleItem
import com.example.epower.AdapterPackage.SettingsListAdapter
import com.example.epower.Fragments.FragmentMenu
import com.example.epower.Fragments.FragmentSettings.Companion.updateTheme
import com.example.epower.Services.ControllerSensors
import com.example.epower.Services.ControllerSensors.Companion.CHANNEL_BLUETOOTH
import com.example.epower.Services.ControllerSensors.Companion.CHANNEL_DISPLAY
import com.example.epower.Services.ControllerSensors.Companion.CHANNEL_GPS
import com.example.epower.Services.ControllerSensors.Companion.CHANNEL_MNETWORK
import com.example.epower.Services.ControllerSensors.Companion.CHANNEL_WIFI
import com.example.epower.UserData.ListSQL
import com.example.epower.UserData.RequestsSQL
import com.example.epower.UserData.RequestsSQL.Companion.MODE_READ_ALL
import com.example.epower.UserData.SharedPreferencesMaster
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.lang.ArithmeticException
import java.lang.Exception
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity()
{
   private lateinit var btnMenu: AppCompatButton
   private lateinit var btnFAQ: AppCompatButton
   private lateinit var generalText: TextView
   private lateinit var listView: ListView
   private lateinit var mainRelativeLayout: RelativeLayout
   private lateinit var pieChart: PieChart

   private lateinit var controllerThemeApp: ControllerThemeApp
   private lateinit var sharedPreferencesMaster: SharedPreferencesMaster
   private lateinit var anim_menu_back_btn: Animation

   private lateinit var fragmentMenu: FragmentMenu
   private lateinit var fragmentsList: ArrayList<Fragment>
   private lateinit var listSettings: ArrayList<DataSingleItem>
   private var themeApp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findObjects()
        drawView()
        startWorkActivity()
    }

    private fun findObjects()
    {
        this.btnMenu = findViewById(R.id.btnMenu)
        this.btnFAQ = findViewById(R.id.btnFAQ)
        this.generalText = findViewById(R.id.main_text)
        this.listView = findViewById(R.id.listUserSettings)
        this.mainRelativeLayout = findViewById(R.id.main_linear)
        this.pieChart = findViewById(R.id.lineChart)

        this.controllerThemeApp = ControllerThemeApp(this)
        this.sharedPreferencesMaster = SharedPreferencesMaster(this)

        this.fragmentMenu = FragmentMenu()
        this.fragmentsList = ArrayList()
        this.anim_menu_back_btn = AnimationUtils.loadAnimation(this, R.anim.back_button_magic)
    }

    @SuppressLint("NewApi")
    private fun drawView()
    {
       this.themeApp = this.sharedPreferencesMaster.generalThemeApp(null)

       this.controllerThemeApp.setDesignButton(!themeApp, this.btnMenu, R.drawable.menu_btn, 0)
       this.controllerThemeApp.setDesignButton(!themeApp, this.btnFAQ, R.drawable.info_btn, 2)
       this.controllerThemeApp.setTextDesign(this.generalText, "EPower", !themeApp)

       this.mainRelativeLayout.backgroundTintList = this.controllerThemeApp.colorButton(themeApp)
       findViewById<View>(R.id.separator1).backgroundTintList = this.controllerThemeApp.colorButton(!themeApp)
       findViewById<View>(R.id.separator2).backgroundTintList = this.controllerThemeApp.colorButton(!themeApp)

       createPieChart()
       setListSettings()
       this.listView.dividerHeight = 20
    }

    @SuppressLint("SetTextI18n")
    private fun startWorkActivity()
    {
      startService(Intent(this, ControllerSensors::class.java))

        this.btnMenu.setOnClickListener {
            Handler().postDelayed(
            {
                if (leftMenuAnim)
                {
                    leftMenuAnim = false

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                        .replace(mainRelativeLayout.id, this.fragmentMenu)
                        .commit()
                }
                else
                {
                    leftMenuAnim = true
                    it.startAnimation(anim_menu_back_btn)
                    this.onBackPressed()
                }
            }, 50)
        }

        this.btnFAQ.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_button_add_magic))
            val dialog_confirm_password = AlertDialog.Builder(this)

            dialog_confirm_password.setTitle("Приложение EPower")
            dialog_confirm_password.setIcon(R.mipmap.ic_launcher_foreground)

            val info = TextView(this)
            info.text = "Создатель: Илья Александрович. ВК: id_cs_sourse"
            info.setTextColor(Color.WHITE)
            info.textSize = 20f

            dialog_confirm_password.setView(info)
            dialog_confirm_password.setPositiveButton("Назад") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            dialog_confirm_password.show()
        }
    }

    private fun createPieChart()
    {
        try {
            val listUserInfo = ArrayList<ListSQL>()
            val dataList = ArrayList<PieEntry>()

            var sumWiFiSensor: Long = 0
            var sumMNetworkSensor: Long = 0
            var sumBluetoothSensor: Long = 0
            var sumGPSSensor: Long = 0
            var sumDisplaySensor: Long = 0

            RequestsSQL(this).readFromDB(listUserInfo, MODE_READ_ALL)
            for (i in listUserInfo.indices)
            {
                when (listUserInfo[i].sensorName)
                {
                    CHANNEL_GPS -> sumGPSSensor += listUserInfo[i].sensorInfo
                    CHANNEL_WIFI -> sumWiFiSensor += listUserInfo[i].sensorInfo
                    CHANNEL_MNETWORK -> sumMNetworkSensor += listUserInfo[i].sensorInfo
                    CHANNEL_BLUETOOTH -> sumBluetoothSensor += listUserInfo[i].sensorInfo
                    CHANNEL_DISPLAY -> sumDisplaySensor += listUserInfo[i].sensorInfo
                }
            }

            val sumAllSensors: Long = sumGPSSensor + sumWiFiSensor + sumMNetworkSensor + sumBluetoothSensor + sumDisplaySensor

            dataList.add(PieEntry(((sumWiFiSensor.toFloat() / sumAllSensors.toFloat()) * 100), CHANNEL_WIFI))
            dataList.add(PieEntry(((sumMNetworkSensor.toFloat() / sumAllSensors.toFloat()) * 100), CHANNEL_MNETWORK))
            dataList.add(PieEntry(((sumGPSSensor.toFloat() / sumAllSensors.toFloat()) * 100), CHANNEL_GPS))
            dataList.add(PieEntry(((sumBluetoothSensor.toFloat() / sumAllSensors.toFloat()) * 100), CHANNEL_BLUETOOTH))
            dataList.add(PieEntry(((sumDisplaySensor.toFloat() / sumAllSensors.toFloat()) * 100), CHANNEL_DISPLAY))

            val colorNormal = if (!themeApp) Color.parseColor("#FFFFFFFF") else Color.parseColor("#000000")
            val pieDataSet = PieDataSet(dataList, null)

            pieDataSet.colors = ColorTemplate.createColors(intArrayOf(
                Color.parseColor("#14DEBB")
                , Color.parseColor("#94ADFF")
                , Color.parseColor("#BC20FA")
                , Color.parseColor("#E32112")
                , Color.parseColor("#FFBD5F")))
            pieDataSet.valueTextSize = 14f
            pieDataSet.valueTextColor = colorNormal

            this.pieChart.data = PieData(pieDataSet)

            pieChart.centerText = "Ваша статистика"
            pieChart.setCenterTextSize(14f)
            pieChart.setCenterTextColor(colorNormal)

            pieChart.description.textColor = colorNormal
            pieChart.description.text = "Данные в процентах %"
            pieChart.description.textSize = 12f

            pieChart.legend.textColor = colorNormal
            pieChart.legend.textSize = 13f

            pieChart.setHoleColor(this.controllerThemeApp.getMainColorApp(this.themeApp))
            pieChart.setTransparentCircleAlpha(60)
            pieChart.setDrawSliceText(false)
            pieChart.setUsePercentValues(true)

            this.pieChart.invalidate()
        } catch (exceptionIgnored: ArithmeticException) { Log.d("Create PieChart", "EMPTY SQL DB") }
    }

    private fun setListSettings()
    {
        try { listSettings.clear() } catch (ignored: Exception) { }
        listSettings = ArrayList()

        listSettings.add(DataSingleItem("Умный режим" , true))
        listSettings.add(DataSingleItem("Авто таймер WiFi" , false))
        listSettings.add(DataSingleItem("Авто таймер моб.сети" , false))
        listSettings.add(DataSingleItem("Авто таймер Bluetooth" , false))
        listSettings.add(DataSingleItem("Авто таймер GPS" , false))
        listSettings.add(DataSingleItem("Оповещение расхода батареи" , false))

        this.listView.adapter = SettingsListAdapter(listSettings, this, this.controllerThemeApp, !this.themeApp)
    }
    override fun onBackPressed()
    {
        if (updateTheme) { drawView(); updateTheme = false }
        leftMenuAnim = true

        this.fragmentsList.clear()
        try
        {
            this.fragmentsList = supportFragmentManager.fragments as ArrayList<Fragment>

            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.menu_for_right, R.anim.menu_for_left)
                .remove(this.fragmentsList[this.fragmentsList.size - 1])
                .commit()
        } catch (emptyList: ClassCastException) { super.onBackPressed() }
    }

    companion object { @JvmStatic var leftMenuAnim: Boolean = true }
}