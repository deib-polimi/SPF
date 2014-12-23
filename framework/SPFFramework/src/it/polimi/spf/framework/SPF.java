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
package it.polimi.spf.framework;

import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

import android.content.Context;
import android.util.Log;

import it.polimi.spf.framework.local.SPFService;
import it.polimi.spf.framework.notification.SPFAdvertisingManager;
import it.polimi.spf.framework.notification.SPFNotificationManager;
import it.polimi.spf.framework.people.SPFPeopleManager;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.profile.SPFProfileManager;
import it.polimi.spf.framework.proximity.InboundProximityInterface;
import it.polimi.spf.framework.proximity.InboundProximityInterfaceImpl;
import it.polimi.spf.framework.proximity.ProximityMiddleware;
import it.polimi.spf.framework.search.SPFSearchManager;
import it.polimi.spf.framework.security.AppRegistrationHandler;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import it.polimi.spf.framework.services.SPFServiceRegistry;
import it.polimi.spf.shared.model.BaseInfo;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;

/**
 * 
 *
 */
@SuppressWarnings("unused")
public class SPF {

	private static SPF singleton;
	private static final String TAG = "SPF";

	/**
	 * Initializes SPF with a {@link Context} reference. Called by
	 * {@link SPFContext#initialize(Context)}.
	 * 
	 * @param context
	 *            - the context that will be used by SPF
	 */
	/* package */synchronized static void initialize(Context context, ProximityMiddleware.Factory factory) {
		if (singleton == null) {
			loadAlljoynLibrary();
			singleton = new SPF(context, factory);
		}
	}
	
	private static void loadAlljoynLibrary(){
		if (SPFConfig.IS_X86 && SPFConfig.DEBUG) {
			Log.w(TAG, "Alljoyn library not loaded");
		} else {
			System.loadLibrary("alljoyn_java");
		}
	}

	/**
	 * Obtains a reference to the SPF singleton. Call
	 * {@link SPFContext#initialize(Context)} before calling this method.
	 * 
	 * @return the SPF singleton.
	 */
	public static synchronized SPF get() {
		SPFContext.assertInitialization();
		return singleton;
	}

	private Context mContext;
	private String uniqueIdentifier;

	// External interfaces
	private ProximityMiddleware mMiddleware;
	private SPFService spfService = null;

	// Components
	private SPFSecurityMonitor mSecurityMonitor;
	private SPFPeopleManager mPeopleManager;
	private SPFProfileManager mProfileManager;
	private SPFServiceRegistry mServiceRegistry;
	private SPFSearchManager mSearchManager;
	private SPFNotificationManager mNotificationManager;
	private SPFAdvertisingManager mAdvertiseManager;

	private SPF(Context context, ProximityMiddleware.Factory factory) {
		mContext = context;

		// Initialize components
		mServiceRegistry = new SPFServiceRegistry(context);
		mPeopleManager = new SPFPeopleManager();
		mProfileManager = new SPFProfileManager(context);
		mSearchManager = new SPFSearchManager();
		mNotificationManager = new SPFNotificationManager(context);
		mSecurityMonitor = new SPFSecurityMonitor(context);

		// unique id generation
		ProfileFieldContainer pfc = mProfileManager.getProfileFieldBulk(SPFPersona.getDefault(), ProfileField.IDENTIFIER);
		uniqueIdentifier = pfc.getFieldValue(ProfileField.IDENTIFIER);
		if (uniqueIdentifier == null) {// TODO move out
			uniqueIdentifier = "U" + ((int) (new Random().nextFloat() * 10000));
			pfc.setFieldValue(ProfileField.IDENTIFIER, uniqueIdentifier);
		}
		mProfileManager.setProfileFieldBulk(pfc, SPFPersona.getDefault());

		// Initialize middleware
		InboundProximityInterface proximityInterface = new InboundProximityInterfaceImpl(this);
		mMiddleware = factory.createMiddleware(mContext, proximityInterface, uniqueIdentifier);

		mAdvertiseManager = new SPFAdvertisingManager(context, mMiddleware);
	}

	// Utility getters
	public Context getContext() {
		return mContext;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	// Life-cycle methods
	public void connect() {
		if (!mMiddleware.isConnected()) {
			mMiddleware.connect();
		}

		if (!mNotificationManager.isRunning()) {
			mNotificationManager.start();
		}

		if (mAdvertiseManager.isAdvertisingEnabled()) {
			mMiddleware.registerAdvertisement(mAdvertiseManager.generateAdvProfile().toJSON(), 10000);
		}
	}

	public void disconnect() {
		if (mMiddleware.isAdvertising()) {
			mMiddleware.unregisterAdvertisement();
		}

		if (mMiddleware.isConnected()) {
			mMiddleware.disconnect();
		}

		if (mNotificationManager.isRunning()) {
			mNotificationManager.stop();
		}
		List<String> lostRefs = mPeopleManager.clear();
		for(String ref:lostRefs){
			mSearchManager.onInstanceLost(ref);
		}
		
	}

	public boolean isConnected() {
		return mMiddleware.isConnected();
	}

	// Link for local server service
	public void onServerCreated(SPFService spfService) {
		this.spfService = spfService;
	}

	public void onServerDestroy() {
		spfService = null;
	}

	// Components getters
	public SPFServiceRegistry getServiceRegistry() {
		return mServiceRegistry;
	}

	public SPFProfileManager getProfileManager() {
		return mProfileManager;
	}

	public SPFSecurityMonitor getSecurityMonitor() {
		return mSecurityMonitor;
	}

	public SPFSearchManager getSearchManager() {
		return mSearchManager;
	}

	public SPFNotificationManager getNotificationManager() {
		return mNotificationManager;
	}

	public SPFAdvertisingManager getAdvertiseManager() {
		return mAdvertiseManager;
	}

	public SPFPeopleManager getPeopleManager() {
		return mPeopleManager;
	}

	//TODO this class should handle only components life-cycle move search primitives elsewhere
	public void sendSearchSignal(String queryId, String query) {
		mMiddleware.sendSearchSignal(getUniqueIdentifier(), queryId, query);
	}

	public void sendSearchResult(String queryId, BaseInfo baseInfo) {
		Gson g = new Gson();
		mMiddleware.sendSearchResult(queryId, uniqueIdentifier, g.toJson(baseInfo));
	}
}
