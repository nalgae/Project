package com.example.githubsearchuser2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NexBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // not use
    }

    companion object {
        private const val TAG = "NexBroadcastReceiver"

        /** Broadcast intent type.  */
        const val CHECKLIST_SEARCHUSERLISTRANGECHANGE_REFRESH = "CheckListSearchUserListRangeChangedRefresh"
        const val CHECKLIST_ALLRANGECHANGE_REFRESH = "CheckListAllRangeChangedRefresh"
        const val CHECKLIST_NOTIFYITEMREMOVE_REFRESH = "CheckListNotifyItemRemoveRefresh"
        const val GITHUBSEARCHUSERLIST_NOTIFYITEMRANGECHANGE_REFRESH = "GitHubSearchUserListRangeChangedRefresh"
        const val GITHUBSEARCHUSERLIST_REFRESH = "GitHubSearchUserListRefreshReceiver"
        const val CHECKLIST_SEARCH_AUTORUN_REFRESH = "CheckListSearchAutoRunRefresh"
    }
}