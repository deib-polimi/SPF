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

import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFProfileManager;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.proximity.ProximityMiddleware;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

public class SPFAdvertisingManager {

	private final static String PREF_FILE = "advertising";
	private final static String PREF_ADVERTISED_FIELD_NAME = "advertisedFields";
	private final static String PREF_ADVERTISING_ACTIVE = "advertisingActive";
	private final static String PREF_PERSONA_TO_ADV = "personaToAdv";
	private static final String SEPARATOR = ";";

	private final Context mContext;
	private final ProximityMiddleware mMiddleware;
	private final Set<String> mIdentifiers;
	private SPFPersona mPersonaToAdv;
	private boolean mAdvertisingEnabled;

	public SPFAdvertisingManager(Context context, ProximityMiddleware middleware) {
		this.mContext = context;
		this.mMiddleware = middleware;
		this.mIdentifiers = new HashSet<String>();
		//set up profile fields
		SharedPreferences prefs = getSharedPreferences();
		String pref = prefs.getString(PREF_ADVERTISED_FIELD_NAME, null);
		if (pref == null) {
			return;
		}
		String[] fields = TextUtils.split(pref, SEPARATOR);
		for (String s : fields) {
			mIdentifiers.add(s);
		}
		//set up advertising status
		mAdvertisingEnabled = prefs.getBoolean(PREF_ADVERTISING_ACTIVE, false);
		//set spf persona to be advertised
		final String defaultPersonaName = SPFPersona.getDefault().getIdentifier();
		final String personaToAdvName = prefs.getString(PREF_PERSONA_TO_ADV, 
				defaultPersonaName);
		mPersonaToAdv = new SPFPersona(personaToAdvName);
	}
	
	public void setPersonaToAdvertise(SPFPersona persona){
		final List<SPFPersona> availablePersonas = SPF.get().getProfileManager().getAvailablePersonas();
		if(persona==null||!availablePersonas.contains(persona)){
			if(!availablePersonas.contains(mPersonaToAdv)){
				persona = SPFPersona.getDefault();
			}else{
				return;
			}
		}
		mPersonaToAdv = persona;
		refreshPersonaPreferences();
	}

	public void addFieldToAdvertising(ProfileField<?> field) {
		if (field == null) {
			throw new NullPointerException();
		}

		String identifier = field.getIdentifier();
		if (mIdentifiers.add(identifier)) {
			refreshProfileFieldsPreferences();
		}
	}

	public void removeFieldFromAdvertising(ProfileField<?> field) {
		if (field == null) {
			throw new NullPointerException();
		}

		String identifier = field.getIdentifier();
		if (mIdentifiers.remove(identifier)) {
			refreshProfileFieldsPreferences();
		}
	}

	public boolean isAdvertisingEnabled() {
		return mAdvertisingEnabled;
	}

	public boolean isAdvertising() {
		return mMiddleware.isAdvertising();
	}

	public void registerAdvertising() {
		setAdvertisingEnabled(true);
		sendEvent(true);

		if (mMiddleware.isConnected()) {
			mMiddleware.registerAdvertisement(generateAdvProfile().toJSON(), 10000);
		}
	}

	public void unregisterAdvertising() {
		setAdvertisingEnabled(false);
		sendEvent(false);

		if (mMiddleware.isConnected()) {
			mMiddleware.unregisterAdvertisement();
		}
	}

	public List<String> getFieldIdentifiers() {
		return new ArrayList<String>(mIdentifiers);
	}
	
	public SPFPersona getPersonaToAdvertise(){
		return mPersonaToAdv;
	}

	public SPFAdvProfile generateAdvProfile() {
		SPFAdvProfile profile = new SPFAdvProfile();
		SPFProfileManager ps = SPF.get().getProfileManager();
		// the uniqueIdentifier is mandatory
		if (!mIdentifiers.contains(ProfileField.IDENTIFIER.getIdentifier())) {
			mIdentifiers.add(ProfileField.IDENTIFIER.getIdentifier());
		}
		
		SPFPersona persona = mPersonaToAdv;
		ProfileFieldContainer pfc = ps.getProfileFieldBulk(mIdentifiers.toArray(new String[0]), persona);
		for (String k : mIdentifiers) {
			String value = pfc.getFieldValue(k);
			if (value != null) {
				profile.put(k, value);
			} // TODO support collection
		}

		return profile;
	}

	private SharedPreferences getSharedPreferences() {
		return mContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
	}

	private void refreshProfileFieldsPreferences() {
		getSharedPreferences().edit().putString(PREF_ADVERTISED_FIELD_NAME, TextUtils.join(SEPARATOR, mIdentifiers)).apply();
	}
	
	private void refreshPersonaPreferences(){
		getSharedPreferences().edit().putString(PREF_PERSONA_TO_ADV, mPersonaToAdv.getIdentifier());
	}

	private void setAdvertisingEnabled(boolean active) {
		mAdvertisingEnabled = active;
		getSharedPreferences().edit().putBoolean(PREF_ADVERTISING_ACTIVE, active).apply();
	}

	private void sendEvent(boolean active) {
		Bundle b = new Bundle();
		b.putBoolean(SPFContext.EXTRA_ACTIVE, active);
		SPFContext.get().broadcastEvent(SPFContext.EVENT_ADVERTISING_STATE_CHANGED, b);
	}
}