package com.example.epower.AdapterPackage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import com.example.epower.ControllerThemeApp
import com.example.epower.R

open class SettingsListAdapter(private val settingsList: List<DataSingleItem>
, private val context: Context
, private val controllerThemeApp: ControllerThemeApp
, private val themeApp: Boolean): BaseAdapter()
{
    private val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var dataSingleItem: DataSingleItem
    private lateinit var view: View

    private lateinit var btnInfoSettings: AppCompatButton
    private lateinit var onOffSetSwitch: SwitchCompat

    override fun getCount() = this.settingsList.size
    override fun getItem(position: Int): DataSingleItem = this.settingsList[position]
    override fun getItemId(position: Int) = position.toLong()
    private fun infoAdapter(position: Int): DataSingleItem = getItem(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View
    {
        if (convertView != null) this.view = convertView
        else this.view = this.layoutInflater.inflate(R.layout.layout_adaper_singlevalue, parent, false);

        findObjects(position)
        setSingleItem()

        return this.view
    }

    private fun findObjects(position: Int)
    {
        this.btnInfoSettings = this.view.findViewById(R.id.btnMehSet)
        this.onOffSetSwitch = this.view.findViewById(R.id.onOffSet)
        this.dataSingleItem = infoAdapter(position)
    }

    private fun setSingleItem()
    {
        this.btnInfoSettings.text = this.dataSingleItem.singleSettingName
        this.onOffSetSwitch.isChecked = this.dataSingleItem.singleConditionSwitch

        val btnList = ArrayList<AppCompatButton>()
        btnList.add(this.btnInfoSettings)

        this.controllerThemeApp.designButton(themeApp, btnList)
    }
}