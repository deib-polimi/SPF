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
package it.polimi.spf.framework.notification;

import java.util.List;

import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.SPFTrigger;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * @author Jacopo Aliprandi
 * 
 *         {@link SPFNotificationManager} is the component that manages the
 *         SPFNotification services.
 */
public class SPFNotificationManager implements SPFActionPerformer {

	private final Context mContext;
	private SPFTriggerTable mTriggerTable;
	private SPFActionCache mTriggerCache;
	private SPFTriggerEngine mTriggerEngine;

	private boolean isRunning = false;

	// thread and handler resources
	private SPFNotificationHandler mHandler;
	private HandlerThread mHandlerThread;

	// Notification messages
	private NotificationMessageTable mMessageTable;

	/**
	 * 
	 */
	public SPFNotificationManager(Context c) {
		mContext = c;
		mTriggerEngine = new SPFTriggerEngine(this);
		mTriggerTable = new SPFTriggerTable(mContext);
		mMessageTable = new NotificationMessageTable(mContext);
	}

	/**
	 * 
	 * 
	 * @param actionPerformer
	 */
	public SPFNotificationManager(Context c, SPFActionPerformer actionPerformer) {
		mContext = c;
		mTriggerEngine = new SPFTriggerEngine(actionPerformer);
		mTriggerTable = new SPFTriggerTable(mContext);
		mMessageTable = new NotificationMessageTable(mContext);
	}

	/**
	 * Finds a trigger by id.
	 * 
	 * @param triggerId
	 * @param appPackageName
	 * @return
	 */
	public SPFTrigger getTrigger(long triggerId, String appPackageName) {

		return mTriggerTable.getTrigger(triggerId, appPackageName);
	}

	/**
	 * Saves the trigger on the database.
	 * 
	 * @param t
	 */
	public long saveTrigger(SPFTrigger trigger, String appPackageName) {
		trigger = mTriggerTable.saveTrigger(trigger, appPackageName);
		if (trigger != null) {
			if (mHandler != null)
				mHandler.postAddTrigger(trigger);
			return trigger.getId();
		} else {
			return -1;
		}
	}

	/**
	 * Deletes the trigger specified by its id.
	 * 
	 * @param id
	 * @return true if the trigger has been deleted
	 */
	public boolean deleteTrigger(long id, String appPackageName) {

		boolean success = mTriggerTable.deleteTrigger(id, appPackageName);
		if (success) {
			if (mHandler != null)
				mHandler.postRemoveTrigger(id);
		}
		return success;
	}

	/**
	 * Returns a list of triggers saved by the application specified with its
	 * package name.
	 * 
	 * @param appIdentifier
	 * @return a list of triggers
	 */
	public List<SPFTrigger> listTriggers(String appPackageName) {

		return mTriggerTable.getAllTriggers(appPackageName);
	}

	/**
	 * Delete all the trigger of the application specified by mean of its
	 * package name.
	 * 
	 * @return
	 */
	public boolean deleteAllTrigger(String appPackageName) {

		return mTriggerTable.deleteAllTriggerOf(appPackageName);

	}

	private class SPFNotificationHandler extends Handler {

		public static final int ACTION_SETUP = 1;
		public static final int ACTION_STOP = 2;
		public static final int ACTION_ANALYZE = 3;
		public static final int ACTION_ADD_TRIGGER = 4;
		public static final int ACTION_REMOVE_TRIGGER = 5;

		public SPFNotificationHandler(Looper looper) {
			super(looper);

		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ACTION_ANALYZE:
				mTriggerEngine.lookForMatchingTrigger((SPFAdvProfile) msg.obj);
				break;
			case ACTION_ADD_TRIGGER:
				mTriggerEngine.put((SPFTrigger) msg.obj);
				break;
			case ACTION_REMOVE_TRIGGER:
				mTriggerEngine.remove((Long) msg.obj);
				break;
			case ACTION_SETUP:
				SPFNotificationManager.this.setup();
				break;
			case ACTION_STOP:
				this.getLooper().quit();// change to quitSafely() if api level >
										// 18
			default:
				super.handleMessage(msg);
			}
		}

		void postStop() {
			sendEmptyMessage(ACTION_STOP);
		}

		void postSetup(SPFNotificationManager setup) {
			Message msg = obtainMessage(ACTION_SETUP, setup);
			sendMessage(msg);
		}

		void postAnalyzeAdvertisement(SPFAdvProfile advProfile) {
			Message msg = obtainMessage(ACTION_ANALYZE);
			msg.obj = advProfile;
			sendMessage(msg);
		}

		void postAddTrigger(SPFTrigger trigger) {
			Message msg = mHandler.obtainMessage(SPFNotificationHandler.ACTION_ADD_TRIGGER, trigger);
			sendMessage(msg);
		}

		void postRemoveTrigger(long id) {
			Message msg = obtainMessage(ACTION_REMOVE_TRIGGER, Long.valueOf(id));
			sendMessage(msg);
		}
	}

	/**
	 * Release all the resources used by SPFNotification service.
	 */
	public void stop() {
		isRunning = false;
		mHandler.postStop();
	}

	/**
	 * Return true if the logic that handles triggers processing is running.
	 * This value becomes true after a call to
	 * {@link SPFNotificationManager#start()} and returns false after
	 * {@link SPFNotificationManager#stop()} has been called.
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Initialize and start all the threads and resources used by
	 * SPFNotification service. Call this method to initialize the logic that
	 * handles triggers processing.
	 */
	public void start() {
		this.mHandlerThread = new HandlerThread("notification-handler-thread");
		this.mHandlerThread.start();
		this.mHandler = new SPFNotificationHandler(mHandlerThread.getLooper());
		mHandler.postSetup(this);
		isRunning = true;
	}

	// to be called after on start: initializes the triggers' logic
	private void setup() {
		List<SPFTrigger> triggers = mTriggerTable.getAllTriggers();
		mTriggerCache = new SPFActionCache(mContext);
		mTriggerEngine.refreshTriggers(triggers);
		mTriggerCache.refresh(triggers);
	}

	/**
	 * Look for a trigger match on the given profile.
	 * 
	 * @param advProfile
	 */
	public void onAdvertisementReceived(SPFAdvProfile advProfile) {
		if (mHandler != null) {
			mHandler.postAnalyzeAdvertisement(advProfile);
		}
	}

	@Override
	public void perform(SPFAdvProfile target, SPFTrigger trigger) {
		final String targetId = target.getField(ProfileField.IDENTIFIER.getIdentifier());
		final long triggerId = trigger.getId();
		if (!mTriggerCache.triggerIsSleepingOnTarget(targetId, triggerId)) {
			new SPFActionPerformerDelegate().perform(target, trigger);
			mTriggerCache.add(targetId, trigger);
		}

	}

	// Notification messages
	public List<NotificationMessage> getAvailableNotifications() {
		return mMessageTable.getAvailableNotifications();
	}

	public boolean deleteNotification(long id) {
		return mMessageTable.deleteNotification(id);
	}

	public boolean deleteAllNotifications() {
		return mMessageTable.deleteAllNotifications();
	}

	public void onNotificationMessageReceived(NotificationMessage message) {
		mMessageTable.saveNotification(message);
		Bundle b = new Bundle();
		b.putParcelable(SPFContext.EXTRA_NOTIFICATION_MESSAGE, message);
		SPFContext.get().broadcastEvent(SPFContext.EVENT_NOTIFICATION_MESSAGE_RECEIVED, b);
	}

	public int getAvailableNotificationCount() {
		return mMessageTable.getAvailableNotificationCount();
	}
}
