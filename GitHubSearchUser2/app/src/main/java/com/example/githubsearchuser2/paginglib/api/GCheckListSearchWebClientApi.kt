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

package com.example.githubsearchuser2.paginglib.api

import android.util.Log
import com.example.githubsearchuser2.paginglib.vo.WebClientPostCheckListSearch
import com.example.githubsearchuser2.TalkMobileApplication
import com.google.gson.JsonObject
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API communication setup
 */
interface GCheckListSearchWebClientApi {
    @Headers("Accept: application/vnd.github.v3+json")
    @GET("/search/users?")
    fun getTop(
            @Query(value = "q", encoded = true) searchuser: String,
            @Query("page") page: Int,
            @Query("per_page") limit: Int): Call<ListingResponse>

    @GET("/search/users?")
    fun getTopAfter(
        @Query("q") searchuser: String,
        @Query("after") after: String,
        @Query("page") page: Int,
        @Query("per_page") limit: Int): Call<ListingResponse>

    @GET("/search/users?")
    fun getTopBefore(  // why not use ???
        @Query("q") searchuser: String,
        @Query("before") before: String,
        @Query("page") page: Int,
        @Query("per_page") limit: Int): Call<ListingResponse>

    class ListingResponse(val items: List<WebClientPostCheckListSearch>)

/*    class ListingData(
            val items: List<RedditChildrenResponse>, // children
            val after: String?,
            val before: String?
    )*/
    class ListingData(
        val data: List<WebClientPostCheckListSearch>
    )

    //data class RedditChildrenResponse(val data: WebClientPostCheckListSearch)

    companion object {
        private var BASE_URL = TalkMobileApplication.DEFAULT_WEBHTTPURL
        fun create(): GCheckListSearchWebClientApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): GCheckListSearchWebClientApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(GCheckListSearchWebClientApi::class.java)
        }
    }
}