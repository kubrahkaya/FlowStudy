package com.example.flowstudy.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.flowstudy.R
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlantListFragment : Fragment(R.layout.fragment_plant_list) {

    private val viewModel: PlantListViewModel by viewModels()
    private val adapter = PlantsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerUI = view.findViewById<ProgressBar>(R.id.spinner)
        val layout = view.findViewById<FrameLayout>(R.id.layout)
        val recyclerView = view.findViewById<RecyclerView>(R.id.plant_list)

        viewModel.spinner.observe(viewLifecycleOwner) { show ->
            spinnerUI.visibility = if (show) View.VISIBLE else View.GONE
        }

        viewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                Snackbar.make(layout, text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        }

        recyclerView.adapter = adapter

        subscribeUI()

        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter_zone -> {
                updateData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateData() {
        with(viewModel) {
            if (isFiltered()) {
                clearGrowZoneNumber()
            } else {
                setGrowZoneNumber(9)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_plant_list, menu)
    }

    private fun subscribeUI() {
        viewModel.plantsUsingFlow.observe(viewLifecycleOwner) { plants ->
            adapter.differ.submitList(plants)
        }
    }


}