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
package it.polimi.spf.framework.proximity;

import it.polimi.spf.shared.model.ProfileFieldContainer;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class FieldContainerMarshaller {

	private static final String TAG = "FieldContainerMarshaller";
	private static final String SEPARATOR = ";";

	public static String[] unmarshallIdentifierList(String value) {
		return TextUtils.split(value, SEPARATOR);
	}

	public static String marshallIdentifierList(String[] list) {
		return TextUtils.join(SEPARATOR, list);
	}

	public static String marshallContainer(ProfileFieldContainer container) {
		return StringableProfileFieldContainer.from(container).toJSONString();
	}

	public static ProfileFieldContainer unmarshallContainer(String value) {
		return StringableProfileFieldContainer.fromJSONString(value);
	}

	private static class StringableProfileFieldContainer extends ProfileFieldContainer {

		private static final String FIELDS = "fields";
		private static final String STATUS = "modified";

		public static StringableProfileFieldContainer fromJSONString(String value) {
			StringableProfileFieldContainer container = new StringableProfileFieldContainer();

			try {
				JSONObject source = new JSONObject(value);
				JSONObject fields = source.getJSONObject(FIELDS);
				JSONObject status = source.getJSONObject(STATUS);

				Iterator<String> identifiers = fields.keys();
				while (identifiers.hasNext()) {
					String id = identifiers.next();
					container.mFields.putString(id, fields.getString(id));
					container.mStatus.putString(id, status.getString(id));
				}

			} catch (JSONException e) {
				Log.e(TAG, "Invalid JSON for container", e);
			}

			return container;
		}

		public static StringableProfileFieldContainer from(ProfileFieldContainer source) {
			if (source instanceof StringableProfileFieldContainer) {
				return (StringableProfileFieldContainer) source;
			}

			return new StringableProfileFieldContainer(source);
		}

		public StringableProfileFieldContainer(ProfileFieldContainer source) {
			super(source);
		}

		public StringableProfileFieldContainer() {
		}

		public String toJSONString() {
			try {
				JSONObject fields = new JSONObject();
				JSONObject status = new JSONObject();
				for (String key : mFields.keySet()) {
					fields.put(key, mFields.getString(key));
					status.put(key, mStatus.getString(key));
				}

				JSONObject result = new JSONObject();
				result.put(FIELDS, fields);
				result.put(STATUS, status);

				return result.toString();
			} catch (JSONException e) {
				Log.e(TAG, "Error marshalling container", e);
				return "";
			}
		}

	}
}
