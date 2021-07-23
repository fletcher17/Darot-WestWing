package com.assessment.westwingcampaign.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessment.data.campaign.model.CampaignDetails
import com.assessment.data.campaign.viewmodel.CampaignListViewModel
import com.assessment.westwingcampaign.R
import com.assessment.westwingcampaign.databinding.FragmentLandingBinding
import com.assessment.westwingcampaign.ui.activities.ActivityUiStateListener
import com.assessment.westwingcampaign.ui.adapters.CampaignListViewAdapter
import com.assessment.westwingcampaign.ui.adapters.ItemSpaceDecoration
import com.assessment.westwingcampaign.ui.adapters.ItemZoomListener
import com.assessment.westwingcampaign.utils.checkOrientation
import com.assessment.westwingcampaign.utils.extensions.getName
import com.assessment.westwingcampaign.utils.extensions.goto
import com.assessment.westwingcampaign.utils.extensions.setUpUiState
import com.darotpeacedude.eivom.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [LandingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class LandingFragment :
    Fragment(R.layout.fragment_landing),
    ItemZoomListener,
    FragmentUiStateListener {
    private val TAG by lazy { getName() }
    private val binding by viewBinding(FragmentLandingBinding::bind)
    private val campaignListViewModel: CampaignListViewModel by activityViewModels()
    lateinit var campaignViewAdapter: CampaignListViewAdapter
    lateinit var activityUiState: ActivityUiStateListener
    lateinit var fragmentUiUpdate: FragmentUiStateListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        campaignViewAdapter = CampaignListViewAdapter(this)
        fragmentUiUpdate = this
    }

    override fun onResume() {
        super.onResume()
        setupView()
        setUpData()
        scrollToPosition(campaignListViewModel.adapterPosition)
    }

    private fun setUpData() {
        setUpUiState(campaignListViewModel.campaignUiState, fragmentUiUpdate)
    }

    private fun setupView() {
        val orientation = checkOrientation()
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.campaignListRcv.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                addItemDecoration(ItemSpaceDecoration(8, 2))
            }
        } else {
            binding.campaignListRcv.apply {
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(ItemSpaceDecoration(8, 1))
            }
        }
        binding.campaignListRcv.adapter = campaignViewAdapter
    }

    override fun navigate(position: Int) {
        val direction =
            LandingFragmentDirections.actionLandingFragmentToCampaignDetailsFragment(position)
        goto(direction)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityUiState = requireActivity() as ActivityUiStateListener
    }

    override fun <T> onSuccess(data: T) {
        campaignViewAdapter.setData(data as? List<CampaignDetails>)
        activityUiState.displayDataFragment()
    }

    override fun onError(error: String?) {
        activityUiState.showErrorPage(error)
    }

    override fun onNetworkError() {
        activityUiState.showErrorPage(null)
    }

    override fun loading() {
        activityUiState.loading()
    }

    private fun scrollToPosition(position: Int) {
        binding.campaignListRcv.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                binding.campaignListRcv.removeOnLayoutChangeListener(this)
                Log.i(TAG, "${campaignListViewModel.adapterPosition}")
                val layoutManager = binding.campaignListRcv.layoutManager
                val viewAtPosition = layoutManager?.findViewByPosition(position)
                if (viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)) {
                    binding.campaignListRcv.post { layoutManager?.scrollToPosition(position) }
                }
            }
        })
    }
}
