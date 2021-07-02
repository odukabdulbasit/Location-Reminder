package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext

class FakeDataSource(var reminders: MutableList<ReminderDTO>? =
        mutableListOf()) :
    ReminderDataSource {

    /*override suspend fun getReminders(): Result<List<ReminderDTO>> =
            reminders?.let {
        Result.Success(ArrayList(it))
    } ?: Result.Error("No reminder found")*/

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //TODO("Return the reminders")
        if (shouldReturnError) {
            return Result.Error("No tasks")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Tasks not found")

    }


    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO>{
        if (shouldReturnError){
            return Result.Error("No tasks")
        }
        return reminders?.firstOrNull { it.id == id }?.let {
            Result.Success(it)
        } ?: Result.Error("Reminder $id not found")
    }
    /*=
        reminders?.firstOrNull { it.id == id }?.let {
            Result.Success(it)
        } ?: Result.Error("Reminder $id not found")*/

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}