package com.example.epower.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context.DEVICE_POLICY_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import com.example.epower.ControllerThemeApp
import com.example.epower.R
import com.example.epower.StartApp.AdminPermissionReceiver
import com.example.epower.UserData.RequestsSQL
import com.example.epower.UserData.SharedPreferencesMaster

class FragmentSettings : Fragment()
{
    private lateinit var viewF: View
    private lateinit var btnBack: AppCompatButton
    private lateinit var btn_themeApp: AppCompatButton
    private lateinit var btn_controlAdmin: AppCompatButton
    private lateinit var btn_clearDataApp: AppCompatButton
    private lateinit var switch_themeApp: SwitchCompat

    private lateinit var sharedPreferencesMaster: SharedPreferencesMaster

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        this.viewF = inflater.inflate(R.layout.fragment_settings, container, false)

        findObjects()
        drawingView()
        startWorkFragment()

        return this.viewF
    }

    private fun findObjects()
    {
        this.btnBack = viewF.findViewById(R.id.btnBack)
        this.btn_themeApp = viewF.findViewById(R.id.btn_themeApp)
        this.btn_controlAdmin = viewF.findViewById(R.id.btn_controlAdmin)
        this.btn_clearDataApp = viewF.findViewById(R.id.btn_clearDataApp)
        this.switch_themeApp = viewF.findViewById(R.id.switch_themeApp)

        this.sharedPreferencesMaster = SharedPreferencesMaster(requireContext())
    }

    @SuppressLint("NewApi")
    private fun drawingView()
    {
        val controllerThemeApp = ControllerThemeApp(requireContext())
        val themeApp = this.sharedPreferencesMaster.generalThemeApp(null)

        val btnList =  ArrayList<AppCompatButton>()
        btnList.add(this.btn_themeApp)
        btnList.add(this.btn_clearDataApp)
        btnList.add(this.btn_controlAdmin)

        controllerThemeApp.designButton(!themeApp, btnList)
        controllerThemeApp.designGeneralView(themeApp, viewF.findViewById(R.id.main_linearSettings))
        controllerThemeApp.setDesignButton(!themeApp, this.btnBack, R.drawable.btn_back_style, 0)
        controllerThemeApp.setTextDesign(viewF.findViewById(R.id.main_textSettings), "Настройки", !themeApp)

        this.viewF.findViewById<View>(R.id.separator).backgroundTintList = controllerThemeApp.colorButton(!themeApp)
        this.switch_themeApp.isChecked = this.sharedPreferencesMaster.generalThemeApp(null)
    }

    private fun startWorkFragment()
    {
        this.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        this.switch_themeApp.setOnCheckedChangeListener { _, isChecked -> themeApp(isChecked) }
        this.btn_controlAdmin.setOnClickListener { controlAdminPermission() }
        this.btn_clearDataApp.setOnClickListener { clearDataApp() }

        this.btn_themeApp.setOnClickListener{
            this.switch_themeApp.isChecked = !switch_themeApp.isChecked
            themeApp(!this.sharedPreferencesMaster.generalThemeApp(null))
        }
    }

    private fun themeApp(condition: Boolean)
    {
        this.sharedPreferencesMaster.generalThemeApp(condition)
        val textToast = if (condition) "Светлая тема применена" else "Светлая тема отключена"

        updateTheme = true
        drawingView()

        Toast.makeText(requireContext(), textToast, Toast.LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION")
    private fun controlAdminPermission()
    {
        if (this.sharedPreferencesMaster.adminPermission(null))
        {
            val dialog_confirm_password = AlertDialog.Builder(requireContext())

            dialog_confirm_password.setTitle("Внимание")
            dialog_confirm_password.setMessage("Вы точно хотите запретить доступ к правам администратора? Отсутствие доступа приведёт к ограничению некоторых функций приложения.")

            dialog_confirm_password.setNegativeButton("Отмена") { dialog: DialogInterface, _: Int -> dialog.cancel() }

            dialog_confirm_password.setPositiveButton("Запретить") { dialog: DialogInterface?, _: Int ->
                (requireActivity().getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                    .removeActiveAdmin(ComponentName(requireContext(), AdminPermissionReceiver::class.java))

                this.sharedPreferencesMaster.adminPermission(false)
                dialog!!.cancel()
            }

            dialog_confirm_password.setIcon(R.mipmap.ic_launcher_foreground)
            dialog_confirm_password.show()
        }
        else
        {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(requireContext(), AdminPermissionReceiver::class.java))
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Данное разрешение необходимо для авто-блокировки экрана в случаях, когда вы бездействуете.")
            startActivityForResult(intent, 11)
        }
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) this.sharedPreferencesMaster.adminPermission(true)
    }

    private fun clearDataApp()
    {
        val dialog_confirm_password = AlertDialog.Builder(requireContext())
        dialog_confirm_password.setTitle("Внимание")
        dialog_confirm_password.setMessage("Вы точно хотите очистить все данные приложения?")

        dialog_confirm_password.setNegativeButton("Отмена") { dialog: DialogInterface, _: Int -> dialog.cancel() }

        dialog_confirm_password.setPositiveButton("Очистить") { dialog: DialogInterface?, _: Int ->

            RequestsSQL(requireContext()).clearAllDataDB()
            Toast.makeText(requireContext(), "Данные успешно очищены", Toast.LENGTH_SHORT).show()

            dialog!!.cancel()
        }

        dialog_confirm_password.setIcon(R.mipmap.ic_launcher_foreground)
        dialog_confirm_password.show()
    }

    companion object { @JvmStatic var updateTheme = false }
}