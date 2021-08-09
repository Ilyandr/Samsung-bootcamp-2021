package com.example.epower.Services

import android.app.Activity
import android.os.Bundle
import android.app.NotificationManager


class NotificationCancer: Activity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        finish()
    }
}