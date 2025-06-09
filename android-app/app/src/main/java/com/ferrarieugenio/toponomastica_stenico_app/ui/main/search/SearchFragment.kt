package com.ferrarieugenio.toponomastica_stenico_app.ui.main.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.ToponymAssetDataSource
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentSearchBinding
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ToponymAdapter

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ToponymAdapter(emptyList()) { clickedToponym ->
            val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(clickedToponym)
            findNavController().navigate(action)
        }
        binding.toponymRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.toponymRecyclerView.adapter = adapter

        // Observe LiveData
        viewModel.filteredToponyms.observe(viewLifecycleOwner) { toponyms ->
            adapter.updateList(toponyms)
        }

        // Search input
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* No-op */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* No-op */ }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}