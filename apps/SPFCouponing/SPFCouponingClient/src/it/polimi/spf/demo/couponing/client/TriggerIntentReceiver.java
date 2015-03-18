package it.polimi.spf.demo.couponing.client;

import java.util.Map;

import it.polimi.spf.shared.model.SPFActionIntent;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TriggerIntentReceiver extends BroadcastReceiver {

	private static final String PREF_FILE = "trigger_cache";
	private static final String TAG = "TriggerIntentReceiver";
	private static final String PERSON_IDENTIFIER_KEY = "personIdentifier";
	private static final String SHOW_PROFILE_ACTION = "it.polimi.spf.app.ShowProfile";

	@Override
	public void onReceive(Context context, final Intent intent) {
		Log.d(TAG, "Intent received");
		Bundle data = intent.getExtras();
		String dn = data.getString(SPFActionIntent.ARG_STRING_DISPLAY_NAME);
		String id = data.getString(SPFActionIntent.ARG_STRING_TARGET);
		long triggerId = data.getLong(SPFActionIntent.ARG_LONG_TRIGGER_ID);
		
		Map<String, ?> prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getAll();
		String category = null;
		for(Map.Entry<String, ?> entry : prefs.entrySet()){
			if(((Long) entry.getValue()).equals(triggerId)){
				category = entry.getKey();
				break;
			}
		}
		
		if(category == null){
			Log.e(TAG, "Category for trigger ID " + triggerId + " not found");
			return;
		}
		
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
	    	.setSmallIcon(R.drawable.ic_launcher)
	    	.setContentTitle(dn + " is nearby")
	    	.setContentText("This shop is selling " + category)
	    	.setAutoCancel(true);

    	Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	mBuilder.setSound(alarmSound);
    	
    	Intent showProfile = new Intent();
    	showProfile.setAction(SHOW_PROFILE_ACTION);
    	showProfile.putExtra(PERSON_IDENTIFIER_KEY, id);
    	
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, showProfile, PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(pendingIntent);

    	int mNotificationId = 0xabc2;
    	NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.notify(mNotificationId, mBuilder.build());	
	}
}
