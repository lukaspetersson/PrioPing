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
import com.aallam.openai.client.OpenAI
import com.example.prioping.R
import android.app.AlertDialog
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.ListView


class TriggerFragment : Fragment() {

    private val viewModel: TriggerViewModel by viewModels {
        TriggerViewModel.TriggerViewModelFactory(
            requireContext()
        )
    }

    private lateinit var toggleButton: ToggleButton
    private lateinit var apiKeyField: EditText
    private lateinit var instructionField: EditText
    private lateinit var saveButton: Button
    private lateinit var selectAppsButton: Button

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
        selectAppsButton = root.findViewById(R.id.select_apps)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

        selectAppsButton.setOnClickListener {
            showAppSelectionDialog()
        }
    }
private fun showAppSelectionDialog() {
    val pm: PackageManager = requireContext().packageManager
    val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    val userApps = installedApps.filter {
        //it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0 &&
        //!it.packageName.contains("com.android") &&
        it.packageName != "com.example.prioping"
    }

    val appInfos = userApps.map { AppInfo(it.loadLabel(pm).toString(), it.packageName) }.sortedBy { it.appName }
    val checkedItems = appInfos.map { viewModel.selectedApps.contains(it.packageName) }.toMutableList()

    // Dialog's view
    val inflater = layoutInflater
    val dialogView = inflater.inflate(R.layout.dialog_searchable_list, null)
    val searchView = dialogView.findViewById<EditText>(R.id.search_view)
    val listView = dialogView.findViewById<ListView>(R.id.list_view)

    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, appInfos)
    listView.adapter = adapter
    listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

    // Initially check selected apps
    appInfos.forEachIndexed { index, appInfo ->
        listView.setItemChecked(index, viewModel.selectedApps.contains(appInfo.packageName))
    }

    // Filter logic
    searchView.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            adapter.filter.filter(s.toString())
        }
    })

    // OnItemClick Listener
    listView.setOnItemClickListener { _, _, position, _ ->
        val appInfo = listView.getItemAtPosition(position) as AppInfo
        if (viewModel.selectedApps.contains(appInfo.packageName)) {
            viewModel.removeApp(appInfo.packageName)
        } else {
            viewModel.addApp(appInfo.packageName)
        }
    }

    // Show dialog
    AlertDialog.Builder(context)
        .setTitle("Select Apps")
        .setView(dialogView)
        .setPositiveButton("OK", null)
        .show()
}

data class AppInfo(val appName: String, val packageName: String) {
    override fun toString(): String {
        return appName
    }
}


}