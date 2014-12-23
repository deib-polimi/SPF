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
package it.polimi.spf.framework.security;

import android.content.Context;
import android.util.Log;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.aidl.SPFAppRegistrationCallback;
import it.polimi.spf.shared.model.AppDescriptor;

/**
 * Strategy to handle the registration requests coming from external apps, to be
 * plugged into {@link SPF}
 * 
 * @author darioarchetti
 * @see SPF#setAppRegistrationHandler(AppRegistrationHandler)
 */
public interface AppRegistrationHandler {

	/**
	 * Callback to notify the framework of the outcome of the registration
	 * procedure.
	 * 
	 * @author darioarchetti
	 * @see #onRequestAccepted()
	 * @see #onRequestRefused()
	 */
	public static interface Callback {
		/**
		 * Called when the request is refused
		 * 
		 * @param persona
		 *            - the persona that is to be assigned to the registered app
		 */
		void onRequestAccepted(SPFPersona persona);

		/**
		 * Called when the request is accepted
		 */
		void onRequestRefused();
	}

	/**
	 * Handle a registration request coming from the application described by
	 * the given {@link AppDescriptor} and notify the outcome of the
	 * registration to the given {@link SPFAppRegistrationCallback}. The call to
	 * this method is performed on the main thread.
	 * 
	 * @param context
	 *            - reference to the application context
	 * @param descriptor
	 *            - the descriptor of the app that wants to register
	 * @param callback
	 *            - the callback to notify of the outcome
	 */
	public void handleRegistrationRequest(Context context, AppDescriptor descriptor, AppRegistrationHandler.Callback callback);

	/**
	 * Default implementation of {@link AppRegistrationHandler} that
	 * automatically discards all requests.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public static class Default implements AppRegistrationHandler {

		private final static String TAG = "DefaultAppRegistrationHandler";

		/*
		 * (non-Javadoc)
		 * 
		 * @see it.polimi.spf.framework.security.AppRegistrationHandler#
		 * handleRegistrationRequest(it.polimi.spf.shared.model.AppDescriptor,
		 * it.polimi.spf.shared.aidl.SPFAppRegistrationCallback)
		 */
		@Override
		public void handleRegistrationRequest(Context context, AppDescriptor descriptor, AppRegistrationHandler.Callback callback) {
			Log.v(TAG, "Request from" + descriptor.getAppName() + " refused by default handler");
			callback.onRequestRefused();
		}
	}
}
