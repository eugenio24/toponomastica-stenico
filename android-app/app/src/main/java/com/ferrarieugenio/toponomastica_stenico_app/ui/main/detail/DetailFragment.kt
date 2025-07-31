package com.ferrarieugenio.toponomastica_stenico_app.ui.main.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentDetailBinding
import com.ferrarieugenio.toponomastica_stenico_app.ui.adapters.NeighborAdapter
import com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs.ExportDialogFragment
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ExportFormat
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by viewModels()

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toponym = args.toponym
        viewModel.setToponym(toponym)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener{ item ->
                when (item.itemId) {
                    R.id.action_share -> {
                        shareToponym()
                        true
                    }
                    else -> false
                }
            }
        }
        binding.toolbarTitleView.isSelected = true

        viewModel.toponym.observe(viewLifecycleOwner) { toponym ->
            binding.toolbarTitleView.text = toponym.nome
            binding.toolbarTitleView.setOnLongClickListener {
                Toast.makeText(requireContext(), toponym.nome, Toast.LENGTH_LONG).show()
                true
            }

            if (!toponym.forma_ufficiale.isNullOrBlank()) {
                binding.formaUfficialeTextView.text = toponym.forma_ufficiale
            } else {
                binding.formaUfficialeLabel.visibility = View.GONE
                binding.formaUfficialeTextView.visibility = View.GONE
            }

            binding.descrizioneTextView.text = toponym.descrizione

            binding.quotaTextView.text = "${toponym.quota} m"

            binding.latlonTextView.text = "Lat: ${toponym.lat}, Lon: ${toponym.lon}"

            if (toponym.tags.isNotEmpty()) {
                binding.tagsChipGroup.removeAllViews()
                toponym.tags.forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                        text = tag
                        isClickable = false
                        isCheckable = false
                    }
                    binding.tagsChipGroup.addView(chip)
                }
            } else {
                binding.tagsLabel.visibility = View.GONE
                binding.tagsChipGroup.visibility = View.GONE
            }

            binding.clusterTextView.text = toponym.cluster

            binding.hcClusterTextView.text = toponym.hc_cluster

            if (!toponym.varianti.isNullOrEmpty()) {
                val bulletList = toponym.varianti.joinToString("\n") { "• $it" }
                binding.variantiTextView.text = bulletList
            } else {
                binding.variantiLabel.visibility = View.GONE
                binding.variantiTextView.visibility = View.GONE
            }

            binding.viewOnMapButton.setOnClickListener {
                val action = DetailFragmentDirections.actionDetailFragmentToMapFragment(toponym)
                findNavController().navigate(action)
            }
        }

        binding.neighborsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.neighbors.observe(viewLifecycleOwner) { neighbors ->
            if (neighbors.isNotEmpty()) {
                binding.neighborsRecyclerView.apply {
                    adapter = NeighborAdapter(neighbors) { clickedNeighbor ->
                        val action = DetailFragmentDirections.actionDetailFragmentSelf(clickedNeighbor)
                        findNavController().navigate(action)
                    }
                    visibility = View.VISIBLE
                }
            } else {
                binding.neighborsLabel.visibility = View.GONE
                binding.neighborsRecyclerView.visibility = View.GONE
            }
        }

        viewModel.isBookmarked.observe(viewLifecycleOwner) { bookmarked ->
            if (bookmarked) {
                binding.bookmarkToggleButton.text = "Rimuovi dalla raccolta"
                binding.bookmarkToggleButton.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_bookmark_remove
                )
            } else {
                binding.bookmarkToggleButton.text = "Aggiungi alla raccolta"
                binding.bookmarkToggleButton.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_bookmark_add
                )
            }
        }

        binding.bookmarkToggleButton.setOnClickListener {
            viewModel.toggleBookmark()
        }
    }

    private fun shareToponym() {
        val toponym = viewModel.toponym.value ?: return
        val repository = viewModel.toponymRepository

        val dialog = ExportDialogFragment(
            toponym = toponym,
            toponymRepository = repository,
            onExported = { exportedData, format, fileName ->
                val defaultName = viewModel.toponym.value?.nome.orEmpty()
                    .trim()
                    .replace(" ", "_")
                val finalName = (fileName?.takeIf { it.isNotBlank() } ?: defaultName) + "." + format.fileExtension

                shareExportedFile(exportedData, format, finalName)
            }
        )

        dialog.show(parentFragmentManager, "exportDialog")
    }

    private fun shareExportedFile(
        data: ByteArray,
        format: ExportFormat,
        fileName: String
    ) {
        val context = requireContext()
        val file = File(context.cacheDir, fileName)

        file.outputStream().use { it.write(data) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Toponimo: $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Condividi toponimo con..."))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}