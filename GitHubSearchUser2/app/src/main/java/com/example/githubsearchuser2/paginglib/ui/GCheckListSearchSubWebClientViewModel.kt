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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import com.example.githubsearchuser2.paginglib.repository.GCheckListSearchPostRepository
// MutableLiveData 가 변경이 이루어지면, postsOfSubreddit 에 의해 WEB 이 호출되어 진다.
class GCheckListSearchSubWebClientViewModel(private val repository: GCheckListSearchPostRepository) : ViewModel() {
    private val subUserID = MutableLiveData<String>()
    private var pageKey = 0
    private var limitKey = 0
    private lateinit var searchUser: String
    private val repoResult = map(subUserID) {
        repository.postsOfGCheckListSearch(it,pageKey,limitKey, searchUser, 30)
    }
    val posts = switchMap(repoResult, { it.pagedList })!!
    val networkState = switchMap(repoResult, { it.networkState })!!
    val refreshState = switchMap(repoResult, { it.refreshState })!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }
    fun showSubWebDataProcess(subuserid: String, pageKey: Int, limitKey: Int, searchUser: String): Boolean {
        this.pageKey = pageKey
        this.limitKey = limitKey
        this.searchUser = searchUser
        subUserID.value = subuserid

        return true
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun currentSubWebDataProcess(): String? = subUserID.value
}

