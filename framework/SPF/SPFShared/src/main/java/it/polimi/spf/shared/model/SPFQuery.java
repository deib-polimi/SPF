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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO #Documentation
 * 
 * @author darioarchetti
 * 
 */
public class SPFQuery implements Parcelable {

	private List<String> mTags;
	private Map<String, String> mProfileFields;
	private List<String> mApps;

	@SuppressWarnings("unchecked")
	private SPFQuery(Parcel in) {
		ClassLoader cl = ((Object) this).getClass().getClassLoader();
		mTags = in.readArrayList(cl);
		mProfileFields = new HashMap<String, String>();
		in.readMap(mProfileFields, cl);
		mApps = in.readArrayList(cl);
	}

	private SPFQuery() {
		mTags = new ArrayList<String>();
		mProfileFields = new HashMap<String, String>();
		mApps = new ArrayList<String>();
	}

	public SPFQuery or(SPFQuery q) {
		return null;// TODO ?
	}

	public List<String> getTags() {
		return mTags;
	}

	public Map<String, String> getProfileFields() {
		return mProfileFields;
	}

	public List<String> getApps() {
		return mApps;
	}

	/**
	 * @author darioarchetti
	 */
	public static class Builder {

		private SPFQuery mQuery;

		/**
         *
         */
		public Builder() {
			mQuery = new SPFQuery();
		}

		/**
		 * @param profileField
		 * @param value
		 * 
		 * @return
		 */
		public <E> Builder setProfileField(ProfileField<E> profileField, E value) {
			checkQuery();
			mQuery.mProfileFields.put(profileField.getIdentifier(), ProfileFieldConverter.forField(profileField).toStorageString(value));
			return this;
		}

		/**
		 * @param tag
		 * 
		 * @return
		 */
		public Builder setTag(String tag) {
			checkQuery();
			mQuery.mTags.add(tag);
			return this;
		}

		/**
		 * @param tagList
		 * 
		 * @return
		 */
		public Builder setTagList(List<String> tagList) {
			checkQuery();
			mQuery.mTags.addAll(tagList);
			return this;
		}

		/**
		 * @param appIdentifier
		 * 
		 * @return
		 */
		public Builder setAppIdentifier(String appIdentifier) {
			checkQuery();
			mQuery.mApps.add(appIdentifier);
			return this;
		}

		/**
		 * @return
		 */
		public SPFQuery build() {
			SPFQuery q = mQuery;
			mQuery = null;
			return q;
		}

		private void checkQuery() {
			if (mQuery == null) {
				throw new IllegalStateException("SPFQuery already built");
			}
		}
	}

	private static final String TAGS = "tags";
	private static final String FIELDS = "fields";
	private static final String APPS = "apps";
	private static final String TAG = "SPFQuery";

	private String mQueryString;

	public String toQueryString() {
		if (mQueryString != null) {
			return mQueryString;
		}

		JSONObject queryString = new JSONObject();

		JSONArray tags = new JSONArray();
		for (String t : mTags) {
			tags.put(t);
		}

		JSONArray fields = new JSONArray();
		for (String k : mProfileFields.keySet()) {
			fields.put(k + "," + mProfileFields.get(k));
		}

		JSONArray apps = new JSONArray();
		for (String a : mApps) {
			apps.put(a);
		}

		try {
			queryString.put(TAGS, tags);
			queryString.put(FIELDS, fields);
			queryString.put(APPS, apps);
		} catch (JSONException e) {
			// Should never happen
			Log.e(TAG, "Error marshalling query:", e);
		}

		return queryString.toString();
	}

	public static SPFQuery fromQueryString(String input) {
		SPFQuery q = new SPFQuery();

		try {
			JSONObject o = new JSONObject(input);

			JSONArray tags = o.getJSONArray(TAGS);
			for (int i = 0; i < tags.length(); i++) {
				q.mTags.add(tags.getString(i));
			}

			JSONArray fields = o.getJSONArray(FIELDS);
			for (int i = 0; i < fields.length(); i++) {
				String[] parts = fields.getString(i).split(",");
				q.mProfileFields.put(parts[0], parts[1]);
			}

			JSONArray apps = o.getJSONArray(APPS);
			for (int i = 0; i < apps.length(); i++) {
				q.mApps.add(apps.getString(i));
			}

			q.mQueryString = input;
		} catch (JSONException e) {
			Log.e(TAG, "JSON exception unmarshalling query", e);
		}

		return q;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(mTags);
		dest.writeMap(mProfileFields);
		dest.writeList(mApps);
	}

	public static final Creator<SPFQuery> CREATOR = new Creator<SPFQuery>() {

		@Override
		public SPFQuery createFromParcel(Parcel source) {
			return new SPFQuery(source);
		}

		@Override
		public SPFQuery[] newArray(int size) {
			return new SPFQuery[size];
		}
	};
}
