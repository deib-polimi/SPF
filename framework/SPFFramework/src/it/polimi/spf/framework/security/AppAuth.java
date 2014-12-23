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

import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.Permission;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Jacopo Aliprandi TODO IDEA Contains the information about the calling
 *         application e.g. the app package name and permissions. Should be
 *         generated from a token by a security monitor.
 */
public final class AppAuth implements Parcelable {

	private String mAppName;
	private String mAppIdentifier;
	private int mPermissionCode;
	private String mPersona;

	// field not to be parcelled
	private Permission[] mPermissions;
	
	// package visible constructor
	AppAuth(String appName, String appIdentifier, int permissionCode, String persona) {
		this.mAppName = appName;
		this.mAppIdentifier = appIdentifier;
		this.mPermissionCode = permissionCode;
		this.mPersona = persona;
		
		this.mPermissions = PermissionHelper.getPermissions(permissionCode);
	}

	private AppAuth(Parcel source) {
		this.mAppName = source.readString();
		this.mAppIdentifier = source.readString();
		this.mPermissionCode = source.readInt();
		this.mPersona = source.readString();
		this.mPermissions = PermissionHelper.getPermissions(mPermissionCode);
	}

	public String getAppName() {
		return mAppName;
	}

	public String getAppIdentifier() {
		return mAppIdentifier;
	}

	public int getPermissionCode(){
		return mPermissionCode;
	}
	
	public Permission[] getPermissions() {
		return mPermissions;
	}

	public SPFPersona getPersona() {
		return new SPFPersona(mPersona);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mAppName);
		dest.writeString(mAppIdentifier);
		dest.writeInt(mPermissionCode);
		dest.writeString(mPersona);
	}

	public static final Parcelable.Creator<AppAuth> CREATOR = new Creator<AppAuth>() {

		@Override
		public AppAuth[] newArray(int size) {
			return new AppAuth[size];
		}

		@Override
		public AppAuth createFromParcel(Parcel source) {
			return new AppAuth(source);
		}
	};

}
