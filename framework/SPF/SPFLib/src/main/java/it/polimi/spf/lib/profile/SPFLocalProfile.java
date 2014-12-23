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
package it.polimi.spf.lib.profile;

import it.polimi.spf.lib.Component;

import it.polimi.spf.shared.SPFInfo;
import it.polimi.spf.shared.aidl.LocalProfileService;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFError;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Local component that provides access to the local user profile, allowing read
 * and write operations.
 * 
 * @author darioarchetti
 * 
 */
public class SPFLocalProfile extends Component<SPFLocalProfile, LocalProfileService> {

	private static final String TAG = "LocalProfile";
	private static final Component.Descriptor<SPFLocalProfile, LocalProfileService> DESCRIPTOR = new Component.Descriptor<SPFLocalProfile, LocalProfileService>() {

		@Override
		public String getActionName() {
			return SPFInfo.ACTION_PROFILE;
		}

		@Override
		public LocalProfileService castInterface(IBinder binder) {
			return LocalProfileService.Stub.asInterface(binder);
		}

		@Override
		public SPFLocalProfile createInstance(Context context, LocalProfileService serviceInterface, ServiceConnection connection, Component.ConnectionCallback<SPFLocalProfile> callback) {
			return new SPFLocalProfile(context, serviceInterface, connection, callback);
		}

	};

	/**
	 * Loads the local profile connection asynchronously. The operations to
	 * perform once the connection is ready should be placed into an
	 * implementation of the {@link SPFLocalProfile.Callback} interface.
	 * 
	 * @param context
	 *            - the Android {@link Context} to use.
	 * @param callback
	 *            - the callback to execute when the connection is loaded or an
	 *            error occurs.
	 */
	public static void load(final Context context, final Callback callback) {
		Component.load(context, DESCRIPTOR, callback);
	}

	private SPFLocalProfile(Context context, LocalProfileService serviceInterface, ServiceConnection connection, ConnectionCallback<SPFLocalProfile> callback) {
		super(context, serviceInterface, connection, callback);
	}

	public ProfileFieldContainer getValueBulk(ProfileField<?>... fields) {
		if (fields == null) {
			throw new NullPointerException();
		}

		SPFError err = new SPFError();
		try {
			ProfileFieldContainer pfc = getService().getValueBulk(getAccessToken(), ProfileField.toIdentifierList(fields), err);
			if (err.isOk()) {
				return pfc;
			}
		} catch (RemoteException e) {
			onRemoteException(e);
			err.setCode(SPFError.REMOTE_EXC_ERROR_CODE);
		}
		handleError(err);
		Log.e(TAG, "Remote exception @ setValueBulk");
		return null;
	}

	public boolean setValueBulk(ProfileFieldContainer container) {
		if (container == null) {
			throw new NullPointerException();
		}
		SPFError err = new SPFError();
		try {
			getService().setValueBulk(getAccessToken(), container, err);
			if (err.isOk()) {
				return true;
			}
		} catch (RemoteException e) {
			onRemoteException(e);
			err.setCode(SPFError.REMOTE_EXC_ERROR_CODE);
		}
		handleError(err);
		return false;
	}

	private void onRemoteException(RemoteException e) {
		Log.e(TAG, "Remote exception @ setValue", e);
		getCallback().onError(new SPFError(SPFError.REMOTE_EXC_ERROR_CODE));
	}

	/**
	 * Interface of the callback to execute when the connection to the profile
	 * is ready, or an error has occurred.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public interface Callback extends ConnectionCallback<SPFLocalProfile> {

	}
}