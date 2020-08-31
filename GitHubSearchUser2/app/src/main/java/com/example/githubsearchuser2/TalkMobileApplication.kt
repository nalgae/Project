package com.example.githubsearchuser2

import android.app.Application
import android.os.*


open class TalkMobileApplication : Application() {
    companion object {

        val release = true
        val market = false

        private val TAG = "githubsearchuser2"

        val profilePath = Environment.getExternalStorageDirectory().path + "/githubsearchuser2" // 외부 저장소의 최상위 경로를 반환합니다.
        val downloadfilePath = Environment.getExternalStorageDirectory().path + "/githubsearchuser2/Download"

        val sharedPrefsPath = "/data/data/com.example.githubsearchuser2/shared_prefs/"
        val sharedPrefsFile = "com.example.githubsearchuser2_preferences"

        var DEFAULT_WEBHTTPURL = "https://api.github.com"
        var strMY_WEBID = ""
    }

}
