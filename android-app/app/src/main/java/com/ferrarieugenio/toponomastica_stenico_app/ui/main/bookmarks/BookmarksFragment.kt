package com.ferrarieugenio.toponomastica_stenico_app.ui.main.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferrarieugenio.toponomastica_stenico_app.databinding.FragmentBookmarksBinding
import com.ferrarieugenio.toponomastica_stenico_app.ui.adapters.BookmarkAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarksViewModel by viewModels()

    private lateinit var adapter: BookmarkAdapter

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}