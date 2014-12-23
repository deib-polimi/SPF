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
package it.polimi.spf.lib.notification;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import it.polimi.spf.lib.Component;

import it.polimi.spf.shared.SPFInfo;
import it.polimi.spf.shared.aidl.SPFNotificationService;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFTrigger;

/**
 * TODO comment
 * 
 * @author Jacopo Aliprandi
 * 
 */
public class SPFNotification extends Component<SPFNotification, SPFNotificationService> {

	protected SPFNotification(Context context, SPFNotificationService serviceInterface, ServiceConnection connection, Component.ConnectionCallback<SPFNotification> callback) {
		super(context, serviceInterface, connection, callback);
	}

	// descriptor
	private static final Descriptor<SPFNotification, SPFNotificationService> DESCRIPTOR = new Descriptor<SPFNotification, SPFNotificationService>() {

		@Override
		public String getActionName() {
			return SPFInfo.ACTION_NOTIFICATION;
		}

		@Override
		public SPFNotificationService castInterface(IBinder binder) {
			return SPFNotificationService.Stub.asInterface(binder);
		}

		@Override
		public SPFNotification createInstance(Context context, SPFNotificationService serviceInterface, ServiceConnection connection, Component.ConnectionCallback<SPFNotification> callback) {
			return new SPFNotification(context, serviceInterface, connection, callback);
		}
	};

	public static void load(Context context, Callback callback) {
		Component.load(context, DESCRIPTOR, callback);
	}

	public interface Callback extends ConnectionCallback<SPFNotification>{
	}

	/*
	 * 
	 * FIXME callback should run in the main thread after the method
	 * execution... ...concurrency problem in the client apps
	 */

	public List<SPFTrigger> listTrigger() {
		try {
			SPFError err = new SPFError(SPFError.NONE_ERROR_CODE);
			List<SPFTrigger> trgs = getService().listTrigger(getAccessToken(), err);
			if (!err.codeEquals(SPFError.NONE_ERROR_CODE)) {
				handleError(err);
			}
			return trgs;
		} catch (RemoteException e) {
			catchRemoteException(e);
			return new ArrayList<SPFTrigger>(0);
		}
	}

	public SPFTrigger getTrigger(long triggerId) {
		try {
			SPFError err = new SPFError(SPFError.NONE_ERROR_CODE);
			SPFTrigger trg = getService().getTrigger(triggerId, getAccessToken(), err);
			if (!err.codeEquals(SPFError.NONE_ERROR_CODE)) {
				handleError(err);
			}
			return trg;
		} catch (RemoteException e) {
			catchRemoteException(e);
			return null;
		}
	}

	public boolean saveTrigger(SPFTrigger trigger) {
		long newId = trigger.getId();
		try {
			SPFError err = new SPFError(SPFError.NONE_ERROR_CODE);
			newId = getService().saveTrigger(trigger, getAccessToken(), err);
			if (!err.codeEquals(SPFError.NONE_ERROR_CODE)) {
				handleError(err);
			} else if (newId != -1) {
				trigger.setId(newId);
				return true;
			}
		} catch (RemoteException e) {
			catchRemoteException(e);
		}
		return false;
	}

	public boolean deleteTrigger(long triggerId) {
		try {
			SPFError err = new SPFError(SPFError.NONE_ERROR_CODE);
			boolean result = getService().deleteTrigger(triggerId, getAccessToken(), err);
			if (!err.codeEquals(SPFError.NONE_ERROR_CODE)) {
				handleError(err);
			}
			return result;
		} catch (RemoteException e) {
			catchRemoteException(e);
			return false;
		}
	}

	public boolean deleteAllTrigger() {
		try {
			SPFError err = new SPFError(SPFError.NONE_ERROR_CODE);
			boolean res = getService().deleteAllTrigger(getAccessToken(), err);
			if (!err.codeEquals(SPFError.NONE_ERROR_CODE)) {
				handleError(err);
			}
			return res;
		} catch (RemoteException e) {
			catchRemoteException(e);
			return false;
		}
	}

	/**
	 * 
	 */
	private void catchRemoteException(RemoteException e) {
		disconnect();
		getCallback().onDisconnect();
	}

}
