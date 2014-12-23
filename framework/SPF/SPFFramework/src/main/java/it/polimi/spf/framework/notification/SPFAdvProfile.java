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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SPFAdvProfile {

	private static final String TAG = "SPFAdvPRofile";
	private Map<String, String> fields;

	public static SPFAdvProfile fromJSON(String advProfileJSON) {
		SPFAdvProfile profile = new SPFAdvProfile();
		try {
			JSONObject obj = new JSONObject(advProfileJSON);
			Iterator<String> it = obj.keys();

			while (it.hasNext()) {
				String key = it.next();
				profile.put(key, obj.getString(key));
			}

		} catch (JSONException e) {
			Log.e(TAG, "JSON error", e);
		}

		return profile;
	}

	public SPFAdvProfile() {
		this.fields = new HashMap<String, String>();
	}

	public void put(String key, String value) {
		if (key == null || value == null) {
			throw new NullPointerException();
		}

		fields.put(key, value);
	}

	public String getField(String key) {
		if (key == null) {
			throw new NullPointerException();
		}
		
		return fields.get(key);
	}
	
	public Set<String> getFieldKeySet(){
		return fields.keySet();
	}

	public String toJSON() {
		JSONObject o = new JSONObject();

		for (String k : fields.keySet()) {
			try {
				o.put(k, fields.get(k));
			} catch (JSONException e) {
				// This should not happen as all the fields are strings
			}
		}

		return o.toString();
	}

	public Collection<String> getFieldsValues() {
		
		return fields.values();
	}

}
