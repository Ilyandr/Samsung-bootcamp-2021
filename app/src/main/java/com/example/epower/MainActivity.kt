package com.example.epower


import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.epower.AdapterPackage.DataSingleItem
import com.example.epower.AdapterPackage.SettingsListAdapter
import com.example.epower.Services.ControllerSensors
import com.example.epower.UserData.SharedPreferencesMaster
import java.util.*


class MainActivity : AppCompatActivity()
{
   private lateinit var btnMenu: AppCompatButton
   private lateinit var btnFAQ: AppCompatButton
   private lateinit var generalText: TextView
   private lateinit var listView: ListView

   private lateinit var controllerThemeApp: ControllerThemeApp
   private lateinit var sharedPreferencesMaster: SharedPreferencesMaster


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

      this.controllerThemeApp = ControllerThemeApp(this)
      this.sharedPreferencesMaster = SharedPreferencesMaster(this)
    }

    private fun drawView()
    {
       val themeApp = this.sharedPreferencesMaster.generalThemeApp(null)

       this.controllerThemeApp.setDesignButton(themeApp, this.btnMenu, R.drawable.menu_btn, 0)
       this.controllerThemeApp.setDesignButton(themeApp, this.btnFAQ, R.drawable.info_btn, 2)
       this.controllerThemeApp.designGeneralView(themeApp, findViewById(R.id.main_linearTheme))
       this.controllerThemeApp.setTextDesign(this.generalText, "EPower", themeApp)
    }

    private fun startWorkActivity()
    {
      val listSettings: ArrayList<DataSingleItem> = ArrayList()

      listSettings.add(DataSingleItem("Умный режим" , true))
      listSettings.add(DataSingleItem("Авто таймер WiFi" , false))
      listSettings.add(DataSingleItem("Авто таймер моб.сети" , false))
      listSettings.add(DataSingleItem("Авто таймер Bluetooth" , false))
      listSettings.add(DataSingleItem("Авто таймер GPS" , false))
      listSettings.add(DataSingleItem("Оповещение расхода батареи" , false))

      this.listView.adapter = SettingsListAdapter(listSettings, this)
      startService(Intent(this, ControllerSensors::class.java))

      /* Disable admin permission
      (getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager).removeActiveAdmin(ComponentName(this, AdminPermissionReceiver::class.java)) */

      /* Clear All Data SharedPref
      sharedPreferencesMaster.clearAll() */
    }
}