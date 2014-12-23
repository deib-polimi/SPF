/* 
 * Copyright 2014 Jacopo Aliprandi, Dario Archetti
 * 
 * This file is part of SPF.
 * 
 * SPF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * SPF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SPF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package it.polimi.spf.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import it.polimi.spf.app.fragments.profile.ProfileViewActivity;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.notification.NotificationMessage;

/**
 * Component to send system {@link Notification} upon reception of specific
 * events from the {@link SPFApp} event broadcaster. Events are sent only if the
 * manager is turned on.
 * 
 * @author darioarchetti
 * 
 */
public class SystemNotificationManager implements SPFContext.OnEventListener {

	public static final int NOTIFICATION_MESSAGE_RECEIVED = 0xff1;
	public static final int NOTIFICATION_CONTACT_REQUEST_RECEIVED = 0xff2;

	private boolean mIsOn = false;
	private Context mContext;

	public SystemNotificationManager(Context context, boolean active) {
		this.mContext = context;
		if (active) {
			turnOn();
		}
	}

	public void turnOn() {
		if (!mIsOn) {
			SPFContext.get().registerEventListener(this);
			mIsOn = true;
		}
	}

	public void turnOff() {
		if (mIsOn) {
			SPFContext.get().unregisterEventListener(this);
			mIsOn = false;
		}
	}

	@Override
	public void onEvent(int eventCode, Bundle payload) {
		switch (eventCode) {
		case SPFContext.EVENT_NOTIFICATION_MESSAGE_RECEIVED:
			onNotificationMessageReceived(payload);
			break;
		case SPFContext.EVENT_CONTACT_REQUEST_RECEIVED:
			onContactRequestReceived(payload);
			break;
		}
	}

	private void onContactRequestReceived(Bundle payload) {
		// TODO implement notification for contact request
	}

	private void onNotificationMessageReceived(Bundle payload) {
		NotificationMessage message = payload.getParcelable(SPFContext.EXTRA_NOTIFICATION_MESSAGE);
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// Create intent to show sender profile
		Intent i = ProfileViewActivity.getIntent(mContext, message.getSenderId());
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent intent = PendingIntent.getActivity(mContext, 0, i, 0);

		//@formatter:off
		Notification n = new Notification.Builder(mContext)
			.setAutoCancel(true)
			.setContentIntent(intent)
			.setContentTitle(message.getTitle())
			.setContentText(message.getMessage())
			.setSmallIcon(R.drawable.ic_launcher)
			.setSound(alarmSound)
			.build();
		//@formatter:on

		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_MESSAGE_RECEIVED, n);
	}
}
