package com.example.githubsearchuser2

import android.content.Context
import android.content.Intent

class CommonTLibEx {
    // CheckList SearchUserList 범위지정 갱신
    fun CheckListSearchUserListRangeChangedReceiver(mContext: Context, count: Int) {
        val intent = Intent(NexBroadcastReceiver.CHECKLIST_SEARCHUSERLISTRANGECHANGE_REFRESH)
        intent.putExtra("count", count)
        mContext.sendBroadcast(intent)
    }
    // CheckList Header 화면에 전체갱신
    fun CheckListAllRangeChangedReceiver(mContext: Context) {
        val intent = Intent(NexBroadcastReceiver.CHECKLIST_ALLRANGECHANGE_REFRESH)
        mContext.sendBroadcast(intent)
    }
    // GitHubSearchUser List Refresh 화면에 갱신
    fun GitHubSearchUserListRefreshReceiver(mContext: Context, position: Int) {
        val intent = Intent(NexBroadcastReceiver.GITHUBSEARCHUSERLIST_REFRESH)
        intent.putExtra("position", position)
        mContext.sendBroadcast(intent)
    }
    // CheckList Header 화면에 갱신2
    fun CheckListNotifyItemRemoveReceiver(mContext: Context, position: Int) {
        val intent = Intent(NexBroadcastReceiver.CHECKLIST_NOTIFYITEMREMOVE_REFRESH)
        intent.putExtra("position", position)
        mContext.sendBroadcast(intent)
    }
    companion object {
        private val TAG = CommonTLibEx::class.java.simpleName
    }
}