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
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import java.io.File

class RemoveNote: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationID = intent.getIntExtra("ID", 1)
        val content = intent.getStringExtra("Content").toString()
        val isOnlyRemoveNoti = intent.getBooleanExtra("isOnlyRemoveNotification", true)
        Log.d("LOG_REMOVE", notificationID.toString())

        with(NotificationManagerCompat.from(context)) {
            cancel(notificationID)
        }

        if (!isOnlyRemoveNoti){
            Log.d("LOG_REMOVE", "Removing entry in save")
            val fileHandler = FileHandler()
            val dir = File(context.filesDir, "data.dat")
            val tempFile = File(context.filesDir, "temp.dat")
            fileHandler.deleteEntry(dir, tempFile, notificationID.toString())

            //Cancel Alarm (Won't work if the reminder's title have been edited prior to its removal - Please cancel in Edit Note screen before removing)
            val intent = Intent(context, RefreshNote::class.java)
            intent.putExtra("ID", notificationID)
            intent.putExtra("Content", content)
            intent.putExtra("isSounded", true)
            val pendingIntent = PendingIntent.getBroadcast(context, notificationID, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            var alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }

    }
}