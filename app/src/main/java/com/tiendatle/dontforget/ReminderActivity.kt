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
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker

class ReminderActivity : Activity() {
    private var notificationID: Int = 0
    private var Content = ""
    private var origin = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        val intent = getIntent();
        this.notificationID = intent.getIntExtra("ID", 1)
        this.Content = intent.getStringExtra("Content").toString()
        this.origin = intent.getStringExtra("Origin").toString()
        Log.d("LOG_DATEPICK", notificationID.toString())
    }
    public fun onClick(view: android.view.View) {
        Log.d("LOG_DATEPICK", this.notificationID.toString())

        val datePicker = findViewById<DatePicker>(R.id.datepick)
        val year = datePicker.year
        val month = datePicker.month
        val date = datePicker.dayOfMonth
        Log.d("LOG_DATEPICK", date.toString())

        val intent = Intent(this, TimePickActivity::class.java)
        intent.putExtra("ID", notificationID)
        intent.putExtra("Content", Content)
        intent.putExtra("Origin", origin)
        intent.putExtra("Year", year)
        intent.putExtra("Month", month)
        intent.putExtra("Date", date)
        startActivity(intent)
        this.finish()
    }
}