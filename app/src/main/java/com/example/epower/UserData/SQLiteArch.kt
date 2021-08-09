package com.example.epower.UserData

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteArch(context: Context?) : SQLiteOpenHelper(context, name_sql, null, version_sql)
{
    override fun onCreate(db: SQLiteDatabase) { db.execSQL(COMMAND_SQL_CREATE) }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { db.execSQL(COMMAND_SQL_REMOVE); onCreate(db) }

    companion object
    {
        private const val version_sql = 2
        private const val name_sql = "Change_analytics"
        const val TABLE_NAME = "CHANGES"
        const val CHANGE_ID = "_id"
        const val CHANGE_INFO = "info"
        const val SENSOR_NAME = "sensorName"
        const val CHANGE_DATA = "number"
        private const val COMMAND_SQL_CREATE = "create table $TABLE_NAME ($CHANGE_ID integer primary key, $SENSOR_NAME text, $CHANGE_INFO integer, $CHANGE_DATA text)"
        private const val COMMAND_SQL_REMOVE = "drop table if exists $TABLE_NAME"
    }
}
