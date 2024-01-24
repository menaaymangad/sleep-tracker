/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application

import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {
        // Declare Job() and cancel jobs in onCleared().
        /**
         * viewModelJob allows us to cancel all coroutines started by this ViewModel.
         */
        private var viewModelJob = Job()

        // Define uiScope for coroutines.
        /**
         * A [CoroutineScope] keeps track of all coroutines started by this ViewModel.
         *
         * Because we pass it [viewModelJob], any coroutine started in this uiScope can be cancelled
         * by calling `viewModelJob.cancel()`
         *
         * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
         * the main thread on Android. This is a sensible default because most coroutines started by
         * a [ViewModel] update the UI after performing some processing.
         */
//        private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

        // Create a MutableLiveData variable tonight for one SleepNight.
        private var tonight = MutableLiveData<SleepNight?>()

        //Define a variable, nights. Then getAllNights() from the database
        //and assign to the nights variable.
        val nights = database.getAllNights()

        // In an init block, initializeTonight(), and implement it to launch a coroutine
        //to getTonightFromDatabase().
        /**
         * Converted nights to Spanned for displaying.
         */
//        val nightsString = Transformations.map(nights) { nights ->
//                formatNights(nights, application.resources)
//        }
//  Create three corresponding state variables. Assign them a Transformations
        //that tests it against the value of tonight.
        /**
         * If tonight has not been set, then the START button should be visible.
         */
        val startButtonVisible = Transformations.map(tonight) {
                null == it
        }

        // Verify app build and runs without errors.
        /**
         * If tonight has been set, then the STOP button should be visible.
         */
        val stopButtonVisible = Transformations.map(tonight) {
                null != it
        }

        // Using the familiar pattern, create encapsulated showSnackBarEvent variable
        //and doneShowingSnack bar() function.
        /**
         * If there are any nights in the database, show the CLEAR button.
         */
        val clearButtonVisible = Transformations.map(nights) {
                it?.isNotEmpty()
        }

        /**
         * Request a toast by setting this value to true.
         *
         * This is private because we don't want to expose setting this value to the Fragment.
         */
        private var _showSnackbarEvent = MutableLiveData<Boolean>()

        // In onClear(), set the value of _showOnSnackbarEvent to true.
        /**
         * If this is true, immediately `show()` a toast and call `doneShowingSnackbar()`.
         */
        val showSnackBarEvent: LiveData<Boolean>
                get() = _showSnackbarEvent
        /**
         * Call this immediately after calling `show()` on a toast.
         *
         * It will clear the toast request, so if the user rotates their phone it won't show a duplicate
         * toast.
         */

        fun doneShowingSnackbar() {
                _showSnackbarEvent.value = false
        }
        // Implement getTonightFromDatabase()as a suspend function.
        init {
                initializeTonight()
        }
        //create encapsulated LiveData navigateToSleepQuality and doneNavigating() function.
        //Use them in onStopTracking() to trigger navigation.
        /**
         * Variable that tells the Fragment to navigate to a specific "SleepQualityFragment"
         *
         * This is private because we don't want to expose setting this value to the Fragment.
         */
        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

        /**
         * If this is non-null, immediately navigate to "SleepQualityFragment" and call [doneNavigating]
         */
        val navigateToSleepQuality: LiveData<SleepNight>
                get() = _navigateToSleepQuality

        /**
         * Call this immediately after navigating to "SleepQualityFragment"
         *
         * It will clear the navigation request, so if the user rotates their phone it won't navigate
         * twice.
         */
        fun doneNavigating() {
                _navigateToSleepQuality.value = null
        }

        // Implement the click handler for the Start button, onStartTracking(), using
        //coroutines. Define the suspend function insert(), to insert a new night into the database.
        private fun initializeTonight() {
                viewModelScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        // Create onStopTracking() for the Stop button with an update() suspend function.
        /**
         *  Handling the case of the stopped app or forgotten recording,
         *  the start and end times will be the same.j
         *
         *  If the start time and end time are not the same, then we do not have an unfinished
         *  recording.
         */
        private suspend fun getTonightFromDatabase(): SleepNight? {
                return withContext(Dispatchers.IO){
                        var night = database.getTonight()

                        if (night?.endTimeMilli != night?.startTimeMilli) {
                                night = null
                        }
                        night
                }
        }

        // For the Clear button, created onClear() with a clear() suspend function.
         private suspend fun clear() {
               database.clear()
        }

        // Transform nights into a nightsString using formatNights().
        private suspend fun update(night: SleepNight) {

                database.update(night)
        }

        private suspend fun insert(night: SleepNight) {
                database.insert(night)
        }

        /**
         * Executes when the START button is clicked.
         */
        fun onStartTracking() {
                viewModelScope.launch {
                        // Create a new night, which captures the current time,
                        // and insert it into the database.
                        val newNight = SleepNight()

                        insert(newNight)

                        tonight.value = getTonightFromDatabase()
                }
        }

        /**
         * Executes when the STOP button is clicked.
         */
        fun onStopTracking() {
                viewModelScope.launch {
                        // In Kotlin, the return@label syntax is used for specifying which function among
                        // several nested ones this statement returns from.
                        // In this case, we are specifying to return from launch(),
                        // not the lambda.
                        val oldNight = tonight.value ?: return@launch

                        // Update the night in the database to add the end time.
                        oldNight.endTimeMilli = System.currentTimeMillis()

                        update(oldNight)
                        // Set state to navigate to the SleepQualityFragment.
                        _navigateToSleepQuality.value = oldNight
                }
        }

        /**
         * Executes when the CLEAR button is clicked.
         */
        fun onClear() {
                viewModelScope.launch {
                        // Clear the database table.
                        clear()

                        // And clear tonight since it's no longer in the database
                        tonight.value = null
                        // Show a snackbar message, because it's friendly.
                        _showSnackbarEvent.value = true
                }
        }

        /**
         * Called when the ViewModel is dismantled.
         * At this point, we want to cancel all coroutines;
         * otherwise we end up with processes that have nowhere to return to
         * using memory and resources.
         */
//        override fun onCleared() {
//                super.onCleared()
//                viewModelJob.cancel()
//        }


}

