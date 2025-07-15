package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentSatelliteDataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class SatelliteDataFragment : Fragment() {

    private var _binding: FragmentSatelliteDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SatelliteDataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSatelliteDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartDownload.setOnClickListener {
            viewModel.startDownload()
        }

        binding.btnDeleteData.setOnClickListener {
            viewModel.deleteData()
        }

        viewModel.isDataAvailable.observe(viewLifecycleOwner) { available ->
            if (available) {
                binding.tvStatus.text = "I dati satellitari sono già scaricati e pronti all’uso."
                binding.btnStartDownload.isEnabled = false
                binding.btnDeleteData.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.tvStatus.text = """
                    Per utilizzare la modalità satellitare, è necessario scaricare circa 200 MB di dati aggiuntivi.
                    Questo download è richiesto solo una volta e ti permetterà di usare la mappa satellitare anche offline.
                """.trimIndent()
                binding.btnStartDownload.isEnabled = true
                binding.btnDeleteData.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.isDownloading.observe(viewLifecycleOwner) { downloading ->
            binding.progressBar.visibility = if (downloading) View.VISIBLE else View.GONE
            binding.btnStartDownload.isEnabled = !downloading
            binding.btnDeleteData.isEnabled = !downloading
        }

        viewModel.downloadProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.tvStatus.text = "Download in corso... $progress%"
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Check data availability at start
        viewModel.checkDataAvailability()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
