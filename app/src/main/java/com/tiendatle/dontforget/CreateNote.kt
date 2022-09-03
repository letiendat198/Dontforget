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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.BroadcastReceiver;
import android.util.Log
import androidx.core.app.RemoteInput
import androidx.lifecycle.Lifecycle
import java.io.File

class CreateNote: BroadcastReceiver() {
    public override fun onReceive(context: Context, intent: Intent) {
        Log.d("LOG_CREATE", "OnReceive was called");
        var res = RemoteInput.getResultsFromIntent(intent)?.getCharSequence("allseeingeye")
        Log.d("LOG_CREATE", res.toString())

        val notificationID = System.currentTimeMillis().toInt();
        Log.d("LOG_CREATE", notificationID.toString())

        //Create Remind Me intent
        val remindIntent = Intent(context, ReminderActivity::class.java)
        remindIntent.putExtra("ID",notificationID)
        remindIntent.putExtra("Content", res.toString())
        remindIntent.putExtra("Origin", "create_note")
        val remindPendingIntent = PendingIntent.getActivity(context, notificationID, remindIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        //Create Remove intent
        val removeIntent = Intent(context, RemoveNote::class.java)
        removeIntent.putExtra("ID", notificationID)
        removeIntent.putExtra("Content", res.toString())
        val removePendingIntent = PendingIntent.getBroadcast(context, notificationID, removeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        //Create a Note
        val builder = NotificationCompat.Builder(context, "NOTE_CHANNEL")
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle(res.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .addAction(R.drawable.ic_noti, "Remind me", remindPendingIntent)
            .addAction(R.drawable.ic_noti, "Remove", removePendingIntent)
        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build());
        }

        //Update Original Notification
        val KEY_TEXT_CREATE = "allseeingeye";
        var creButString = "Enter title";
        var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_CREATE).run {
            setLabel(creButString)
            build()
        }
        //Remove default notification
        val removeIntentDefault = Intent(context, RemoveNote::class.java)
        val removePendingIntentDefault = PendingIntent.getBroadcast(context, 1, removeIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val creIntent = Intent(context, CreateNote::class.java)
        var crePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, creIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT);
        var creAction: NotificationCompat.Action = NotificationCompat.Action.Builder(R.drawable.ic_noti, "Create a new note", crePendingIntent).addRemoteInput(remoteInput).build()
        val updateOriginal = NotificationCompat.Builder(context, "NOTE_DEFAULT")
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle("Don't Forget is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setGroup("Default")
            .addAction(creAction)
            .addAction(R.drawable.ic_noti, "Stop", removePendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(1, updateOriginal.build());
        }

        //Add note data to save file
        val fileHanlder = FileHandler()
        val newEntry = fileHanlder.constructEntry(notificationID.toString(), res.toString(), null)
        val dir = File(context.filesDir, "data.dat")
        fileHanlder.appendEntry(dir, newEntry)


    }
}