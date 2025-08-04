package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings.mappreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentMapPreferencesBinding
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapConfig
import com.ferrarieugenio.toponomastica_stenico_app.util.map.SatelliteDataChecker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapPreferencesFragment : Fragment() {

    private var _binding: FragmentMapPreferencesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapPreferencesViewModel by viewModels()

    private val seekMax = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.zoomSeekBar.max = seekMax

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.contentLayout.isVisible = state.isLoaded
                    binding.loadingProgressBar.isVisible = !state.isLoaded

                    if (!state.isLoaded) return@collectLatest

                    updateUI(state)
                }
            }
        }

        binding.mapStyleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.osmRadioButton.id -> viewModel.setMapStyle("OSM")
                binding.satelliteRadioButton.id -> {
                    if (SatelliteDataChecker.isSatelliteDataAvailable(requireContext())) {
                        viewModel.setMapStyle("SATELLITE")
                    } else {
                        Snackbar.make(
                            binding.root,
                            "I dati satellitari non sono disponibili. Puoi scaricarli dalle impostazioni.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.mapStyleRadioGroup.check(binding.osmRadioButton.id)
                    }
                }
            }
        }

        binding.contourSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowContours(isChecked)
        }
        binding.municipalitySwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowMunicipalities(isChecked)
        }

        binding.zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val threshold = progressToThreshold(progress)
                    viewModel.setZoomLevelForNamedMarkers(threshold)

                    binding.zoomValueText.text = when {
                        threshold <= MapConfig.MIN_ZOOM_BOUND + 0.1 -> "Mostra sempre"
                        threshold >= MapConfig.NEVER_SHOW_NAMED_MARKER_THRESHOLD - 0.1 -> "Non mostrare mai"
                        else -> "Mostra da zoom: %.1f".format(threshold)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun progressToThreshold(progress: Int): Double {
        return MapConfig.MIN_ZOOM_BOUND +
                (progress / seekMax.toDouble()) *
                (MapConfig.NEVER_SHOW_NAMED_MARKER_THRESHOLD - MapConfig.MIN_ZOOM_BOUND)
    }

    private fun thresholdToProgress(threshold: Double): Int {
        return (((threshold - MapConfig.MIN_ZOOM_BOUND) /
                (MapConfig.NEVER_SHOW_NAMED_MARKER_THRESHOLD - MapConfig.MIN_ZOOM_BOUND)) * seekMax).toInt()
    }

    private fun updateUI(state: MapPreferencesUiState) {
        binding.mapStyleRadioGroup.check(
            when (state.mapStyle.name) {
                "OSM" -> binding.osmRadioButton.id
                "SATELLITE" -> binding.satelliteRadioButton.id
                else -> binding.osmRadioButton.id
            }
        )
        binding.contourSwitch.isChecked = state.mapStyle.showContours
        binding.municipalitySwitch.isChecked = state.mapStyle.showMunicipalities

        val progress = thresholdToProgress(state.zoomLevel).coerceIn(0, seekMax)
        binding.zoomSeekBar.progress = progress
        binding.zoomValueText.text = when {
            state.zoomLevel <= MapConfig.MIN_ZOOM_BOUND + 0.1 -> "Mostra sempre"
            state.zoomLevel >= MapConfig.NEVER_SHOW_NAMED_MARKER_THRESHOLD - 0.1 -> "Non mostrare mai"
            else -> "Mostra da zoom: %.1f".format(state.zoomLevel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

