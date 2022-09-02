/*
 * This file is a part of Don't Forget
 *
 * Don't Forget is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.tiendatle.dontforget

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import java.io.File

class TimePickActivity : Activity() {
    private var notificationID: Int = 0
    private var Content = ""
    private var origin = ""
    private var year = 0
    private var month = 0
    private var date = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_pick)

        this.notificationID = intent.getIntExtra("ID", 1)
        this.Content = intent.getStringExtra("Content").toString()
        this.origin = intent.getStringExtra("Origin").toString()
        this.year = intent.getIntExtra("Year", 1)
        this.month = intent.getIntExtra("Month", 1)
        this.date = intent.getIntExtra("Date", 1)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    public fun onClick(view: android.view.View) {
        Log.d("LOG_TIMEPICK", notificationID.toString())
        Log.d("LOG_TIMEPICK", date.toString())

        val timePicker = findViewById<TimePicker>(R.id.timepicker)
        val hour = timePicker.hour
        val minute = timePicker.minute

        var calendar = Calendar.getInstance()
        calendar.set(year, month, date, hour, minute, 0)
        val triggerAt = calendar.timeInMillis

        if (origin == "activity_create_note") {
            Log.d("LOG_TIMEPICK", "Origin is activity_create_note")
            val redirectIntent = Intent(this, CreateNoteActivity::class.java)
            redirectIntent.putExtra("ID", notificationID)
            redirectIntent.putExtra("Content", Content)
            redirectIntent.putExtra("remindTime", triggerAt)
            redirectIntent.putExtra("Origin", "activity_time_pick")
            redirectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(redirectIntent)
        }
        else if (origin == "activity_edit_note") {
            Log.d("LOG_TIMEPICK", "Origin is activity_create_note")
            val redirectIntent = Intent(this, EditNoteActivity::class.java)
            redirectIntent.putExtra("ID", notificationID)
            redirectIntent.putExtra("Content", Content)
            redirectIntent.putExtra("remindTime", triggerAt)
            redirectIntent.putExtra("Origin", "activity_time_pick")
            redirectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(redirectIntent)
        }
        else {
            val intent = Intent(this, RefreshNote::class.java)
            intent.putExtra("ID", notificationID)
            intent.putExtra("Content", Content)
            intent.putExtra("isSounded", true)
            val pendingIntent = PendingIntent.getBroadcast(this, notificationID, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            var alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)

            //Update entry to include triggerAt
            val fileHandler = FileHandler()
            val dir = File(this.filesDir, "data.dat")
            val temp = File(this.filesDir, "temp.data")
            val newEntry = fileHandler.constructEntry(notificationID.toString(), Content, triggerAt.toString())
            fileHandler.updateEntry(dir, temp ,notificationID.toString(), newEntry)
        }
        this.finish()
    }
}