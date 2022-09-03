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

import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create Notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Note creator";
            val description = "Allow you to add notes on notification";
            val importanceCreator = NotificationManager.IMPORTANCE_MIN;
            val importanceNote = NotificationManager.IMPORTANCE_DEFAULT;
            val importanceRemind = NotificationManager.IMPORTANCE_HIGH;
            val defaultChannel = NotificationChannel("NOTE_DEFAULT", name, importanceCreator).apply {
                description
            };
            var reminderChannel = NotificationChannel("NOTE_REMINDER", name, importanceRemind).apply {
                description
            }
            var noteChannel = NotificationChannel("NOTE_CHANNEL", name, importanceNote).apply {
                description
            }
            defaultChannel.setShowBadge(false)
            noteChannel.setSound(null, null)
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            notificationManager.createNotificationChannel(defaultChannel);
            notificationManager.createNotificationChannel(reminderChannel);
            notificationManager.createNotificationChannel(noteChannel)
        }

        Log.d("Log", "this is actually working");

        //Create new remoteInput
        val KEY_TEXT_CREATE = "allseeingeye";
        var creButString = "Enter title";
        var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_CREATE).run {
            setLabel(creButString)
            build()
        }

        //Create pendingIntent
        val creIntent = Intent(this, CreateNote::class.java)
        var crePendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, creIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT);
        var creAction: NotificationCompat.Action = NotificationCompat.Action.Builder(R.drawable.ic_noti, "Create a new note", crePendingIntent).addRemoteInput(remoteInput).build()


        //Remove default notification
        val removeIntent = Intent(this, RemoveNote::class.java)
        val removePendingIntent = PendingIntent.getBroadcast(this, 1, removeIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        //Create Notification
        val builder = NotificationCompat.Builder(this, "NOTE_DEFAULT")
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle("Don't Forget is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setGroup("Default")
            .addAction(creAction)
            .addAction(R.drawable.ic_noti, "Stop", removePendingIntent)

        //Show the Notification
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build());
        }

        //Get all data entries
        val dir = File(filesDir, "data.dat")
        val fileHandler = FileHandler()
        var entries = fileHandler.readEntries(dir)
        if (entries != null) {
            for (entry in entries) {
                Log.d("MAIN_LOG", entry)
            }
        }

        //Define radio button onClick behavior
        val radioOnClick = object: View.OnClickListener {
            override fun onClick(view: View) {
                Log.d("MAIN_LOG", "Radio button clicked")
                val notificationID = view.id
                val targetID = -view.id

                val intent = Intent(view.context, RemoveNote::class.java)
                intent.putExtra("ID", notificationID)
                intent.putExtra("isOnlyRemoveNotification", false)

                val entries = fileHandler.readEntries(dir)
                if (entries != null) {
                    for (entry in entries) {
                        if (entry.startsWith(notificationID.toString())) {
                            val processed = fileHandler.processEntry(entry)
                            val content = processed[1]
                            Log.d("MAIN_LOG", "Content of matching ID found")
                            intent.putExtra("Content", content)
                        }
                    }
                }
                sendBroadcast(intent)

                val parent = findViewById<LinearLayout>(targetID)
                parent.removeAllViews()
            }
        }

        //Displaying entries
        val mainLayout = findViewById<LinearLayout>(R.id.mainlayout)
        val onClickListener = object: View.OnClickListener {
            //Define action when a note is clicked
            override fun onClick(view: View) {
                val notificationID = view.id / 10
                val getTextView = view.rootView.findViewById<TextView>(view.id)
                val content = getTextView.text.toString()
                val editNoteIntent = Intent(view.context, EditNoteActivity::class.java)
                editNoteIntent.putExtra("ID", notificationID)
                editNoteIntent.putExtra("Content", content)
                editNoteIntent.putExtra("Origin", "activity_main")
                if (entries != null) {
                    for (entry in entries) {
                        val processed = fileHandler.processEntry(entry)
                        if (processed[0] == notificationID.toString()) {
                            Log.d("MAIN_LOG", "Matching ID found")
                            if (processed.size == 3) {
                                Log.d("MAIN_LOG", "Reminder is set")
                                val remindTime = processed[2]
                                editNoteIntent.putExtra("triggerAt", remindTime.toLong())
                            }
                        }
                    }
                }
                var context = view.context
                startActivity(editNoteIntent)
                while (context is ContextWrapper){
                    if (context is Activity){
                        context.finish()
                    }
                    val contextWrapper = context as ContextWrapper
                    context = contextWrapper.baseContext
                }
            }
        }
        val font = ResourcesCompat.getFont(this, R.font.opensans_regular)
        if (entries != null) {
            for (entry in entries) {
                val processed = fileHandler.processEntry(entry)
                val noteChild = LinearLayout(this)
                noteChild.id = -processed[0].toInt()
                val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.setMargins(0,0,0,50)
                noteChild.layoutParams = layoutParams
                noteChild.orientation = LinearLayout.HORIZONTAL
                mainLayout.addView(noteChild)
                val radio = RadioButton(this)
                radio.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                radio.id = processed[0].toInt()
                radio.setOnClickListener(radioOnClick)
                noteChild.addView(radio)
                val header = TextView(this)
                val headerLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                headerLayoutParams.setMargins(20,12,0,0)
                header.layoutParams = headerLayoutParams
                header.text = processed[1]
                header.textSize = 25f
                header.setTypeface(font)
                //header.setTextColor(ContextCompat.getColor(this, R.color.black))
                header.isClickable = true
                header.id = processed[0].toInt() * 10
                header.setOnClickListener(onClickListener)
                noteChild.addView(header)
            }
        }

        //Reload all notification
        if (entries != null) {
            for (entry in entries) {
                var processed = fileHandler.processEntry(entry)
                val notificationIntent = Intent(this, RefreshNote::class.java)
                notificationIntent.putExtra("ID", processed[0].toInt())
                notificationIntent.putExtra("Content", processed[1])
                notificationIntent.putExtra("isSounded", false)
                sendBroadcast(notificationIntent)
            }
        }
    }
    public fun onClick(view: View) {
        var intent = Intent(this, CreateNoteActivity::class.java)
        startActivity(intent)
        this.finish()
    }
}