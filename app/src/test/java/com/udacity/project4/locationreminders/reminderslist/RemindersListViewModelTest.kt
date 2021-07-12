package com.udacity.project4.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private val reminder1 = ReminderDTO("testtitle1", "testdescription1", "testlocation1", 18.3, 23.3)
    private val reminder2 = ReminderDTO("testtitle2", "testdescription2", "testlocation2", 18.3, 23.3)

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var remindersListViewModel: RemindersListViewModel


    val reminderList = mutableListOf<ReminderDTO>()
    private lateinit var remindersLocalDataSource: FakeDataSource

    @Before
    fun createDataSource() = runBlockingTest {
        remindersLocalDataSource = FakeDataSource(reminderList)

    }

    @Test
    fun getReminders() = runBlockingTest {

        //GIVEN: 2 reminders
        remindersLocalDataSource.saveReminder(reminder1)
        remindersLocalDataSource.saveReminder(reminder2)

        // WHEN - Get the reminders
        val loadedlist = remindersLocalDataSource.getReminders()

        // THEN - The reminders count is 2
        assertThat((loadedlist as Result.Success).data.size, CoreMatchers.`is`(2))

    }

    @Test
    fun ErrorInGetReminders() = runBlockingTest {

        //GIVEN: Error in reminders
        remindersLocalDataSource.setShouldReturnError(true)
        remindersLocalDataSource.saveReminder(reminder1)
        remindersLocalDataSource.saveReminder(reminder2)

        // WHEN - Get the reminders
        val loadedlist = remindersLocalDataSource.getReminders()

        //refesh function from fakedatasource
        remindersListViewModel.refresh()
        MatcherAssert.assertThat(remindersListViewModel.empty.getOrAwaitValue(), `is`(true))
        MatcherAssert.assertThat(remindersListViewModel.error.getOrAwaitValue(), `is`(false))
        // THEN - Error is returned
        //assertThat((loadedlist as Result.Error), CoreMatchers.`is`(Result.Error("No tasks")))
        //live Data testing
        Assert.assertEquals(remindersLocalDataSource.reminders, loadedlist )

    }
    @After
    fun cleanupDataSource() = runBlockingTest {
        stopKoin()
        remindersLocalDataSource.deleteAllReminders()

    }



    @Before
    fun setUp() {
        //stopKoin()

        remindersLocalDataSource = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                remindersLocalDataSource
            )

    }



    @Test
    fun check_loading_status() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        var loading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(loading, `is`(true) )

        mainCoroutineRule.resumeDispatcher()

        loading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(loading, `is`(false))
    }

}