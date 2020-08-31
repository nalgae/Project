package com.example.githubsearchuser2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NexBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.action
        //		Log.e(TAG, "onReceive : " + intentAction.toString()); // xxx

/*		if (intentAction.equals(TALK_CONNECTION_CLOSED)) {
		    CharSequence message = intent.getCharSequenceExtra("message");
//			Log.d(TAG, "onReceive message : " + message.toString()); // xxx

		} else if (intentAction.equals(TALK_CONNECTION_RESTART)) { // R20140808A RECONNECT
//	    	context.startService(new Intent(context, NexService.class));

		} else if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
		    if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
//		    	context.stopService(new Intent(context, NexService.class));
		    }
		}*/
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