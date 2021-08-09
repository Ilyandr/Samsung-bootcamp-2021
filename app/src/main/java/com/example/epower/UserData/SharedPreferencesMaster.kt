package com.example.epower.UserData

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("CommitPrefEdits")
class SharedPreferencesMaster(private val context: Context)
{
   private val sharedPreferences: SharedPreferences
   private val editor: SharedPreferences.Editor
   private val tableName: String = "GENERAL USER SETTINGS"

   init
   {
     this.sharedPreferences = this.context.getSharedPreferences(tableName, Context.MODE_PRIVATE);
     this.editor = this.sharedPreferences.edit()
   }

   fun generalThemeApp(setTheme: Boolean?): Boolean
   {
      val nameColumnTheme  = "THEME"

      if (setTheme != null)
      {
         try { editor.remove(nameColumnTheme).apply() }
         catch (ignored: Exception) { }
         editor.putBoolean(nameColumnTheme, setTheme).apply()
      }

      return sharedPreferences.getBoolean(nameColumnTheme, false)
   }

   fun firstSetting(firstSetting: Boolean?): Boolean
   {
      val nameColumnTheme  = "FIRST_SETTING"

      if (firstSetting != null)
      {
         try { editor.remove(nameColumnTheme).apply() }
         catch (ignored: Exception) { }
         editor.putBoolean(nameColumnTheme, firstSetting).apply()
      }

      return sharedPreferences.getBoolean(nameColumnTheme, false)
   }

   fun adminPermission(give: Boolean?): Boolean
   {
      val nameColumnAP  = "ADMIN_PERMISSION"

      if (give != null)
      {
         try { editor.remove(nameColumnAP).apply() }
         catch (ignored: Exception) { }
         editor.putBoolean(nameColumnAP, give).apply()
      }

      return sharedPreferences.getBoolean(nameColumnAP, false)
   }

   fun clearAll() { this.editor.clear().apply() }
}