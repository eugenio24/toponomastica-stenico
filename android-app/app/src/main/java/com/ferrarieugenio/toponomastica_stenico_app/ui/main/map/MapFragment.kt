package com.ferrarieugenio.toponomastica_stenico_app.ui.main.map

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentMapBinding
import com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs.MapStyleSelectorDialog
import com.ferrarieugenio.toponomastica_stenico_app.util.location.LocationHelper
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapConfig
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapMarkerManager
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapStyle
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapStyleManager
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MarkerIconCache
import com.ferrarieugenio.toponomastica_stenico_app.util.ui.SwipeGestureListener
import com.ferrarieugenio.toponomastica_stenico_app.util.ui.ViewAnimatorUtils
import com.ferrarieugenio.toponomastica_stenico_app.util.ui.createDetailShimmerPlaceholder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var mapLibreMap: MapLibreMap

    private var onMapReady: (() -> Unit)? = null
    private var isMapFullyReady = false

    private lateinit var markerManager: MapMarkerManager
    private val mapStyleManager: MapStyleManager by lazy {
        MapStyleManager(requireContext())
    }
    private lateinit var currentMapStyle: MapStyle
    @Inject lateinit var markerIconCache: MarkerIconCache

    private lateinit var locationHelper: LocationHelper
    private var isLocationActive = false

    private val viewModel: MapViewModel by activityViewModels()

    private var pendingZoom = false

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val frameRenderListener = object : MapView.OnDidFinishRenderingFrameListener {
        override fun onDidFinishRenderingFrame(
            fully: Boolean,
            frameEncodingTime: Double,
            frameRenderingTime: Double
        ) {
            if (fully) {
                binding.progressOverlay.visibility = View.GONE
                binding.mapView.removeOnDidFinishRenderingFrameListener(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentMapStyle = viewModel.getSavedMapStyle()

        mapView = binding.mapView
        setupMap()

        isLocationActive = savedInstanceState?.getBoolean("locationActive") ?: false

        onMapReady = {
            observeToponyms()
            observeSelectedToponym()
            setupUI()

            val toponym = consumeParcelableArgOnce<Toponym>("toponym")
            if (toponym != null) {
                pendingZoom = true
                viewModel.selectToponymById(toponym.id)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI() {
        binding.markerDismissButton.setOnClickListener {
            viewModel.clearSelectedToponym()
        }

        mapLibreMap.addOnMapClickListener {
            viewModel.clearSelectedToponym()
            true
        }

        binding.markerDetailsButton.setOnClickListener {
            viewModel.selectedToponym.value?.let { navigateToDetail(it) }
        }

        binding.previewContainer.setOnTouchListener(
            SwipeGestureListener(
                context = requireContext(),
                onSwipeUp = {
                    viewModel.selectedToponym.value?.let { navigateToDetail(it) }
                },
                onSwipeDown = {
                    viewModel.clearSelectedToponym()
                }
            )
        )

        binding.fabMyLocation.setOnClickListener {
            if (::mapLibreMap.isInitialized && mapLibreMap.style != null){
                locationHelper = LocationHelper(
                    context = requireContext(),
                    map = mapLibreMap,
                    requestPermissionLauncher = requestLocationPermissionLauncher
                )

                locationHelper.checkAndEnableLocation(
                    onPermissionDenied = {
                        isLocationActive = false
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
                    },
                    onLocationAvailable = {
                        isLocationActive = true
                        binding.fabMyLocation.visibility = View.GONE
                        binding.fabZoomToLocation.visibility = View.VISIBLE
                        binding.fabDisableLocation.visibility = View.VISIBLE
                    }
                )
            }
        }

        binding.fabZoomToLocation.setOnClickListener {
            locationHelper.zoomToUserLocation()
        }

        binding.fabDisableLocation.setOnClickListener {
            locationHelper.disableLocationComponent()
            binding.fabZoomToLocation.visibility = View.GONE
            binding.fabDisableLocation.visibility = View.GONE
            binding.fabMyLocation.visibility = View.VISIBLE
            isLocationActive = false
        }

        binding.fabMapStyle.setOnClickListener {
            showMapStyleDialog()
        }

        if (isLocationActive) {
            binding.fabMyLocation.visibility = View.GONE
            binding.fabZoomToLocation.visibility = View.VISIBLE
            binding.fabDisableLocation.visibility = View.VISIBLE
        } else {
            binding.fabMyLocation.visibility = View.VISIBLE
            binding.fabZoomToLocation.visibility = View.GONE
            binding.fabDisableLocation.visibility = View.GONE
        }
    }

    private fun showMapStyleDialog() {
        MapStyleSelectorDialog(
            context = requireContext(),
            currentStyle = currentMapStyle
        ) { selected ->
            val previous = currentMapStyle
            val baseStyleChanged = selected.name != previous.name
            val contoursChanged = selected.showContours != previous.showContours
            val municipalitiesChanged = selected.showMunicipalities != previous.showMunicipalities

            if (baseStyleChanged) {
                changeMapStyle(selected)
            } else {
                currentMapStyle = selected
                viewModel.saveMapStyle(selected)

                mapLibreMap.style?.let { style ->
                    if (contoursChanged) {
                        applyContourVisibility(style, selected.showContours)
                    }
                    if (municipalitiesChanged) {
                        applyMunicipalitiesVisibility(style, selected.showMunicipalities)
                    }
                }
            }
        }.show()
    }

    private fun changeMapStyle(nextStyle: MapStyle) {
        when (val styleResult = mapStyleManager.setupStyle(nextStyle)) {
            is MapStyleManager.StyleSetupResult.Success -> {
                // If success, save style and reload fragment
                viewModel.saveMapStyle(nextStyle)
                currentMapStyle = nextStyle
                reloadFragment()
            }
            is MapStyleManager.StyleSetupResult.Error -> {
                val exception = styleResult.exception
                if (exception.message?.contains("Satellite data not downloaded") == true) {
                    showSatelliteDownloadDialog()
                } else {
                    Toast.makeText(requireContext(), "Errore: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun reloadFragment() {
        findNavController().navigate(
            R.id.action_mapFragment_self,
            null,
            androidx.navigation.navOptions {
                popUpTo(R.id.mapFragment) {
                    inclusive = true
                }
            }
        )
    }

    private fun showSatelliteDownloadDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Scarica dati satellitari")
            .setMessage(
                "Per utilizzare la modalità satellitare, è necessario scaricare circa 200 MB di dati aggiuntivi. " +
                "Il download verrà effettuato una sola volta e ti permetterà di usare la mappa satellitare anche offline in futuro.\n\n" +
                "Se vuoi, puoi avviare il download ora nella schermata dedicata."
            )
            .setPositiveButton("Vai al download") { _, _ ->
                findNavController().navigate(R.id.action_mapFragment_to_satelliteDataFragment)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationHelper.checkAndEnableLocation()
        } else {
            Toast.makeText(requireContext(), "Autorizzazione alla localizzazione negata", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMap() {
        val styleBuilder = when (val result = mapStyleManager.setupStyle(currentMapStyle)) {
            is MapStyleManager.StyleSetupResult.Success -> result.styleBuilder
            is MapStyleManager.StyleSetupResult.Error -> {
                Toast.makeText(requireContext(), "Errore nel caricamento dello stile: ${result.exception.message}", Toast.LENGTH_LONG).show()
                return
            }
        }

        showLoading()

        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle(styleBuilder) { style ->
                applyContourVisibility(style, currentMapStyle.showContours)
                applyMunicipalitiesVisibility(style, currentMapStyle.showMunicipalities)

                markerManager = MapMarkerManager(
                    mapView = mapView,
                    map = mapLibreMap,
                    style = style,
                    iconCache = markerIconCache,
                    onMarkerClick = { toponymId ->
                        viewModel.selectToponymById(toponymId)
                    }
                )

                restoreMapState()

                mapView.addOnDidFinishRenderingFrameListener(renderListener)
            }

            mapLibreMap.setLatLngBoundsForCameraTarget(MapConfig.LOCATION_BOUNDS_WITH_BUFFER)
            mapLibreMap.setMinZoomPreference(MapConfig.MIN_ZOOM_BOUND)
            mapLibreMap.setMaxZoomPreference(MapConfig.MAX_ZOOM_BOUND)
        }
    }

    private fun applyContourVisibility(style: org.maplibre.android.maps.Style, showContours: Boolean) {
        val visibility = if (showContours)
            Property.VISIBLE
        else
            Property.NONE

        style.getLayer("contour")?.setProperties(
            PropertyFactory.visibility(visibility)
        )
        style.getLayer("contour_label")?.setProperties(
            PropertyFactory.visibility(visibility)
        )
    }

    private fun applyMunicipalitiesVisibility(style: org.maplibre.android.maps.Style, showMunicipalities: Boolean) {
        val visibility = if (showMunicipalities)
            Property.VISIBLE
        else
            Property.NONE

        style.getLayer("municipality-boundary")?.setProperties(
            PropertyFactory.visibility(visibility)
        )
        style.getLayer("municipality-label")?.setProperties(
            PropertyFactory.visibility(visibility)
        )
    }

    private fun waitForNextFullRender(onComplete: () -> Unit) {
        val listener = object : MapView.OnDidFinishRenderingFrameListener {
            override fun onDidFinishRenderingFrame(
                fully: Boolean,
                frameEncodingTime: Double,
                frameRenderingTime: Double
            ) {
                if (fully) {
                    binding.mapView.removeOnDidFinishRenderingFrameListener(this)
                    onComplete()
                }
            }
        }
        binding.mapView.addOnDidFinishRenderingFrameListener(listener)
    }

    private val renderListener = object : MapView.OnDidFinishRenderingFrameListener {
        override fun onDidFinishRenderingFrame(
            fully: Boolean,
            frameEncodingTime: Double,
            frameRenderingTime: Double
        ) {
            if (fully) {
                mapView.removeOnDidFinishRenderingFrameListener(this)
                isMapFullyReady = true
                onMapReady?.invoke()
                onMapReady = null
            }
        }
    }

    private fun observeToponyms() {
        viewModel.toponyms.observe(viewLifecycleOwner) { toponyms ->
            if (isMapFullyReady && ::markerManager.isInitialized) {
                viewLifecycleOwner.lifecycleScope.launch {

                    if (!viewModel.iconsLoaded) {
                        markerIconCache.preloadBitmaps(toponyms)
                        viewModel.iconsLoaded = true
                    }
                    markerManager.loadMarkerIcons(toponyms)

                    markerManager.addMarkers(toponyms, viewModel.selectedToponym.value?.id) {
                        waitForNextFullRender {
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.mapView.addOnDidFinishRenderingFrameListener(frameRenderListener)
    }

    private fun observeSelectedToponym() {
        viewModel.selectedToponym.observe(viewLifecycleOwner) { toponym ->
            if (!::markerManager.isInitialized) {
                return@observe
            }

            markerManager.setSelectedId(toponym?.id)

            if (toponym != null) {
                showMarkerInfo(toponym)
                if (pendingZoom){
                    zoomToToponym(toponym.lat, toponym.lon)
                    pendingZoom = false
                }
            } else {
                ViewAnimatorUtils.hideBottomPanel(binding.previewContainer)
            }
        }
    }

    private fun showMarkerInfo(toponym: Toponym) {
        ViewAnimatorUtils.showBottomPanel(binding.previewContainer)
        binding.markerNameText.text = toponym.nome
    }

    private fun zoomToToponym(lat: Double, lon: Double){
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(lat, lon))
            .zoom(MapConfig.DETAIL_ZOOM)
            .build()
        mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun navigateToDetail(toponym: Toponym) {
        ViewAnimatorUtils.expandWithShimmerAndNavigate(
            preview = binding.previewContainer,
            rootView = binding.mapFragmentRoot,
            shimmerContainer = binding.previewShimmerContainer,
            contentContainer = binding.previewContentContainer,
            shimmerView = createDetailShimmerPlaceholder(binding.previewContainer.context)
        ) {
            val action = MapFragmentDirections.actionMapFragmentToDetailFragment(toponym)
            findNavController().navigate(action)
        }
    }

    private fun restoreMapState() {
        val savedPosition = viewModel.getSavedCameraPosition()
        if (savedPosition != null) {
            mapLibreMap.cameraPosition = savedPosition
        } else {
            val cameraPosition = CameraPosition.Builder()
                .target(MapConfig.DEFAULT_LOCATION)
                .zoom(MapConfig.DEFAULT_ZOOM)
                .build()
            mapLibreMap.cameraPosition = cameraPosition
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("locationActive", isLocationActive)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        if (::mapLibreMap.isInitialized) {
            val cameraPosition = mapLibreMap.cameraPosition
            viewModel.saveCameraPosition(cameraPosition)
        }
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::markerManager.isInitialized) {
            markerManager.onDestroy()
        }
        mapView.onDestroy()
        _binding = null
    }

    inline fun <reified T : Parcelable> Fragment.consumeParcelableArgOnce(key: String): T? {
        val result: T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(key) as? T
        }
        arguments?.remove(key)
        return result
    }
}
