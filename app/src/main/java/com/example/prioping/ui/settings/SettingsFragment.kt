package com.example.prioping.ui.settings

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.fragment.app.viewModels
import com.example.prioping.R

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var toggleButton: ToggleButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        toggleButton = root.findViewById(R.id.service_toggle)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel: SettingsViewModel by viewModels {
            SettingsViewModel.SettingsViewModelFactory(
                requireContext()
            )
        }

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                viewModel.startService()
            } else {
                // The toggle is disabled
                viewModel.stopService()
            }
        }
    }
}

