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

import it.polimi.spf.alljoyn.AlljoynProximityMiddleware;
import it.polimi.spf.framework.ExceptionLogger;
import it.polimi.spf.framework.R;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.wfdadapter.WFDMiddlewareAdapter;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

/**
 * {@link Application} subclass that serves two main purposes:
 * <ul>
 * <li>Initializing SPF singleton with a context reference</li>
 * <li>Local broadcaster for application-wide events</li>
 * <ul>
 * 
 * @author aliprax
 * 
 */
public class SPFApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Initialize SPF
		SPFContext.initialize(this, AlljoynProximityMiddleware.FACTORY);
		// Use this line to initialize SPF on Wi-Fi Direct
		// SPFContext.initialize(this, WFDMiddlewareAdapter.FACTORY);
		SPFContext.get().setAppRegistrationHandler(new PopupAppRegistrationHandler());
		
		// Set notification to show when SPF service is in foreground
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
		PendingIntent.FLAG_UPDATE_CURRENT);

		Notification n = new Notification.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setTicker("spf is active")
			.setContentTitle("SPF")
			.setContentText("SPF is active.")
			.setContentIntent(pIntent)
			.build();
		
		SPFContext.get().setServiceNotification(n);
		
		// Set exception logger to log uncaught exceptions
		ExceptionLogger.installAsDefault(this);
	}
}
