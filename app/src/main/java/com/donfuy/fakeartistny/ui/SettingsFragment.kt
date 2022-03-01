package com.donfuy.fakeartistny.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.donfuy.fakeartistny.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@Suppress("unused")
private const val TAG = "SettingsFragment"


// Preferences DataStore Setup
const val PREFS_NAME = "prefs"
val ALL_FA = booleanPreferencesKey("all_fa")
val NO_FA = booleanPreferencesKey("no_fa")
val RANDOM_FA = booleanPreferencesKey("random_fa")
val START_FA = booleanPreferencesKey("start_fa")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSettings()
        binding.apply {

            buttonClose.setOnClickListener {
                runBlocking {
                    saveSettings()
                }
                findNavController().popBackStack()
            }

        }
    }

    /**
     * Loads settings from DataStore and updates the UI
     */
    private fun updateSettings() {

        binding.apply {
            requireContext().dataStore.data.apply {
                // All FAs checkbox
                map { prefs -> prefs[ALL_FA] ?: false }.asLiveData().observe(viewLifecycleOwner) {
                    checkboxAllFa.isChecked = it
                }

                // No FA checkbox
                map { prefs -> prefs[NO_FA] ?: false }.asLiveData().observe(viewLifecycleOwner) {
                    checkboxNoFa.isChecked = it
                }

                // Random FAs checkbox
                map { prefs -> prefs[RANDOM_FA] ?: false }.asLiveData()
                    .observe(viewLifecycleOwner) {
                        checkboxRandomFa.isChecked = it
                    }

                // Start FA checkbox
                map { prefs -> prefs[START_FA] ?: false }.asLiveData().observe(viewLifecycleOwner) {
                    checkboxStartFa.isChecked = it
                }
            }
        }

    }

    private suspend fun saveSettings() {
        binding.apply {
            requireContext().dataStore.edit { settings ->
                settings[ALL_FA] = checkboxAllFa.isChecked
                settings[NO_FA] = checkboxNoFa.isChecked
                settings[RANDOM_FA] = checkboxRandomFa.isChecked
                settings[START_FA] = checkboxStartFa.isChecked
            }
        }

    }

}