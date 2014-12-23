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
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO #Documentation
 * 
 * @author Jacopo Aliprandi
 */
public final class SPFActionIntent extends SPFAction {

    private static final String KEY_ACTION = "action";
    private static final String TAG = "SPFActionIntent";
    private final int mType = TYPE_INTENT;
    private final String mAction;

	/*
	 * spf add some arguments in the intent. Use this constant to retrieve the information from the received intent
	 */
    /**
     * the spf identifier of the entity that activated the trigger
     */
    public static final String ARG_STRING_TARGET = "targetid";

    /**
     * the display name name of the entity that activated the trigger
     */
    public static final String ARG_STRING_DISPLAY_NAME = "displayname";

    /**
     * the name of the trigger
     */
    public static final String ARG_STRING_TRIGGER_NAME = "trigger_name";

    /**
     * the id of the trigger
     */
    public static final String ARG_LONG_TRIGGER_ID = "trigger_id";


    /**
     * Constructs a new instance provided the intent action
     *
     * @param action - the intent action
     */
    public SPFActionIntent(String action) {
        if (action == null) {
            throw new NullPointerException("action must not be null");
        }

        this.mAction = action;
    }

    /*
     * Constructor to create instances from parcels.
     */
    private SPFActionIntent(Parcel source) {
        mAction = source.readString();
    }

    public SPFActionIntent(JSONObject source) {
        try {
            mAction = source.getString(KEY_ACTION);
        } catch (JSONException e) {
            throw new IllegalArgumentException("JSON does not represent a valid SPFActionIntent", e);
        }
    }

    public String getAction() {
        return mAction;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAction);
    }

    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} pattern.
     */
    public static final Creator<SPFActionIntent> CREATOR = new Creator<SPFActionIntent>() {

        @Override
        public SPFActionIntent[] newArray(int size) {
            return new SPFActionIntent[size];
        }

        @Override
        public SPFActionIntent createFromParcel(Parcel source) {
            return new SPFActionIntent(source);
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see it.polimi.spf.framework.local.SPFAction#toJSON()
     */
    @Override
    public String toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(KEY_TYPE, mType);
            o.put(KEY_ACTION, mAction);
            return o.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error marshalling action:", e);
            return "";
        }
    }
}
