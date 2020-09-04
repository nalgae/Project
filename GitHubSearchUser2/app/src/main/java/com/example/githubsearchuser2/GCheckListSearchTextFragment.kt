package com.example.githubsearchuser2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.githubsearchuser2.GCheckListSearchActivity.Companion.eventTypesDB
import com.example.githubsearchuser2.paginglib.GlideApp
import com.example.githubsearchuser2.paginglib.ui.GCheckListSearchViewHolder
import com.example.githubsearchuser2.paginglib.vo.WebClientPostCheckListSearch
import kotlinx.android.synthetic.main.gchecklist_fragment_search_api.*
import org.json.JSONException
import java.util.ArrayList
import java.util.concurrent.Executors


class GCheckListSearchTextFragment : Fragment() {

    companion object {
        var mContext: Context? = null

        data class GCheckListSearchHeaderRow(
            var login: String,
            var id: String,
            var node_id: String,
            var avatar_url: String,
            var url: String,
            var html_url: String,
            var followers_url: String,
            var following_url: String,
            var gists_url: String,
            var starred_url: String,
            var subscriptions_url: String,
            var organizations_url: String,
            var repos_url: String,
            var events_url: String,
            var received_events_url: String,
            var type: String,
            var score: String )

        var mTwoListRecyclerView: RecyclerView? = null
        var mTwoAdapterHeaderList: MutableList<GCheckListSearchHeaderRow>? = null

        fun LoadCheckListHeaderList(strQuery: String) {
            LoadCheckListHeaderListSub(strQuery)
        }
        fun LoadCheckListHeaderListSub(strQuery: String) {
            Executors.newSingleThreadScheduledExecutor().execute {
                var eventTypes = ArrayList<GCheckListSearchDB>()
                try {
                    var _strQuery = strQuery.replace(" ","")
                    if( _strQuery.isEmpty() ) {
                        eventTypes = eventTypesDB.getEventTypes().toMutableList() as ArrayList<GCheckListSearchDB>
                    } else {
                        _strQuery = "%" + _strQuery + "%"
                        eventTypes = eventTypesDB.getEventTypeWithId(_strQuery).toMutableList() as ArrayList<GCheckListSearchDB>
                    }
                    Log.i("Local Query", _strQuery)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                mTwoAdapterHeaderList!!.clear()

                val iArraySize = eventTypes.size
                try {
                    for (i in 0 until iArraySize) {
                        val rawItem = eventTypes.get(i)

                        val item = GCheckListSearchHeaderRow(rawItem.login, rawItem.id.toString(), rawItem.node_id, rawItem.avatar_url
                            , rawItem.url, rawItem.html_url, rawItem.followers_url, rawItem.following_url
                            , rawItem.gists_url, rawItem.starred_url, rawItem.subscriptions_url, rawItem.organizations_url
                            , rawItem.repos_url, rawItem.events_url, rawItem.received_events_url, rawItem.type, rawItem.score )
                        mTwoAdapterHeaderList!!.add(item)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                // Header List
                mTwoListRecyclerView?.adapter = MyRecyclerAdapter(mTwoAdapterHeaderList!!, R.layout.gchecklist_header_local_row)

                if( iArraySize > 0 ) {
                    val mCommonTLibEx = CommonTLibEx()
                    mContext?.let { mCommonTLibEx.CheckListSearchUserListRangeChangedReceiver(it, iArraySize) }
                }
            }

        }


    }

    // CheckList 화면에 범위지정 갱신
    private val mCheckListSearchUserListRangeChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val iCount = intent.getIntExtra("count",0)

            mTwoListRecyclerView?.adapter!!.notifyItemRangeChanged(0, 1000)
        }
    }
    // CheckList 화면에 갱신2
    private val mCheckListNotifyItemRemovedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val iPosition = intent.getIntExtra("position",0)


            mTwoListRecyclerView?.adapter!!.notifyItemRemoved(iPosition)
        }
    }
    // CheckList 화면에 전체갱신
    private val mCheckListAllRangeChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            mTwoListRecyclerView?.adapter!!.notifyItemRangeChanged(0, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              saveInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.gchecklist_fragment_search_local, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mContext = activity
        mContext?.registerReceiver(mCheckListAllRangeChangedReceiver, IntentFilter(NexBroadcastReceiver.CHECKLIST_ALLRANGECHANGE_REFRESH))
        mContext?.registerReceiver(mCheckListNotifyItemRemovedReceiver, IntentFilter(NexBroadcastReceiver.CHECKLIST_NOTIFYITEMREMOVE_REFRESH))
        mContext?.registerReceiver(mCheckListSearchUserListRangeChangedReceiver, IntentFilter(NexBroadcastReceiver.CHECKLIST_SEARCHUSERLISTRANGECHANGE_REFRESH))


        mTwoAdapterHeaderList = ArrayList()
        // SEARCH Header List
        mTwoListRecyclerView = getView()?.findViewById(R.id.search_headerlist_item)
        mTwoListRecyclerView!!.layoutManager = LinearLayoutManager(mContext)
        mTwoListRecyclerView!!.itemAnimator = DefaultItemAnimator()
        mTwoListRecyclerView?.adapter = MyRecyclerAdapter(mTwoAdapterHeaderList!!, R.layout.gchecklist_header_local_row)

        LoadCheckListHeaderList("")
    }

    class MyRecyclerAdapter(private val mAdapterList: List<GCheckListSearchHeaderRow>, private val itemLayout: Int) : RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

            val view = LayoutInflater.from(viewGroup.context).inflate(itemLayout, viewGroup, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            val item = mAdapterList[position]

            viewHolder.textViewName.text = item.login
            viewHolder.textViewID.text = item.id
            viewHolder.textViewHTMLUrl.text = item.html_url
            viewHolder.checkBoxJobCheck.isClickable = false
            viewHolder.checkBoxJobCheck.isChecked = true
            viewHolder.checkBoxJobCheck.isClickable = true

            var strDataURL = item.avatar_url
            if (strDataURL?.startsWith("http") == true) {

                mContext?.let {
                    GlideApp.with(it).load(strDataURL)
                        .centerCrop()
                        .placeholder(R.drawable.bg)
                        .into(viewHolder.imgViewImgUrl)
                }
            }

            viewHolder.itemView.tag = item
        }

        override fun getItemCount(): Int {
            return mAdapterList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            internal var mLayoutView: View
            internal var textViewName: TextView
            internal var textViewID: TextView
            internal var textViewHTMLUrl: TextView
            internal var checkBoxJobCheck: CheckBox
            internal var imgViewImgUrl: ImageView

            init {
                mLayoutView = itemView.findViewById(R.id.row_team_cardlayout)
                textViewName = itemView.findViewById(R.id.row_team_name)
                textViewID = itemView.findViewById(R.id.row_team_id)
                textViewHTMLUrl = itemView.findViewById(R.id.row_team_htmlurl)
                checkBoxJobCheck = itemView.findViewById(R.id.row_team_check)
                imgViewImgUrl = itemView.findViewById(R.id.row_team_imgurl)

                checkBoxJobCheck.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val position = adapterPosition
                val item = mAdapterList[position]

                if( v == checkBoxJobCheck ) {

                    if( checkBoxJobCheck.isChecked ) {
                    } else {
                        deleteEventTypeSync(item.node_id, position)
                        uncheckGitHubSearchUserSync(item.node_id)
                    }
                }
            }
        }

        fun deleteEventTypeSync(node_id: String, position: Int) {
            Executors.newSingleThreadScheduledExecutor().execute {
                eventTypesDB.deleteEventTypesWithGCheckListId(node_id)
                mTwoAdapterHeaderList?.removeAt(position)

                val mCommonTLibEx = CommonTLibEx()
                mCommonTLibEx.CheckListNotifyItemRemoveReceiver(GCheckListSearchActivity.mContext, position)
            }
        }

        fun uncheckGitHubSearchUserSync(node_id: String) {
            val iCount: Int? = GCheckListSearchFragment2.adapter.currentList?.size
            for( i in 0 until iCount!!) {
                val curList = GCheckListSearchFragment2.adapter.currentList?.get(i)
                if (curList != null) {
                    if( curList.node_id == node_id) {
                        curList.bCheckFlag = false

                        val mCommonTLibEx = CommonTLibEx()
                        mCommonTLibEx.GitHubSearchUserListRefreshReceiver(GCheckListSearchActivity.mContext, i)
                        break
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()

        mContext?.unregisterReceiver(mCheckListSearchUserListRangeChangedReceiver)
        mContext?.unregisterReceiver(mCheckListNotifyItemRemovedReceiver)
        mContext?.unregisterReceiver(mCheckListAllRangeChangedReceiver)

    }
}