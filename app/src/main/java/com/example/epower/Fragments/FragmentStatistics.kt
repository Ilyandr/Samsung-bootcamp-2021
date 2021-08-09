package com.example.epower.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.widget.AppCompatButton
import com.example.epower.AdapterPackage.StatisticsListAdapter
import com.example.epower.ControllerThemeApp
import com.example.epower.R
import com.example.epower.UserData.ListSQL
import com.example.epower.UserData.RequestsSQL
import com.example.epower.UserData.RequestsSQL.Companion.MODE_READ_ALL
import com.example.epower.UserData.SharedPreferencesMaster

class FragmentStatistics : Fragment()
{
    private lateinit var viewF: View
    private lateinit var btnBack: AppCompatButton
    private lateinit var listStatistics: ListView

    private var themeApp: Boolean = false
    private lateinit var controllerThemeApp: ControllerThemeApp

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        this.viewF = inflater.inflate(R.layout.fragment_statistics, container, false)

        findObjects()
        drawView()
        startWorkFragment()

        return this.viewF
    }

    private fun findObjects()
    {
        this.btnBack = viewF.findViewById(R.id.btnBack)
        this.listStatistics = viewF.findViewById(R.id.statisticList)

        this.themeApp = SharedPreferencesMaster(requireContext()).generalThemeApp(null)
        this.controllerThemeApp = ControllerThemeApp(requireContext())
    }

    @SuppressLint("NewApi")
    private fun drawView()
    {
        this.controllerThemeApp.designGeneralView(themeApp, viewF.findViewById(R.id.mainLinearStatistics))
        this.controllerThemeApp.setDesignButton(!themeApp, this.btnBack, R.drawable.btn_back_style, 0)

        this.viewF.findViewById<View>(R.id.separator1).backgroundTintList = this.controllerThemeApp.colorButton(!themeApp)
        this.viewF.findViewById<View>(R.id.separator2).backgroundTintList = this.controllerThemeApp.colorButton(!themeApp)
        this.viewF.findViewById<View>(R.id.separator3).backgroundTintList = this.controllerThemeApp.colorButton(!themeApp)
        this.viewF.findViewById<View>(R.id.separator4).backgroundTintList = this.controllerThemeApp.colorButton(!themeApp)

        this.controllerThemeApp.setTextDesign(viewF.findViewById(R.id.sensorData), "Дата сбора информации", !themeApp)
        this.controllerThemeApp.setTextDesign(viewF.findViewById(R.id.sensorName), "Название параметра (ENG)", !themeApp)
        this.controllerThemeApp.setTextDesign(viewF.findViewById(R.id.sensorTime), "Работа (секунды)", !themeApp)
        this.controllerThemeApp.setTextDesign(viewF.findViewById(R.id.main_textStatistics), "История", !themeApp)

        this.listStatistics.dividerHeight = 20
    }

    private fun startWorkFragment()
    {
        val statisticsInfoList = ArrayList<ListSQL>()
        RequestsSQL(requireContext()).readFromDB(statisticsInfoList, MODE_READ_ALL)

        val statisticsListAdapter = StatisticsListAdapter(
            statisticsInfoList
            , requireContext()
            , this.controllerThemeApp
            , this.themeApp)

        this.listStatistics.adapter = statisticsListAdapter
        this.btnBack.setOnClickListener { requireActivity().onBackPressed() }
    }
}