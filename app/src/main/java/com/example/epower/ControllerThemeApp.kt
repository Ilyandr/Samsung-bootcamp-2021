package com.example.epower

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton

@SuppressLint("NewApi")
class ControllerThemeApp(private val context: Context)
{
    @SuppressLint("RestrictedApi")
    fun setDesignButton(themeApp: Boolean, button: AppCompatButton, drawResource: Int, positionDraw: Short)
    {
        val positionArr = arrayOfNulls<Drawable>(4)

        for (i in positionArr.indices)
        positionArr[i] = if (i == positionDraw.toInt()) AppCompatResources.getDrawable(this.context, drawResource) else null

        button.setCompoundDrawablesWithIntrinsicBounds(positionArr[0], positionArr[1], positionArr[2], positionArr[3])
        button.supportCompoundDrawablesTintList = colorButton(themeApp)
    }

    fun designGeneralView(themeApp: Boolean, layout: LinearLayout) { layout.backgroundTintList = colorButton(themeApp) }

    private fun colorButton(themeApp: Boolean) : ColorStateList =
        if (themeApp) ColorStateList.valueOf(Color.parseColor("#000000"))
        else ColorStateList.valueOf(Color.parseColor("#FFFFFFFF"))

    fun setTextDesign(textView: TextView, text: String, theme: Boolean)
    {
        textView.setTextColor(colorButton(theme))
        textView.text = text
    }
}

