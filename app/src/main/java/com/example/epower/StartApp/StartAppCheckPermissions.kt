package com.example.epower.StartApp

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.epower.ControllerThemeApp
import com.example.epower.MainActivity
import com.example.epower.R
import com.example.epower.UserData.SharedPreferencesMaster


@Suppress("DEPRECATION")
class StartAppCheckPermissions : AppCompatActivity()
{
    private lateinit var btn_getPermissions: AppCompatButton
    private lateinit var btn_next: AppCompatButton
    private lateinit var sharedPreferencesMaster: SharedPreferencesMaster
    private lateinit var allPermissions: Array<String>

    companion object
    {
        private const val REQUEST_CODE: Int = 10
        private const val RESULT_ENABLE_SUPER_ADMIN = 11
        private const val RESULT_ENABLE_SUPER_WRITE = 12
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app_check_permissions)

        findObjects()
        drawView()
        startWorkActivity()
    }

    private fun findObjects()
    {
        this.btn_getPermissions = findViewById(R.id.btn_getPermissions)
        this.btn_next = findViewById(R.id.btn_next)
        this.sharedPreferencesMaster = SharedPreferencesMaster(this)
    }

    @SuppressLint("ResourceType")
    private fun drawView()
    {
        val themeApp = true
        val controllerThemeApp = ControllerThemeApp(this)

        controllerThemeApp.setDesignButton(themeApp, this.btn_getPermissions,
            R.drawable.style_get_permissions, 2)
        controllerThemeApp.setDesignButton(themeApp, this.btn_next, R.drawable.style_next, 2)

        controllerThemeApp.designGeneralView(themeApp, findViewById(R.id.main_linear))
        controllerThemeApp.setTextDesign(findViewById(R.id.mainText), resources.getString(R.string.hello_one),false)
    }

    @SuppressLint("InlinedApi")
    private fun startWorkActivity()
    {
        if (this.sharedPreferencesMaster.firstSetting(null)) goToNext()
        else
        {
            this.allPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_ADMIN
                , Manifest.permission.BLUETOOTH
                , Manifest.permission.VIBRATE
                , Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION)

            this.btn_getPermissions.setOnClickListener { requestPerms() }
            this.btn_next.setOnClickListener { goToNext() }
            this.btn_next.isEnabled = false
        }
    }

    private fun goToNext()
    {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun requestPerms()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ActivityCompat.requestPermissions(this, allPermissions, REQUEST_CODE)
        else Toast.makeText(this, "Ошибка - приложение не может получить доступ к разрешениям", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE)
        {
            if (this.allPermissions.all { ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED }) superAdminPermission(false)
            else
            {
                Toast.makeText(this, "Ошибка - предоставлены не все разрешения", Toast.LENGTH_LONG).show()
                requestPerms()
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun superAdminPermission(twoPermissions: Boolean)
    {
        Handler().postDelayed(
        {
            val intent: Intent

            if (!twoPermissions)
            {
                intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
                startActivityForResult(intent, RESULT_ENABLE_SUPER_WRITE)
            }
            else
            {
                intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, AdminPermissionReceiver::class.java))
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Данное разрешение необходимо для авто-блокировки экрана в случаях, когда вы бездействуете.")
                startActivityForResult(intent, RESULT_ENABLE_SUPER_ADMIN)
            }
        }, 500)
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

            when (requestCode)
            {
                RESULT_ENABLE_SUPER_WRITE ->  superAdminPermission(true)
                RESULT_ENABLE_SUPER_ADMIN ->
                {
                    this.btn_next.isEnabled = true
                    this.btn_getPermissions.isEnabled = false

                    this.btn_next.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFFFF"))
                    this.btn_getPermissions.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#66FFFFFF"))

                    this.sharedPreferencesMaster.firstSetting(true)
                }
        }
    }
}