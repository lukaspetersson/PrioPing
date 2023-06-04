package com.example.prioping.ui.action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.prioping.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ActionFragment : Fragment() {

    private lateinit var viewModel: ActionViewModel
    private lateinit var emailFieldLayout: TextInputLayout
    private lateinit var emailField: TextInputEditText
    private lateinit var emailFlaggedCheckbox: CheckBox
    private lateinit var emailUnflaggedCheckbox: CheckBox
    private lateinit var filterFlaggedCheckbox: CheckBox
    private lateinit var filterUnflaggedCheckbox: CheckBox
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_action, container, false)
        emailFieldLayout = root.findViewById(R.id.email_field_layout)
        emailField = root.findViewById(R.id.email_field)
        emailFlaggedCheckbox = root.findViewById(R.id.email_flagged_checkbox)
        emailUnflaggedCheckbox = root.findViewById(R.id.email_unflagged_checkbox)
        filterFlaggedCheckbox = root.findViewById(R.id.filter_flagged_checkbox)
        filterUnflaggedCheckbox = root.findViewById(R.id.filter_unflagged_checkbox)
        saveButton = root.findViewById(R.id.save_button)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val viewModel: ActionViewModel by viewModels {
        ActionViewModel.ActionViewModelFactory(
            requireContext()
        )
    }

    // Set the states of the checkboxes and the email field according to the saved preferences
    emailField.setText(viewModel.email)
    emailFlaggedCheckbox.isChecked = viewModel.emailFlagged
    emailUnflaggedCheckbox.isChecked = viewModel.emailUnflagged
    filterFlaggedCheckbox.isChecked = viewModel.filterFlagged
    filterUnflaggedCheckbox.isChecked = viewModel.filterUnflagged

    // Manage visibility of email field initially
    manageEmailFieldVisibility()

    emailFlaggedCheckbox.setOnCheckedChangeListener { _, _ ->
        manageEmailFieldVisibility()
    }

    emailUnflaggedCheckbox.setOnCheckedChangeListener { _, _ ->
        manageEmailFieldVisibility()
    }

    saveButton.setOnClickListener {
        viewModel.email = emailField.text.toString()
        viewModel.emailFlagged = emailFlaggedCheckbox.isChecked
        viewModel.emailUnflagged = emailUnflaggedCheckbox.isChecked
        viewModel.filterFlagged = filterFlaggedCheckbox.isChecked
        viewModel.filterUnflagged = filterUnflaggedCheckbox.isChecked
    }
}


    private fun manageEmailFieldVisibility() {
        if (emailFlaggedCheckbox.isChecked || emailUnflaggedCheckbox.isChecked) {
            emailFieldLayout.visibility = View.VISIBLE
        } else {
            emailFieldLayout.visibility = View.GONE
        }
    }
}
