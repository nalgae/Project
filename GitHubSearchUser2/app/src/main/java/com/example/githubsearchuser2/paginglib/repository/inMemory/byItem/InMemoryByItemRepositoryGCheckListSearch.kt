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
 * Repository implementation that returns a Listing that loads data directly from the network
 * and uses the Item's name as the key to discover prev/next pages.
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

/**
 * A data source that uses the "name" field of posts as the key for next/prev pages.
 * <p>
 * Note that this is not the correct consumption of the Reddit API but rather shown here as an
 * alternative implementation which might be more suitable for your backend.
 * see PageKeyedSubredditDataSource for the other sample.
 */
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
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter and we don't support loadBefore
     * in this example.
     * <p>
     * See BoundaryCallback example for a more complete example on syncing multiple network states.
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
//origin                            val items = response.body()?.data?.items?.map { it.data } ?: emptyList()
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
     * The name field is a unique identifier for post items.
     * (no it is not the title of the post :) )
     * https://www.reddit.com/dev/api
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
//origin            val items = response.body()?.data?.items?.map { it.data } ?: emptyList()
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
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
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