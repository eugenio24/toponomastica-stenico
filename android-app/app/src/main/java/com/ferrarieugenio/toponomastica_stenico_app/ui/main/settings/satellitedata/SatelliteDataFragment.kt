package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings.satellitedata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentSatelliteDataBinding
import com.ferrarieugenio.toponomastica_stenico_app.util.download.NotificationPermissionHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SatelliteDataFragment : Fragment() {

    private var _binding: FragmentSatelliteDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SatelliteDataViewModel by viewModels()

    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionHelper.handlePermissionResult(
            granted = granted,
            onGranted = { viewModel.startDownload() },
            onDenied = {
                Toast.makeText(requireContext(), "Notifiche negate, il download continua comunque.", Toast.LENGTH_SHORT).show()
                viewModel.startDownload()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSatelliteDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationPermissionHelper = NotificationPermissionHelper(requireContext(), requestNotificationPermissionLauncher)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnStartDownload.setOnClickListener {
            notificationPermissionHelper.checkAndRequestPermission(
                onPermissionGranted = {
                    viewModel.startDownload()
                }
            )
        }

        binding.btnCancelDownload.setOnClickListener {
            viewModel.cancelDownload()
        }

        binding.btnDeleteData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Conferma eliminazione")
                .setMessage("Sei sicuro di voler eliminare i dati scaricati?")
                .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Elimina") { _, _ -> viewModel.deleteData() }
                .show()
        }

        observeViewModel()
        viewModel.checkDataAvailability()
    }

    private fun observeViewModel() {
        viewModel.isDataAvailable.observe(viewLifecycleOwner) { available ->
            updateUI(
                dataAvailable = available,
                downloading = viewModel.isDownloading.value ?: false
            )
        }

        viewModel.isDownloading.observe(viewLifecycleOwner) { downloading ->
            updateUI(
                dataAvailable = viewModel.isDataAvailable.value ?: false,
                downloading = downloading
            )
        }

        viewModel.downloadProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgressPercent.text = "$progress%"
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.estimatedTimeRemaining.observe(viewLifecycleOwner) { timeEstimate ->
            if (!timeEstimate.isNullOrEmpty()) {
                binding.tvEstimatedTime.text = timeEstimate
                binding.tvEstimatedTime.isVisible = true
            } else {
                binding.tvEstimatedTime.text = ""
                binding.tvEstimatedTime.isVisible = false
            }
        }
    }

    private fun updateUI(dataAvailable: Boolean, downloading: Boolean) {
        when {
            downloading -> {
                binding.tvStatus.text = "Download in corso..."
                binding.progressLayout.isVisible = true
                binding.tvProgressPercent.isVisible = true
                binding.tvEstimatedTime.isVisible = true
                binding.btnStartDownload.isVisible = false
                binding.btnCancelDownload.isVisible = true
                binding.btnDeleteData.isVisible = false
            }

            dataAvailable -> {
                binding.tvStatus.text = "I dati satellitari sono scaricati e pronti all’uso."
                binding.progressLayout.isVisible = false
                binding.tvProgressPercent.isVisible = false
                binding.tvEstimatedTime.isVisible = false
                binding.btnStartDownload.isVisible = false
                binding.btnCancelDownload.isVisible = false
                binding.btnDeleteData.isVisible = true
            }

            else -> {
                binding.tvStatus.text = """
                Per utilizzare la modalità satellitare, è necessario scaricare circa 180 MB di dati aggiuntivi.
                Il download è richiesto solo una volta e permette l'uso offline della mappa satellitare.
            """.trimIndent()
                binding.progressLayout.isVisible = false
                binding.tvProgressPercent.isVisible = false
                binding.tvEstimatedTime.isVisible = false
                binding.btnStartDownload.isVisible = true
                binding.btnCancelDownload.isVisible = false
                binding.btnDeleteData.isVisible = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
