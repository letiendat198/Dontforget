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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.File
import java.text.SimpleDateFormat

class EditNoteActivity : AppCompatActivity() {
    private var notificationID = 0
    private var contentOriginal = ""
    private var origin = ""
    private var triggerAt: Long = 0
    private var isReminderSet = false
    private var isNeedAlarmCancel = false
    private var isHaveReminderAlready = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        this.notificationID = intent.getIntExtra("ID", 1)
        Log.d("LOG_EDIT_NOTE", notificationID.toString())
        this.contentOriginal = intent.getStringExtra("Content").toString()
        this.origin = intent.getStringExtra("Origin").toString()
        this.triggerAt = intent.getLongExtra("triggerAt", 1)

        val edittext = findViewById<EditText>(R.id.contentEditNote)
        val onEditorActionListener = object: TextView.OnEditorActionListener {
            override fun onEditorAction(view: TextView, actionID: Int, event: KeyEvent?): Boolean {
                if (actionID == EditorInfo.IME_ACTION_SEND) {
                    onConfirm(view)
                }
                return true
            }
        }
        edittext.setOnEditorActionListener(onEditorActionListener)
        val editable = SpannableStringBuilder(contentOriginal)
        edittext.text = editable

        if (triggerAt != 0L && triggerAt != 1L) {
            Log.d("LOG_EDIT_ACTIVITY", "triggerAt is present")
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            if (now < triggerAt) {
                this.isReminderSet = true
                calendar.timeInMillis = triggerAt

                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
                val date:String = simpleDateFormat.format(calendar.time)

                Log.d("LOG_EDIT_ACTIVITY", simpleDateFormat.toString())
                val remindInfo = findViewById<TextView>(R.id.remindInfoEdit)
                remindInfo.text = "Reminder set to: $date"

                val cancelBut = findViewById<Button>(R.id.cancelreminderEdit)
                cancelBut.visibility = View.VISIBLE

                this.isHaveReminderAlready = true
            }
        }
    }

    public fun onConfirm(view: View) {
        val contentInput = findViewById<EditText>(R.id.contentEditNote)
        val content = contentInput.text.toString()
        if (content != "") {
            Log.d("LOG_EDIT_ACTIVITY", content)

            /* val intent = Intent(this, RefreshNote::class.java)
            intent.putExtra("ID", notificationID)
            intent.putExtra("Content", content)
            intent.putExtra("isSounded", false)
            sendBroadcast(intent) */



            //If isReminderSet = true -> Create an Alarm
            if (isReminderSet) {
                //Add note data to save file
                val fileHanlder = FileHandler()
                val newEntry = fileHanlder.constructEntry(notificationID.toString(), content, triggerAt.toString())
                val dir = File(this.filesDir, "data.dat")
                val temp = File(this.filesDir, "temp.dat")
                fileHanlder.updateEntry(dir, temp, notificationID.toString(), newEntry)

                val intent = Intent(this, RefreshNote::class.java)
                intent.putExtra("ID", notificationID)
                intent.putExtra("Content", content)
                intent.putExtra("isSounded", true)
                val pendingIntent = PendingIntent.getBroadcast(this, notificationID, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                var alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
            else {
                //Add note data to save file
                val fileHanlder = FileHandler()
                val newEntry = fileHanlder.constructEntry(notificationID.toString(), content, null)
                val dir = File(this.filesDir, "data.dat")
                val temp = File(this.filesDir, "temp.dat")
                fileHanlder.updateEntry(dir, temp, notificationID.toString(), newEntry)

                //Cancel alarm
                if (isNeedAlarmCancel) {
                    val intent = Intent(this, RefreshNote::class.java)
                    intent.putExtra("ID", notificationID)
                    intent.putExtra("Content", contentOriginal)
                    intent.putExtra("isSounded", true)
                    val pendingIntent = PendingIntent.getBroadcast(this, notificationID, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    var alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(pendingIntent)
                }
            }
        }

        val reloadMain = Intent(this, MainActivity::class.java)
        startActivity(reloadMain)
        this.finish()
    }

    override fun onBackPressed() {
        val reloadMain = Intent(this, MainActivity::class.java)
        startActivity(reloadMain)
        this.finish()
    }
    public fun onReminderClick(view: View) {
        val contentInput = findViewById<EditText>(R.id.contentEditNote)
        val content = contentInput.text.toString()
        Log.d("LOG_EDIT_ACTIVITY", content)
        val notificationID = System.currentTimeMillis().toInt();

        val reminderIntent = Intent(this, ReminderActivity::class.java)
        reminderIntent.putExtra("Origin", "activity_edit_note")
        reminderIntent.putExtra("ID", notificationID)
        reminderIntent.putExtra("Content", content)
        startActivity(reminderIntent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        //super.onNewIntent(intent)
        Log.d("LOG_EDIT_ACTIVITY", "onNewIntent")
        val origin = intent.getStringExtra("Origin")
        Log.d("LOG_EDIT_ACTIVITY", origin.toString())
        if (origin == "activity_time_pick") {
            this.triggerAt = intent.getLongExtra("remindTime", 0)
            this.isReminderSet = true

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = triggerAt

            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            val date:String = simpleDateFormat.format(calendar.time)

            Log.d("LOG_EDIT_ACTIVITY", simpleDateFormat.toString())
            val remindInfo = findViewById<TextView>(R.id.remindInfoEdit)
            remindInfo.text = "Reminder set to: $date"
            remindInfo.invalidate()
            remindInfo.requestLayout()

            val cancelBut = findViewById<Button>(R.id.cancelreminderEdit)
            cancelBut.visibility = View.VISIBLE
        }
    }
    public fun onCancel (view: View) {
        this.isReminderSet = false
        if (isHaveReminderAlready) {
            this.isNeedAlarmCancel = true
        }
        val remindText = findViewById<TextView>(R.id.remindInfoEdit)
        remindText.setText("")
        view.visibility = View.INVISIBLE
    }
}