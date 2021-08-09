package com.example.epower.StartApp

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.epower.UserData.SharedPreferencesMaster

class AdminPermissionReceiver: DeviceAdminReceiver()
{
    override fun onEnabled(context: Context, intent: Intent)
    {
        Toast.makeText(context, "Права администратора успешно даны", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent)
    {
        Toast.makeText(context, "Права администратора успешно отменены", Toast.LENGTH_SHORT).show()
    }
}