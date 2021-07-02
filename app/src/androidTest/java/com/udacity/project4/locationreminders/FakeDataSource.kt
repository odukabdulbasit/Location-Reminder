package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {


    override suspend fun getReminders(): Result<List<ReminderDTO>> = reminders?.let {
        Result.Success(ArrayList(it))
    } ?: Result.Error("No reminder found")

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        reminders?.firstOrNull { it.id == id }?.let {
            Result.Success(it)
        } ?: Result.Error("Reminder $id not found")

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}