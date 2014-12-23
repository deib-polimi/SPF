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
package it.polimi.spf.shared.model;

import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO #Documentation
 * 
 * @author Jacopo Aliprandi
 */
public abstract class SPFAction implements Parcelable {

	protected static final int TYPE_NOTIFICATION = 0;
	protected static final int TYPE_INTENT = 1;
	protected static final int TYPE_SERVICE_CALL = 2;

	protected static final String KEY_TYPE = "type";

	/**
	 * TODO comment
	 * 
	 * @return
	 */
	public abstract String toJSON();

	/**
	 * TODO comment
	 * 
	 * @return
	 */
	public static SPFAction fromJSON(String json) {
		try {
			JSONObject o = new JSONObject(json);
			int type = o.getInt(KEY_TYPE);
			switch (type) {
			case TYPE_INTENT:
				return new SPFActionIntent(o);
			case TYPE_NOTIFICATION:
				return new SPFActionSendNotification(o);
			default:
				throw new IllegalArgumentException("Unknown action type: " + type);
			}

		} catch (JSONException e) {
			throw new IllegalArgumentException("JSON does not represent a valid SPFAction");
		}
	}

}
