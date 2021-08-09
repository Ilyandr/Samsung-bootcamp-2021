package com.example.epower.AdapterPackage

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.epower.ControllerThemeApp
import com.example.epower.R
import com.example.epower.UserData.ListSQL

class StatisticsListAdapter(private val settingsList: List<ListSQL>
, private val context: Context
, private val controllerThemeApp: ControllerThemeApp
, private val themeApp: Boolean): BaseAdapter()
{
    private val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var dataSingleItem: ListSQL
    private lateinit var view: View

    private lateinit var linearLayout: LinearLayout
    private lateinit var tv_sensorName: TextView
    private lateinit var tv_sensorTime: TextView
    private lateinit var tv_sensorData: TextView


    override fun getCount() = this.settingsList.size
    override fun getItem(position: Int): ListSQL = this.settingsList[position]
    override fun getItemId(position: Int) = position.toLong()
    private fun infoAdapter(position: Int): ListSQL = getItem(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        if (convertView != null) this.view = convertView
        else this.view = this.layoutInflater.inflate(R.layout.history_single_adapter, parent, false);

        findObjects(position)
        setSingleItem()

        return this.view
    }

    private fun findObjects(position: Int)
    {
        this.tv_sensorName = view.findViewById(R.id.sensorName)
        this.tv_sensorTime = view.findViewById(R.id.sensorTime)
        this.tv_sensorData = view.findViewById(R.id.sensorData)
        this.linearLayout = view.findViewById(R.id.linearAdapterStat)
        this.dataSingleItem = infoAdapter(position)
    }

    @SuppressLint("NewApi")
    private fun setSingleItem()
    {
        this.controllerThemeApp.setTextDesign(this.tv_sensorName, this.dataSingleItem.sensorName, this.themeApp)
        this.controllerThemeApp.setTextDesign(this.tv_sensorTime, this.dataSingleItem.sensorInfo.toString() + "с", this.themeApp)
        this.controllerThemeApp.setTextDesign(this.tv_sensorData, this.dataSingleItem.sensorData, this.themeApp)

        val idDrawView: Int = if (themeApp) R.drawable.style_adapter_history_black else R.drawable.style_adapter_history_white
        this.linearLayout.setBackgroundResource(idDrawView)

        this.view.findViewById<View>(R.id.separator1).backgroundTintList = this.controllerThemeApp.colorButton(themeApp)
        this.view.findViewById<View>(R.id.separator2).backgroundTintList = this.controllerThemeApp.colorButton(themeApp)
    }
}


