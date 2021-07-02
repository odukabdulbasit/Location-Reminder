package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Rule
    @JvmField
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant("android.permission.ACCESS_FINE_LOCATION")

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(appContext, get() as ReminderDataSource)
            }
            single {
                SaveReminderViewModel(appContext, get() as ReminderDataSource)
            }

            single { RemindersLocalRepository(get())  }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            modules(listOf(myModule))
        }
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    //An Idling Resource that waits for Data Binding to Have no pending bindings.
    private val databindingIdlingResource = DataBindingIdlingResource()

    /** Idling resource tells Espresso that the App is Idle or Busy. This is needed when operations are
     * not scheduled in the Main Looper (ex when executed in a different Thread)
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(databindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(databindingIdlingResource)
    }


    @Test
    fun addLocation_saveReminder_check() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(withId(R.id.reminder_fragment_no_reminder))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(withId(R.id.reminders_fragment_add_reminder_fab)).perform(ViewActions.click())

        onView(withId(R.id.fragment_save_reminder_title))
            .perform(ViewActions.replaceText("Remember to buy eggs and milk"))

        onView(withId(R.id.fragment_save_reminder_description))
            .perform(ViewActions.replaceText("Buy eggs and milk from general store"))

        onView(withId(R.id.fragment_save_reminder_select_location)).perform(ViewActions.click())

        onView(withId(R.id.select_location_fragment_map_view)).perform(ViewActions.longClick())

        pressBack()

        repository.saveReminder(
            ReminderDTO(
                "Remember to buy eggs and milk",
                "Buy eggs and milk from general store",
                "Mega city",
                0.0,
                0.0
            )
        )
        onView(ViewMatchers.withText("Remember to buy eggs and milk"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("Buy eggs and milk from general store"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        onView(ViewMatchers.withText(R.string.select_location))
                .inRoot(withDecorView(not(getActivity(appContext)
                        ?.getWindow()?.getDecorView())))
        .check(matches(isDisplayed()))

        onView(ViewMatchers.withText(R.string.select_poi))
                .inRoot(withDecorView(not(getActivity(appContext)
                        ?.getWindow()?.getDecorView())))
                .check(matches(isDisplayed()))
        activityScenario.close()
    }
}
