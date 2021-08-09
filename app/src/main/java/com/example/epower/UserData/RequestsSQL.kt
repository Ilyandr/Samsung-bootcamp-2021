package com.example.epower.UserData

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.epower.UserData.SQLiteArch.Companion.CHANGE_INFO
import com.example.epower.UserData.SQLiteArch.Companion.TABLE_NAME
import com.example.epower.UserData.SQLiteArch.Companion.CHANGE_DATA
import com.example.epower.UserData.SQLiteArch.Companion.CHANGE_ID
import com.example.epower.UserData.SQLiteArch.Companion.SENSOR_NAME
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RequestsSQL(private val context: Context)
{
    private var SQLiteArch: SQLiteArch
    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var cursor: Cursor

    private var sensorName: Int = 0
    private var sensorInfo: Int = 0
    private var sensorTime: Int = 0
    private var sensorID: Int = 0

    init { this.SQLiteArch = SQLiteArch(this.context) }

    fun writeToDB(sensorName: String, sensorInfo: Int)
    {

            this.SQLiteDatabase = this.SQLiteArch.writableDatabase
            val contentValues = ContentValues()

            contentValues.put(CHANGE_INFO, sensorInfo)
            contentValues.put(CHANGE_DATA, realDataTime())
            contentValues.put(SENSOR_NAME, sensorName)

            this.SQLiteDatabase.insert(TABLE_NAME, null, contentValues)
            contentValues.clear()
    }

    fun readFromDB(convertList: ArrayList<ListSQL>, SELECT_MODE: String?, sensorName: String? = null)
    {
        convertList.clear()
        this.SQLiteDatabase = this.SQLiteArch.readableDatabase

        if (SELECT_MODE != MODE_READ_ALL && sensorName == null) return

        this.cursor = when (SELECT_MODE)
        {
            MODE_READ_ALL -> SQLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null)
            MODE_READ_SINGLE -> SQLiteDatabase.rawQuery(selectSingleEntrySQL + sensorName, null)
            else -> return
        }

        if (cursor.moveToFirst())
        {
            this.sensorID = cursor.getColumnIndex(CHANGE_ID)
            this.sensorName = cursor.getColumnIndex(SENSOR_NAME)
            this.sensorInfo = cursor.getColumnIndex(CHANGE_INFO)
            this.sensorTime = cursor.getColumnIndex(CHANGE_DATA)

            do { convertList.add(ListSQL(
                  cursor.getString(this.sensorName)
                , cursor.getInt(this.sensorInfo)
                , cursor.getString(this.sensorTime))) }
            while (this.cursor.moveToNext())
        }

        cursor.close()
    }

     fun clearAllDataDB()
    {
        this.SQLiteDatabase = this.SQLiteArch.writableDatabase
        SQLiteDatabase.delete(TABLE_NAME, null, null)
    }

    private fun realDataTime() = SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.getDefault()).format(Date())

    companion object
    {
        private const val selectSingleEntrySQL = "SELECT * FROM $TABLE_NAME WHERE $SENSOR_NAME = $"
        const val MODE_READ_ALL = "SELECT ALL"
        const val MODE_READ_SINGLE = "SELECT SINGLE"
    }
}