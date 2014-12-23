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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ContactsDetailStorage {

	private static final String HAS_PICTURE = "_haspic";
	private static final String CONTACT_INFO = "contacts";
	private static final String DISPLAY_NAME = "_displayname";
	private static final String TAG = "ContactsDetailStorage";
	private Context mContext;

	public ContactsDetailStorage(Context context) {
		this.mContext = context;
	}

	public PersonInfo getContactInfo(PersonAuth auth) {
		SharedPreferences prefs = mContext.getSharedPreferences(CONTACT_INFO, Context.MODE_PRIVATE);
		String name = prefs.getString(auth.getUserIdentifier() + DISPLAY_NAME, null);
		boolean hasPic = prefs.getBoolean(auth.getUserIdentifier() + HAS_PICTURE, false);
		return new PersonInfo(auth, name, hasPic);
	}

	public void saveContactDetail(String identifier, String displayName, Bitmap profilePic) {
		if (profilePic != null) {
			try {
				FileOutputStream out = mContext.openFileOutput(identifier, Context.MODE_PRIVATE);
				profilePic.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
			} catch (IOException e) {
				Log.e(TAG, "Error writing pic to file", e);
			}
		}

		SharedPreferences prefs = mContext.getSharedPreferences(CONTACT_INFO, Context.MODE_PRIVATE);
		prefs.edit().putString(identifier + DISPLAY_NAME, displayName).putBoolean(identifier + HAS_PICTURE, profilePic != null).apply();
	}

	public static Drawable getProfilePicture(Context context, PersonInfo info) {
		if (!info.hasProfilePic()) {
			return null;
		}

		try {
			FileInputStream input = context.openFileInput(info.getIdentifier());
			Bitmap bitmap = BitmapFactory.decodeStream(input);
			input.close();
			return new BitmapDrawable(context.getResources(), bitmap);
		} catch (IOException e) {
			Log.e(TAG, "Error opening profile pic", e);
			return null;
		}
	}
	
	// TODO #Framework - remember to handle null value of getProfilePicture
	// public static Drawable getProfilePicture(Context context, PersonInfo
	// info) {
	// if (!info.hasProfilePic()) {
	// return
	// context.getResources().getDrawable(R.drawable.empty_profile_picture);
	// }
	//
	// try {
	// FileInputStream input = context.openFileInput(info.getIdentifier());
	// Bitmap bitmap = BitmapFactory.decodeStream(input);
	// input.close();
	// return new BitmapDrawable(context.getResources(), bitmap);
	// } catch (IOException e) {
	// Log.e(TAG, "Error opening profile pic", e);
	// return
	// context.getResources().getDrawable(R.drawable.empty_profile_picture);
	// }
	// }
}
