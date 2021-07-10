package com.udacity.project4.locationreminders

import androidx.lifecycle.LiveData
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

    override suspend fun refreshReminders() {
        TODO("Not yet implemented")
    }

    override suspend fun refreshReminder(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun observeReminders(): LiveData<Result<List<ReminderDTO>>> {
        TODO("Not yet implemented")
    }

    override suspend fun observeTask(reminderId: String): LiveData<Result<ReminderDTO>> {
        TODO("Not yet implemented")
    }
}