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
package it.polimi.spf.framework.services;

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Implementation of {@link BroadcastReceiver} that listents for intents with
 * action {@link Intent#ACTION_PACKAGE_ADDED} and
 * {@link Intent#ACTION_PACKAGE_REMOVED}.<br>
 * At the moment, when a new application is installed nothing is done. On the
 * other hand, when an application is uninstalled, the recevier checks if such
 * application was registered in SPF; if so, the app is automatically
 * unregistered.
 * 
 * @author darioarchetti
 * 
 */
public class PackageChangeReceiver extends BroadcastReceiver {

	private static final String TAG = "PackageChangeReceiver";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		SPFSecurityMonitor securityMonitor = SPF.get().getSecurityMonitor();
		String packageIdentifier = intent.getData().getSchemeSpecificPart();

		if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
			// TODO Maybe we can check if the app wants to use SPF?
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			if(intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)){
				Log.d(TAG, "App " + packageIdentifier + " is being updated - do not remove");
				return;
			}
			
			if (securityMonitor.isAppRegistered(packageIdentifier)) {
				securityMonitor.unregisterApplication(packageIdentifier);
				Log.d(TAG, "App " + packageIdentifier + " uninstalled from SPF");
			} else {
				Log.d(TAG, "App " + packageIdentifier + " was not installed in SPF");
			}
		}
	}
}
