/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.githubsearchuser2.paginglib.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.githubsearchuser2.paginglib.GlideRequests
import com.example.githubsearchuser2.paginglib.vo.WebClientPostCheckListSearch
import com.example.githubsearchuser2.CommonTLibEx
import com.example.githubsearchuser2.*
import com.example.githubsearchuser2.GCheckListSearchActivity.Companion.eventTypesDB
import com.example.githubsearchuser2.GCheckListSearchActivity.Companion.mContext
import java.util.concurrent.Executors


/**
 * A RecyclerView ViewHolder that displays a reddit post.
 */
class GCheckListSearchViewHolder(view: View, private val glide: GlideRequests)
    : RecyclerView.ViewHolder(view), View.OnClickListener {
    //private val mLayoutView: View = view.findViewById(R.id.row_team_cardlayout)
    private val textViewName: TextView = view.findViewById(R.id.row_team_name)
    private val textViewID: TextView = view.findViewById(R.id.row_team_id)
    private val textViewHTMLUrl: TextView = view.findViewById(R.id.row_team_htmlurl)
    private val checkBoxJobCheck: CheckBox = view.findViewById(R.id.row_team_check)
    private var imgViewImgUrl : ImageView = view.findViewById(R.id.row_team_imgurl)
    private var post : WebClientPostCheckListSearch? = null

    companion object {
        fun create(parent: ViewGroup, glide: GlideRequests): GCheckListSearchViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.gchecklist_header_api_row, parent, false)
            return GCheckListSearchViewHolder(view, glide)
        }
    }

    init {
        checkBoxJobCheck.setOnClickListener(this)
    }

    fun insertOrUpdateEventTypeSync(eventType: GCheckListSearchDB) {
        Executors.newSingleThreadScheduledExecutor().execute {
            // DB 추가
            eventTypesDB.insertOrUpdate(eventType)

            // AdapterList리스트 추가
            val item = GCheckListSearchTextFragment.Companion.GCheckListSearchHeaderRow(
                eventType.login, eventType.id.toString(), eventType.node_id, eventType.avatar_url, eventType.url, eventType.html_url,
                eventType.followers_url, eventType.following_url, eventType.gists_url, eventType.starred_url, eventType.subscriptions_url,
                eventType.organizations_url, eventType.repos_url, eventType.events_url, eventType.received_events_url, eventType.type, eventType.score)
            if( !GCheckListSearchTextFragment.mTwoAdapterHeaderList!!.contains(item) )
                GCheckListSearchTextFragment.mTwoAdapterHeaderList!!.add(item)

            // 화면 갱신
            val mCommonTLibEx = CommonTLibEx()
            mCommonTLibEx.CheckListAllRangeChangedReceiver(mContext)
        }
    }

    override fun onClick(v: View?) {
        val position = adapterPosition // getLayoutPosition
        val item = itemView.tag as WebClientPostCheckListSearch

        if( v == checkBoxJobCheck ) {
            val id = item.rnumindex.toLong()

            // Local DB 체크된 데이터 저장
            val checkListData = post?.let {
                GCheckListSearchDB(id, it.login, it.node_id, it.avatar_url, it.url, it.html_url, it.followers_url, it.following_url
                    , it.gists_url, it.starred_url, it.subscriptions_url, it.organizations_url, it.repos_url, it.events_url, it.received_events_url, it.type, it.score)
            }
            if (checkListData != null) {
                insertOrUpdateEventTypeSync(checkListData)
            }

            if( checkBoxJobCheck.isChecked ) {
                val locidx = GCheckListSearchFragment2.model.posts.value!!.indexOf(post) // paginglib livedata 에서 읽어온 데이터 중에 해당 데이터 검색해서 index 반환 2/2
                val item = GCheckListSearchFragment2.model.posts.value!!.get(locidx) // index 에 해당하는 데이터 읽음
                if (item != null) {
                    item.bCheckFlag = true // paginglib livedata 값을 변경
                    itemView.tag = item // 데이터 값 갱신
                }
            } else {
                val locidx = GCheckListSearchFragment2.model.posts.value!!.indexOf(post) // paginglib livedata 에서 읽어온 데이터 중에 해당 데이터 검색해서 index 반환
                val item = GCheckListSearchFragment2.model.posts.value!!.get(locidx) // index 에 해당하는 데이터 읽음
                if (item != null) {
                    item.bCheckFlag = false // paginglib livedata 값을 변경
                    itemView.tag = item
                }
            }
        }
    }

    fun bind(post: WebClientPostCheckListSearch?) {
        this.post = post

        textViewName.text = post?.login
        textViewID.text = post?.rnumindex
        textViewHTMLUrl.text = post?.html_url
        checkBoxJobCheck.isChecked = post?.bCheckFlag!!


        val strDataURL = post.avatar_url
        if ( strDataURL.startsWith("http") ) {
            glide.load(strDataURL)
                    .centerCrop()
                    .placeholder(R.drawable.bg)
                    .into(imgViewImgUrl)
        }

        itemView.tag = post

    }


    fun updateScore(item: WebClientPostCheckListSearch?) {
        post = item
    }

}