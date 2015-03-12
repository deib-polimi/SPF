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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

public class SPFAdvProfile {

	private Map<String, String> fields;
	private List<String> applications;

	public static SPFAdvProfile fromJSON(String advProfileJSON) {
		return new Gson().fromJson(advProfileJSON, SPFAdvProfile.class);
	}

	public SPFAdvProfile() {
		this.fields = new HashMap<String, String>();
		this.applications = new ArrayList<String>();
	}

	public void putField(String key, String value) {
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

	public Set<String> getFieldKeySet() {
		return fields.keySet();
	}

	public Collection<String> getFieldsValues() {
		return fields.values();
	}

	public void putApplication(String identifier) {
		if (identifier == null) {
			throw new NullPointerException();
		}

		applications.add(identifier);
	}

	public Collection<String> getApplications() {
		return applications;
	}

	public String toJSON() {
		return new Gson().toJson(this);
	}
}
