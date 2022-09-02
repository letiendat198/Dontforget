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
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class RefreshNote: BroadcastReceiver() {
    public override fun onReceive(context: Context, intent: Intent) {
        val notificationID = intent.getIntExtra("ID", 1)
        val Content = intent.getStringExtra("Content")
        val isSounded = intent.getBooleanExtra("isSounded", true)
        Log.d("LOG_REFRESH", notificationID.toString())
        Log.d("LOG_REFRESH", Content.toString())

        val remindIntent = Intent(context, ReminderActivity::class.java)
        remindIntent.putExtra("ID", notificationID)
        remindIntent.putExtra("Content", Content.toString())
        remindIntent.putExtra("Origin", "refresh_note")
        val remindPendingIntent = PendingIntent.getActivity(context, notificationID, remindIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val removeIntent = Intent(context, RemoveNote::class.java)
        removeIntent.putExtra("ID", notificationID)
        val removePendingIntent = PendingIntent.getBroadcast(context, notificationID, removeIntent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        var channelID = "NOTE_REMINDER"
        if (isSounded == false){
            channelID = "NOTE_CHANNEL"
        }

        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle(Content.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setGroup("Note")
            .addAction(R.drawable.ic_noti, "Remind me", remindPendingIntent)
            .addAction(R.drawable.ic_noti, "Remove", removePendingIntent)
        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, builder.build());
        }
    }
}