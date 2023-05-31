package com.example.prioping.ui.logs

import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prioping.databinding.FragmentLogsBinding

class LogsFragment : Fragment() {

    private val viewModel: LogsViewModel by viewModels()
    private var _binding: FragmentLogsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter()

        binding.logsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LogsFragment.adapter
        }

        viewModel.notifications.observe(viewLifecycleOwner) { notifications: List<StatusBarNotification> ->
            adapter.notifications = notifications
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
