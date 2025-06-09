package com.ferrarieugenio.toponomastica_stenico_app.ui.main.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentMapBinding
import com.ferrarieugenio.toponomastica_stenico_app.ui.components.ToponymMarker
import dagger.hilt.android.AndroidEntryPoint
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme.*
import java.io.File
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private val viewModel: MapViewModel by viewModels()

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var currentSelectedToponym: Toponym? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AndroidGraphicFactory.createInstance(requireContext().applicationContext)

        mapView = MapView(requireContext())
        (view as ViewGroup).addView(mapView, 0) // Add mapView as first child

        setupMap()
        observeToponyms()

        mapView.setOnTouchListener { _, _ ->
            if (binding.previewContainer.isVisible) {
                binding.previewContainer.visibility = View.GONE
            }
            false
        }

        binding.markerDismissButton.setOnClickListener {
            binding.previewContainer.visibility = View.GONE
            currentSelectedToponym = null
        }
    }

    private fun setupMap() {
        val mapFile = File(requireContext().filesDir, "stenico.map")
        if (!mapFile.exists()) {
            requireContext().assets.open("stenico.map").use { input ->
                mapFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val map = MapFile(mapFile)

        val tileCache = AndroidUtil.createTileCache(
            requireContext(),
            "mapcache",
            mapView.model.displayModel.tileSize,
            1f,
            mapView.model.frameBufferModel.overdrawFactor
        )

        val tileRendererLayer = TileRendererLayer(
            tileCache,
            map,
            mapView.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )
        tileRendererLayer.setXmlRenderTheme(DEFAULT)
        mapView.layerManager.layers.add(tileRendererLayer)

        // todo change to constant or parameter, parameter also for map file
        mapView.setCenter(LatLong(46.052168, 10.8540886))
        mapView.setZoomLevel(14)

        // todo add pan and zoom constraints to remain inside the downloaded map
    }

    private fun observeToponyms() {
        viewModel.toponyms.observe(viewLifecycleOwner) { toponyms ->
            addMarkers(toponyms)
        }
    }

    private fun addMarkers(toponyms: List<Toponym>) {
        val layers = mapView.layerManager.layers

        layers.filterIsInstance<Marker>().forEach { layers.remove(it) }

        toponyms.forEach { toponym ->
            val latLong = LatLong(toponym.lat, toponym.lon)
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker)

            if (drawable != null) {
                val bitmap = AndroidGraphicFactory.convertToBitmap(drawable)

                val marker = ToponymMarker(
                    latLong, bitmap, 0, -bitmap.height, toponym
                )

                marker.setOnTabAction(Runnable {
                    if (currentSelectedToponym != toponym) {
                        currentSelectedToponym = toponym
                        showMarkerInfo(toponym)
                    }
                })

                mapView.layerManager.layers.add(marker)
            }
        }

        mapView.invalidate()
        mapView.repaint()
    }

    private fun showMarkerInfo(toponym: Toponym) {
        binding.previewContainer.visibility = View.VISIBLE
        binding.markerNameText.text = toponym.nome

        binding.markerDetailsButton.setOnClickListener {
            val action = MapFragmentDirections.actionMapFragmentToDetailFragment(toponym)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.destroyAll()
        AndroidGraphicFactory.clearResourceMemoryCache()
        _binding = null
    }
}
