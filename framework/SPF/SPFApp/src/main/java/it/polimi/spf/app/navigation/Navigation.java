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
package it.polimi.spf.app.navigation;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import it.polimi.spf.app.MainActivity;
import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.SPF;

/**
 * Component that handles the navigation of {@link MainActivity}
 * 
 * @author darioarchetti
 * 
 */
public class Navigation implements SPFContext.OnEventListener {

	public static enum Entry {
		PROFILE, PERSONAS, CONTACTS, NOTIFICATIONS, ADVERTISING, APPS, ACTIVITIES;
	}

	private final Context mContext;
	private final String[] mPageTitles;
	private final Map<Entry, NavigationEntryView> mEntries;

	public Navigation(Context context) {
		this.mContext = context;
		this.mPageTitles = mContext.getResources().getStringArray(R.array.content_fragments_titles);
		this.mEntries = new HashMap<Entry, NavigationEntryView>();
	}

	public View createEntryView(Entry entry) {
		NavigationEntryView entryView = new NavigationEntryView(mContext);
		entryView.setName(mPageTitles[entry.ordinal()]);
		updateViewNotification(entry, entryView);
		mEntries.put(entry, entryView);
		return entryView;
	}

	@Override
	public void onEvent(int eventCode, Bundle payload) {
		Navigation.Entry entry;

		switch (eventCode) {
		case SPFContext.EVENT_ADVERTISING_STATE_CHANGED: {
			entry = Entry.ADVERTISING;
			break;
		}
		case SPFContext.EVENT_NOTIFICATION_MESSAGE_RECEIVED: {
			entry = Entry.NOTIFICATIONS;
			break;
		}
		case SPFContext.EVENT_CONTACT_REQUEST_RECEIVED: {
			entry = Entry.CONTACTS;
			break;
		}
		default:
			return;
		}

		NavigationEntryView entryView = mEntries.get(entry);
		updateViewNotification(entry, entryView);
	}

	private void updateViewNotification(Navigation.Entry entry, NavigationEntryView entryView) {
		switch (entry) {
		case NOTIFICATIONS:
			int notifCount = SPF.get().getNotificationManager().getAvailableNotificationCount();
			showNotificationOrClear(entryView, notifCount > 0 ? String.valueOf(notifCount) : null);
			break;
		case CONTACTS:
			int msgCount = SPF.get().getSecurityMonitor().getPersonRegistry().getPendingRequestCount();
			showNotificationOrClear(entryView, msgCount > 0 ? String.valueOf(msgCount) : null);
			break;
		case ADVERTISING:
			boolean active = SPF.get().getAdvertiseManager().isAdvertisingEnabled();
			showNotificationOrClear(entryView, active ? "ON" : null);
			break;
		default:
			break;
		}
	}

	private void showNotificationOrClear(NavigationEntryView entry, String text) {
		if (entry == null) {
			return;
		}

		if (text == null) {
			entry.clearNotification();
		} else {
			entry.showNotification(text);
		}
	}
}
