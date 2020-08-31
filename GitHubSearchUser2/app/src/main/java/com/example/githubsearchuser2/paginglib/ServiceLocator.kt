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

package com.example.githubsearchuser2.paginglib

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.githubsearchuser2.paginglib.api.*
import com.example.githubsearchuser2.paginglib.repository.*
import com.example.githubsearchuser2.paginglib.repository.inMemory.byItem.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


// GCheckListSearchServiceLocator ----------------------------------------------

interface GCheckListSearchServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: GCheckListSearchServiceLocator? = null
        fun instance(context: Context): GCheckListSearchServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = GCheckListSearchDefaultServiceLocator(
                            app = context.applicationContext as Application,
                            useInMemoryDb = false)
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: GCheckListSearchServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(type: GCheckListSearchPostRepository.Type): GCheckListSearchPostRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getWebClientApi(): GCheckListSearchWebClientApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class GCheckListSearchDefaultServiceLocator(val app: Application, val useInMemoryDb: Boolean) : GCheckListSearchServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)
/*
    private val db by lazy {
        RedditDb.create(app, useInMemoryDb)
    }*/

    private val api by lazy {
        GCheckListSearchWebClientApi.create()
    }

    override fun getRepository(type: GCheckListSearchPostRepository.Type): GCheckListSearchPostRepository {
        return when (type) {
            GCheckListSearchPostRepository.Type.IN_MEMORY_BY_ITEM -> InMemoryByItemRepositoryGCheckListSearch(
                    redditApi = getWebClientApi(),
                    networkExecutor = getNetworkExecutor())
/*            WebClientPostRepository.Type.IN_MEMORY_BY_PAGE -> InMemoryByPageKeyRepository(
                    redditApi = getWebClientApi(),
                    networkExecutor = getNetworkExecutor())*/
/*            WebClientPostRepository.Type.DB -> DbRedditPostRepository(
                    db = db,
                    redditApi = getRedditApi(),
                    ioExecutor = getDiskIOExecutor())*/
        }
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getWebClientApi(): GCheckListSearchWebClientApi = api
}