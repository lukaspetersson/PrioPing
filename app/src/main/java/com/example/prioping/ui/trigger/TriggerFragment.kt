package com.example.prioping.ui.trigger

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ToggleButton
import androidx.fragment.app.viewModels
import com.example.prioping.R

class TriggerFragment : Fragment() {

    private lateinit var viewModel: TriggerViewModel
    private lateinit var toggleButton: ToggleButton
    private lateinit var apiKeyField: EditText
    private lateinit var instructionField: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_trigger, container, false)
        toggleButton = root.findViewById(R.id.service_toggle)
        apiKeyField = root.findViewById(R.id.api_key_field)
        instructionField = root.findViewById(R.id.instruction_field)
        saveButton = root.findViewById(R.id.save_button)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel: TriggerViewModel by viewModels {
            TriggerViewModel.TriggerViewModelFactory(
                requireContext()
            )
        }

        // Set the initial text of the EditText fields according to saved values
        apiKeyField.setText(viewModel.apiKey)
        instructionField.setText(viewModel.instruction)
        toggleButton.isChecked = viewModel.isServiceActive

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                viewModel.startService()
                viewModel.isServiceActive = true
            } else {
                // The toggle is disabled
                viewModel.stopService()
                viewModel.isServiceActive = false
            }
        }


        saveButton.setOnClickListener {
            viewModel.apiKey = apiKeyField.text.toString()
            viewModel.instruction = instructionField.text.toString()
        }
    }
}