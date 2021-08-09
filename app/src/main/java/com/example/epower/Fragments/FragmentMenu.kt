package com.example.epower.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentTransaction
import com.example.epower.ControllerThemeApp
import com.example.epower.MainActivity.Companion.leftMenuAnim
import com.example.epower.R
import com.example.epower.UserData.SharedPreferencesMaster


class FragmentMenu : Fragment(), OnTouchListener, View.OnClickListener
{
    private lateinit var btn_settings: AppCompatButton
    private lateinit var btn_statistic: AppCompatButton
    private lateinit var mainImage: ImageView
    private lateinit var mainLinearLayout: LinearLayout

    private lateinit var fragmentTransaction: FragmentTransaction
    private lateinit var viewF: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        this.viewF = inflater.inflate(R.layout.fragment_menu, container, false)

        findObjects()
        drawingView()
        startWorkFragment()

        return this.viewF
    }


    @Suppress("DEPRECATION")
    private fun findObjects()
    {
        this.btn_settings = viewF.findViewById(R.id.settings_btn)
        this.btn_statistic = viewF.findViewById(R.id.statistic_btn)
        this.mainImage = viewF.findViewById(R.id.app_photo)
        this.mainLinearLayout = viewF.findViewById(R.id.LL_menu)

        this.fragmentTransaction =
             this.requireFragmentManager()
            .beginTransaction()
            .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
    }

    private fun drawingView()
    {
        val themeApp: Boolean = SharedPreferencesMaster(requireContext()).generalThemeApp(null)
        val controllerThemeApp = ControllerThemeApp(requireContext())

        val listBtn = ArrayList<AppCompatButton>()
        listBtn.add(this.btn_settings)
        listBtn.add(this.btn_statistic)

        this.mainImage.setBackgroundResource(R.mipmap.ic_launcher_foreground)
        controllerThemeApp.designButton(!themeApp, listBtn)
        controllerThemeApp.designGeneralView(themeApp, this.mainLinearLayout)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startWorkFragment()
    {
        this.mainLinearLayout.setOnTouchListener(this)
        this.btn_settings.setOnClickListener(this)
        this.btn_statistic.setOnClickListener(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean
    {
        if (event!!.action == MotionEvent.ACTION_MOVE)
        {
            leftMenuAnim = true
            requireActivity().onBackPressed()
        }
        return true
    }

    override fun onClick(v: View?)
    {
        leftMenuAnim = false
        requireActivity().onBackPressed()

        when(v!!.id)
        {
            R.id.settings_btn -> fragmentTransaction.replace(R.id.main_linear, FragmentSettings()).commit()
            R.id.statistic_btn -> fragmentTransaction.replace(R.id.main_linear, FragmentStatistics()).commit()
        }
    }
}