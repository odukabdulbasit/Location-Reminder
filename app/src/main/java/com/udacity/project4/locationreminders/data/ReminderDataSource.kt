package com.udacity.project4.locationreminders.data

import androidx.lifecycle.LiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun getReminder(id: String): Result<ReminderDTO>
    suspend fun deleteAllReminders()
    suspend fun refreshReminders()
    suspend fun refreshReminder(id: String)
    suspend fun observeReminders(): LiveData<Result<List<ReminderDTO>>>
    suspend fun observeTask(reminderId: String): LiveData<Result<ReminderDTO>>
}