package com.ferrarieugenio.toponomastica_stenico_app.ui.main.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentBookmarksBinding
import com.ferrarieugenio.toponomastica_stenico_app.ui.adapters.BookmarkAdapter
import com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs.ExportDialogFragment
import com.ferrarieugenio.toponomastica_stenico_app.util.download.NotificationPermissionHelper
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ExportManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarksViewModel by viewModels()

    private lateinit var adapter: BookmarkAdapter

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
        _binding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportManager = ExportManager(requireContext(), NotificationPermissionHelper(requireContext(), requestPermissionLauncher))

        adapter = BookmarkAdapter(
            onToggleBookmark = { id ->
                Toast.makeText(requireContext(), "Elemento rimosso dalla raccolta", Toast.LENGTH_SHORT).show()
                viewModel.toggleBookmark(id)
            },
            onOpenDetails = { id ->
                viewModel.getToponymById(id)?.let {
                    val action = BookmarksFragmentDirections.actionBookmarksFragmentToDetailFragment(it)
                    findNavController().navigate(action)
                }
            }
        )

        binding.bookmarksRecyclerView.adapter = adapter
        binding.bookmarksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.exportButton.setOnClickListener {
            if (viewModel.hasBookmarkedToponyms()) {
                showExportDialog()
            } else {
                Toast.makeText(requireContext(), "Nessun toponimo da esportare", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.bookmarkedToponyms.observe(viewLifecycleOwner) { toponyms ->
            adapter.updateData(toponyms)

            if (toponyms.isEmpty()) {
                binding.emptyMessage.visibility = View.VISIBLE
                binding.bookmarksRecyclerView.visibility = View.GONE
            } else {
                binding.emptyMessage.visibility = View.GONE
                binding.bookmarksRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun showExportDialog() {
        val toponyms = viewModel.bookmarkedToponyms.value ?: emptyList()
        val repository = viewModel.toponymRepository

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