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

package com.example.githubsearchuser2.paginglib.vo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "posts",
        indices = [Index(value = ["subreddit"], unique = false)])

data class WebClientPostCheckListSearch (
        @PrimaryKey
        @SerializedName("id")
        var rnumindex: String,
        @SerializedName("login")
        var login: String,
        @SerializedName("node_id")
        var node_id: String,
        @SerializedName("avatar_url")
        var avatar_url: String,
        @SerializedName("url")
        var url: String,
        @SerializedName("html_url")
        var html_url: String,
        @SerializedName("followers_url")
        var followers_url: String,
        @SerializedName("following_url")
        var following_url: String,
        @SerializedName("gists_url")
        var gists_url: String,
        @SerializedName("starred_url")
        var starred_url: String,
        @SerializedName("subscriptions_url")
        var subscriptions_url: String,
        @SerializedName("organizations_url")
        var organizations_url: String,
        @SerializedName("repos_url")
        var repos_url: String,
        @SerializedName("events_url")
        var events_url: String,
        @SerializedName("received_events_url")
        var received_events_url: String,
        @SerializedName("type")
        var type: String,
        @SerializedName("score")
        var score: String,

        var bCheckFlag: Boolean = false
)
