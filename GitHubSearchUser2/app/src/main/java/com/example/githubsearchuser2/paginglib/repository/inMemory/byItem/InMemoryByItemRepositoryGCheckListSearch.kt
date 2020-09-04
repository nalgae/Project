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

package com.example.githubsearchuser2.paginglib.repository.inMemory.byItem

import androidx.lifecycle.Transformations
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import com.example.githubsearchuser2.paginglib.api.GCheckListSearchWebClientApi
import com.example.githubsearchuser2.paginglib.repository.GCheckListSearchPostRepository
import com.example.githubsearchuser2.paginglib.repository.Listing
import com.example.githubsearchuser2.paginglib.repository.NetworkState
import com.example.githubsearchuser2.paginglib.vo.WebClientPostCheckListSearch
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.Executor

/**
 * 네트워크에서 직접 데이터를 로드하고 항목 이름을 사전/다음 페이지를 검색하는 키로 사용하는 목록을 반환하는 리포지토리 구현.
 */
class InMemoryByItemRepositoryGCheckListSearch(
        private val redditApi: GCheckListSearchWebClientApi,
        private val networkExecutor: Executor) : GCheckListSearchPostRepository {

    @MainThread
    override fun postsOfGCheckListSearch(subUserID: String, pageKey: Int, limitKey: Int, searchUser: String, pageSize: Int): Listing<WebClientPostCheckListSearch> {
        val sourceFactory = GCheckListSearchDataSourceFactory(redditApi, subUserID, pageKey, limitKey, searchUser, networkExecutor)

        val livePagedList = sourceFactory.toLiveData(
                config = Config(
                        pageSize = pageSize,
                        enablePlaceholders = false,
                        initialLoadSizeHint = pageSize * 2),
                fetchExecutor = networkExecutor)

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                  it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }
}

// ItemKeyedDataSource : Item-key 기반의 데이터를 가져올 때 사용 , PositionalDataSource : 위치 기반 데이터에 사용
class GCheckListSearchItemKeyedDataSource(
        private val redditApi: GCheckListSearchWebClientApi,
        private val subUserID: String,
        private val pageKey: Int,
        private val limitKey: Int,
        private val searchUser: String,
        private val retryExecutor: Executor)
    : ItemKeyedDataSource<String, WebClientPostCheckListSearch>() {
    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * 페이징은 항상 부하를 호출하므로 상태에 동기화가 없음.
     * 먼저 초기화한 후 load를 호출하기 전에 load가 성공 값을 반환할 때까지 기다리세요.
     * 여러 네트워크 상태 동기화에 대한 자세한 예는 경계콜백 예제를 참조.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()
    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<WebClientPostCheckListSearch>) {
        // ignored, since we only ever append to our initial load
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<WebClientPostCheckListSearch>) {

        networkState.postValue(NetworkState.LOADING)

        redditApi.getTopAfter(searchuser = searchUser,
                after = params.key,
                page = pageKey,
                limit = params.requestedLoadSize
                ).enqueue(
                object : retrofit2.Callback<GCheckListSearchWebClientApi.ListingResponse> {
                    override fun onFailure(call: Call<GCheckListSearchWebClientApi.ListingResponse>, t: Throwable) {
                        // keep a lambda for future retry
                        retry = {
                            loadAfter(params, callback)
                        }
                        // publish the error
                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(
                            call: Call<GCheckListSearchWebClientApi.ListingResponse>,
                            response: Response<GCheckListSearchWebClientApi.ListingResponse>) {
                        if (response.isSuccessful) {
                            val items = response.body()?.items?.map { it } ?: emptyList()
                            // clear retry since last request succeeded
                            retry = null
                            callback.onResult(items)
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = {
                                loadAfter(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )
    }

    /**
     * 이름 필드는 포스트 항목에 대한 고유 식별자.
     */
    override fun getKey(item: WebClientPostCheckListSearch): String = item.rnumindex

    override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<WebClientPostCheckListSearch>) {
        val request = redditApi.getTop(
                searchuser = searchUser,
                page = pageKey,
                limit = params.requestedLoadSize
        )
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        try {
            val response = request.execute()
            val items = response.body()?.items?.map { it } ?: emptyList()
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(items)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }
}

/**
 * 마지막으로 생성된 데이터 소스를 관찰할 수 있는 방법을 제공하는 단순한 데이터 원본 팩토리.
 * 이를 통해 네트워크 요청 상태 등을 UI로 다시 채널링할 수 있다.
 * 리포지토리 클래스의 목록 작성을 참조.
 */
class GCheckListSearchDataSourceFactory(
        private val redditApi: GCheckListSearchWebClientApi,
        private val subUserID: String,
        private val pageKey: Int,
        private val limitKey: Int,
        private val searchUser: String,
        private val retryExecutor: Executor) : DataSource.Factory<String, WebClientPostCheckListSearch>() {

    val sourceLiveData = MutableLiveData<GCheckListSearchItemKeyedDataSource>()

    override fun create(): DataSource<String, WebClientPostCheckListSearch> {
        val source = GCheckListSearchItemKeyedDataSource(redditApi, subUserID, pageKey, limitKey, searchUser, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}