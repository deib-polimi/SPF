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
package it.polimi.spf.app.fragments.appmanager;

import it.polimi.spf.framework.R;
import it.polimi.spf.shared.model.Permission;

import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

public class PermissionNameHelper {

	private static final String TAG = "PermissionHelper";
	private static SparseArray<String> sPermissionNamesCache = new SparseArray<String>();

	public static String getPermissionFriendlyName(Permission p, Context context) {
		String permissionIdentifier = p.name().toLowerCase(Locale.US);
		int id = -1;
		try {
			id = R.string.class.getField("permission_" + permissionIdentifier).getInt(null);
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (NoSuchFieldException e) {
		}

		if(id == -1) {
			Log.w(TAG, "No friendly name defined for permission " + permissionIdentifier);
			return permissionIdentifier;
		}
		
		String name = sPermissionNamesCache.get(id);
		if (name == null) {
			name = context.getString(id);
			sPermissionNamesCache.put(id, name);
		}

		return name;
	}
}
