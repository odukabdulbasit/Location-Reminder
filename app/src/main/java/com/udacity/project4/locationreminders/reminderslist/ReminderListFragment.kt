package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.FirebaseUserLiveData
import com.udacity.project4.authentication.LoginViewModel
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.reminderFragmentRefreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.remindersFragmentAddReminderFab.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        /*val adapter = RemindersListAdapter {
        }*/

        val adapter = RemindersListAdapter { reminder ->
            startActivity(
                    Intent(requireContext(), ReminderDescriptionActivity::class.java).apply {
                        putExtra(EXTRA_ReminderDataItem, reminder)
                    }
            )
        }
//        setup the recycler view using the extension function
        binding.reminderFragmentRemindersList.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                launchAuthenticationActivity()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    private fun launchAuthenticationActivity() {
        val i = Intent(activity, AuthenticationActivity::class.java)
        startActivity(i)
        requireActivity().finish()
    }

 /*   val State = FirebaseUserLiveData().map { user ->
        if (user != null) {
            LoginViewModel.AuthenticationState.AUTHENTICATED
        } else {
            LoginViewModel.AuthenticationState.UNAUTHENTICATED
        }

    }*/


}
private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"