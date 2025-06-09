package com.ferrarieugenio.toponomastica_stenico_app.ui.main.detail

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentDetailBinding
import dagger.hilt.android.AndroidEntryPoint

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

        viewModel.toponym.observe(viewLifecycleOwner) { toponym ->
            binding.nomeTextView.text = toponym.nome
            binding.formaUfficialeTextView.text = toponym.forma_ufficiale ?: "N/A"
            binding.comuneTextView.text = toponym.comune
            binding.descrizioneTextView.text = toponym.descrizione
            binding.quotaTextView.text = "${toponym.quota} m"
            binding.latlonTextView.text = "Lat: ${toponym.lat}, Lon: ${toponym.lon}"
            binding.tagsTextView.text = toponym.tags.joinToString(", ")
            binding.clusterTextView.text = toponym.cluster
            binding.hcClusterTextView.text = toponym.hc_cluster
            binding.variantiTextView.text = toponym.varianti?.joinToString(", ") ?: "N/A"
            binding.neighborsTextView.text = toponym.closest_5_neighbors_ids.joinToString(", ")        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}