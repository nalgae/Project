package com.example.githubsearchuser2

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.githubsearchuser2.paginglib.GlideApp
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.githubsearchuser2.paginglib.GCheckListSearchServiceLocator
import com.example.githubsearchuser2.paginglib.repository.GCheckListSearchPostRepository
import com.example.githubsearchuser2.paginglib.repository.NetworkState
import com.example.githubsearchuser2.paginglib.ui.GCheckListSearchAdapter
import com.example.githubsearchuser2.paginglib.ui.GCheckListSearchSubWebClientViewModel
import com.example.githubsearchuser2.paginglib.vo.WebClientPostCheckListSearch

import kotlinx.android.synthetic.main.gchecklist_fragment_search_api.*

class GCheckListSearchFragment2 : Fragment() {
    companion object {
        var mContext: Context? = null
        const val KEY_REPOSITORY_TYPE = "GCheckListSearch"
        lateinit var model: GCheckListSearchSubWebClientViewModel
        lateinit var adapter: GCheckListSearchAdapter

        fun intentFor(context: Context, type: GCheckListSearchPostRepository.Type): Intent {
            val intent = Intent(context, GCheckListSearchFragment2::class.java)
            intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
            return intent
        }

        fun GCallLoadTimeLineHeaderList(pageKey: Int, limitKey: Int, searchUser: String) {
            model.showSubWebDataProcess(TalkMobileApplication.strMY_WEBID, pageKey, limitKey, searchUser)
        }
    }

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    internal var strGroupKey: String? = ""

    internal var strSearchDate: String? = null


    private val mGitHubSearchUserListRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val iPosition = intent.getIntExtra("position",0)

            adapter.notifyItemRangeChanged(iPosition, 1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.gchecklist_fragment_search_api, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mContext = activity

        requireActivity().registerReceiver(mGitHubSearchUserListRefreshReceiver, IntentFilter(NexBroadcastReceiver.GITHUBSEARCHUSERLIST_REFRESH))

        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        initSearch()

    }

    private fun getViewModel(): GCheckListSearchSubWebClientViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            @SuppressLint("UseRequireInsteadOfGet")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repoTypeParam = activity!!.intent.getIntExtra(KEY_REPOSITORY_TYPE, 0)
                val repoType = GCheckListSearchPostRepository.Type.values()[repoTypeParam]
                val repo = GCheckListSearchServiceLocator.instance(this@GCheckListSearchFragment2.context!!)
                        .getRepository(repoType)
                @Suppress("UNCHECKED_CAST")
                return GCheckListSearchSubWebClientViewModel(repo) as T
            }
        })[GCheckListSearchSubWebClientViewModel::class.java]
    }

    private fun initAdapter() {
        val glide = GlideApp.with(this)
        adapter = GCheckListSearchAdapter(glide) {
            model.retry()
        }
        search_asisheaderlist_item.adapter = adapter
        search_asisheaderlist_item.setLayoutManager(LinearLayoutManager(context)) // RecyclerView Grid 모양설정
        model.posts.observe(viewLifecycleOwner, Observer<PagedList<WebClientPostCheckListSearch>> {
            adapter.submitList(it)
        })
        model.networkState.observe(viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(viewLifecycleOwner, Observer {
            swipe_layout.isRefreshing = it == NetworkState.LOADING
        })
        swipe_layout.setOnRefreshListener {
            model.refresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TalkMobileApplication.strMY_WEBID, model.currentSubWebDataProcess())
    }

    private fun initSearch() {
        // not use
    }

    // not use
    private fun LoadTimeLineHeaderList(pageKey: Int, limitKey: Int, searchUser: String) {
        if (model.showSubWebDataProcess(TalkMobileApplication.strMY_WEBID, pageKey, limitKey, searchUser)) {
            search_asisheaderlist_item.scrollToPosition(0)
            (search_asisheaderlist_item.adapter as? GCheckListSearchAdapter)?.submitList(null)
        }
    }

    override fun onDetach() {
        super.onDetach()

        requireActivity().unregisterReceiver(mGitHubSearchUserListRefreshReceiver)
    }

}