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
package it.polimi.spf.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppDeregistrationReceiver extends BroadcastReceiver {

	private static final Object DEREGISTRATION_INTENT = "it.polimi.spf.framework.AppDeregistered";
	private static final String DEREGISTERED_APP = "appIdentifier";
	private static final String TAG = "AppDreregistrationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!intent.getAction().equals(DEREGISTRATION_INTENT)) {
			return;
		}

		String identifier = intent.getStringExtra(DEREGISTERED_APP);
		if (identifier.equals(Utils.getAppIdentifier(context))) {
			AccessTokenManager.get(context).invalidateToken();
			Log.d(TAG, "App was unregistered from SPF");
		}
	}
}
