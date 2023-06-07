package com.example.prioping.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prioping.R
import com.example.prioping.databinding.FragmentLogsBinding

class LogsFragment : Fragment() {

    private val logsViewModel: LogsViewModel by viewModels()
    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

        private lateinit var clearLogsButton: Button


    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
                clearLogsButton = binding.root.findViewById(R.id.clear_logs_button)

        return binding.root
    }


override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(context)
    adapter = NotificationAdapter()
    recyclerView.adapter = adapter

    logsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
        adapter.submitList(notifications)
    }

        // Handle Clear Logs Button Click
        clearLogsButton.setOnClickListener {
            logsViewModel.clearLogs()
        }
}


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
