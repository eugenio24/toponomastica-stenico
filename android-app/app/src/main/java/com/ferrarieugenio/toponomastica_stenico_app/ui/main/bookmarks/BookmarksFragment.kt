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
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.FileExportUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarksViewModel by viewModels()

    private lateinit var adapter: BookmarkAdapter

    private var pendingExportData: ByteArray? = null

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
                pendingExportData = exportedData

                val safeName = FileExportUtils.getSuggestedFileName(fileName, format)
                createFileLauncher.launch(safeName)
            }
        )
        dialog.show(parentFragmentManager, "export_dialog")
    }

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null && pendingExportData != null) {
                val success = FileExportUtils.saveTextToUri(
                    context = requireContext(),
                    uri = uri,
                    content = pendingExportData!!
                )

                Toast.makeText(
                    requireContext(),
                    if (success)
                        "File salvato con successo"
                    else
                        "Errore nel salvataggio del file",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "Salvataggio annullato", Toast.LENGTH_SHORT).show()
            }

            pendingExportData = null
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}