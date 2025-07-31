package com.ferrarieugenio.toponomastica_stenico_app.ui.main.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentSearchBinding
import com.ferrarieugenio.toponomastica_stenico_app.ui.adapters.ToponymAdapter
import com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs.AdvancedSearchDialogFragment
import com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs.ExportDialogFragment
import com.ferrarieugenio.toponomastica_stenico_app.util.download.NotificationPermissionHelper
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ExportManager
import com.ferrarieugenio.toponomastica_stenico_app.util.filters.AdvancedFilters
import com.ferrarieugenio.toponomastica_stenico_app.util.filters.SortDirection
import com.ferrarieugenio.toponomastica_stenico_app.util.filters.SortField
import com.ferrarieugenio.toponomastica_stenico_app.util.filters.SortOption
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ToponymAdapter

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var searchJob: Job? = null

    private lateinit var exportManager: ExportManager

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            exportManager.notificationPermissionHelper.handlePermissionResult(
                granted = isGranted,
                onGranted = { },
                onDenied = { }
            )
        }

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            exportManager.handleCreateDocumentResult(
                uri,
                createSuccessCallback = {
                    Toast.makeText(requireContext(), "File salvato con successo", Toast.LENGTH_SHORT).show()
                },
                createFailCallback = {
                    Toast.makeText(requireContext(), "Errore nel salvataggio del file o salvataggio annullato", Toast.LENGTH_SHORT).show()
                }
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportManager = ExportManager(requireContext(), NotificationPermissionHelper(requireContext(), requestPermissionLauncher))

        adapter = ToponymAdapter(emptyList()) { clickedToponym ->
            val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(clickedToponym)
            findNavController().navigate(action)
        }
        binding.toponymRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.toponymRecyclerView.adapter = adapter

        viewModel.filteredToponyms.observe(viewLifecycleOwner) { toponyms ->
            adapter.updateList(toponyms)
            viewModel.currentQuery.value?.let { adapter.setQuery(it) }

            binding.itemCountTextView.text = "${toponyms.size} risultati"
            binding.toponymRecyclerView.scrollToPosition(0)

            binding.noResultsTextView.isVisible = toponyms.isEmpty()
            binding.toponymRecyclerView.isVisible = toponyms.isNotEmpty()
        }

        binding.filtersButton.setOnClickListener {
            val dialog = AdvancedSearchDialogFragment(
                initialFilters = viewModel.currentFilters.value ?: AdvancedFilters(),
                onApplyFilters = { filters ->
                    viewModel.filter(filters = filters)
                },
                availableClusters = viewModel.availableClusters,
                availableHcClusters = viewModel.availableHcClusters,
                availableTags = viewModel.availableTags
            )
            dialog.show(parentFragmentManager, "AdvancedSearchDialog")
        }

        binding.sortButton.setOnClickListener {
            showSortDialog()
        }

        binding.exportButton.setOnClickListener {
            if (viewModel.filteredToponyms.value?.isNotEmpty() == true) {
                showExportDialog()
            } else {
                Toast.makeText(requireContext(), "Nessun toponimo da esportare", Toast.LENGTH_SHORT).show()
            }
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(200) // debounce delay 200 ms
                    viewModel.filter(query = query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* No-op */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* No-op */ }
        })

        viewModel.currentFilters.observe(viewLifecycleOwner) { filters ->
            updateFiltersBadge(filters)
        }

        viewModel.currentSortOption.observe(viewLifecycleOwner) { option ->
            adapter.setSortOption(option)
            updateSortButtonLabel(option)
        }
    }

    private fun showSortDialog() {
        val sortOptions = listOf(
            "Nome (A-Z)",
            "Nome (Z-A)",
            "Quota (crescente)",
            "Quota (decrescente)"
        )

        val currentSort = viewModel.currentSortOption.value ?: SortOption()
        val currentIndex = when (currentSort) {
            SortOption(SortField.NAME, SortDirection.ASCENDING) -> 0
            SortOption(SortField.NAME, SortDirection.DESCENDING) -> 1
            SortOption(SortField.QUOTA, SortDirection.ASCENDING) -> 2
            SortOption(SortField.QUOTA, SortDirection.DESCENDING) -> 3
            else -> 0
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ordina per")
            .setSingleChoiceItems(sortOptions.toTypedArray(), currentIndex) { dialog, which ->
                val newSortOption = when (which) {
                    0 -> SortOption(SortField.NAME, SortDirection.ASCENDING)
                    1 -> SortOption(SortField.NAME, SortDirection.DESCENDING)
                    2 -> SortOption(SortField.QUOTA, SortDirection.ASCENDING)
                    3 -> SortOption(SortField.QUOTA, SortDirection.DESCENDING)
                    else -> SortOption()
                }

                viewModel.setSortOption(newSortOption)

                dialog.dismiss()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun updateSortButtonLabel(option: SortOption) {
        val label = when (option) {
            SortOption(SortField.NAME, SortDirection.ASCENDING) -> "Nome (A-Z)"
            SortOption(SortField.NAME, SortDirection.DESCENDING) -> "Nome (Z-A)"
            SortOption(SortField.QUOTA, SortDirection.ASCENDING) -> "Quota ↑"
            SortOption(SortField.QUOTA, SortDirection.DESCENDING) -> "Quota ↓"
            else -> "Ordina"
        }
        binding.sortButton.text = label
    }

    private fun updateFiltersBadge(filters: AdvancedFilters) {
        val count = filters.countActiveFilters()
        binding.filtersButton.setBadgeNumber(count)
    }

    private fun showExportDialog() {
        val toponyms = viewModel.filteredToponyms.value ?: emptyList()
        val repository = viewModel.repository

        val dialog = ExportDialogFragment(
            toponyms = toponyms,
            toponymRepository = repository,
            onExported = { exportedData, format, fileName ->
                exportManager.prepareExport(exportedData, format, fileName)
                exportManager.launchCreateDocument(createFileLauncher)
            }
        )
        dialog.show(parentFragmentManager, "export_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}