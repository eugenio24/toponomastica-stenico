package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings.info

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentAppInfoBinding

class AppInfoFragment : Fragment() {
    private var _binding: FragmentAppInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.githubChip.setOnClickListener {
            val githubUrl = "https://github.com/eugenio24/toponomastica-stenico"
            val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}