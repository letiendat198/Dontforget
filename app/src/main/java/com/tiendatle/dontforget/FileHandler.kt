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

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.Exception

class FileHandler {
    public fun appendEntry(file: File, text: String) {
        val writer = BufferedWriter(FileWriter(file, true))
        writer.append(text + System.getProperty("line.separator"))
        writer.flush()
        writer.close()
    }
    public fun deleteEntry(file:File, tempFile: File, id: String) {
        try {
            val reader = file.bufferedReader()
            val tempWriter = BufferedWriter(FileWriter(tempFile, true))
            for (line in reader.readLines()) {
                if (!line.startsWith(id)) {
                    Log.d("FILE_HANDLER_LOG", "Line safe: $line")
                    tempWriter.append(line + System.getProperty("line.separator"))
                    continue
                }
                Log.d("FILE_HANDLER_LOG", "Line deleted: $line")
            }
            tempWriter.close()
            reader.close()
            tempFile.renameTo(file)
        }
        catch (e: Exception) {
            println(e)
        }
    }
    public fun readEntries(file: File): List<String>? {
        try {
            val reader = file.bufferedReader()
            val lines = reader.readLines()
            reader.close()

            val sanitizedLines = mutableListOf<String>()
            for (line in lines) {
                if (line == "") {
                    continue
                }
                sanitizedLines.add(line)
            }
            if (sanitizedLines.isEmpty()) {
                return null
            }
            return sanitizedLines
        }
        catch (e: Exception) {
            return null
        }
    }
    public fun processEntry(entry: String): List<String> {
        val separated = entry.split(":")
        return separated
    }
    public fun constructEntry(id: String, content:String, remind :String?): String {
        if (remind != null) {
            val result = "$id:$content:$remind"
            return result
        }
        else {
            val result = "$id:$content"
            return result
        }
    }
    public fun updateEntry(file:File, tempFile: File, id: String, newEntry: String) {
        try {
            val reader = file.bufferedReader()
            val tempWriter = BufferedWriter(FileWriter(tempFile, true))
            for (line in reader.readLines()) {
                if (!line.startsWith(id)) {
                    Log.d("FILE_HANDLER_LOG", line)
                    tempWriter.append(line + System.getProperty("line.separator"))
                }
                else {
                    Log.d("FILE_HANDLER_LOG", "Matching id found: $line")
                    tempWriter.append(newEntry + System.getProperty("line.separator"))
                }
            }
            tempWriter.close()
            reader.close()
            tempFile.renameTo(file)
        }
        catch (e: Exception) {
            println(e)
        }
    }
}