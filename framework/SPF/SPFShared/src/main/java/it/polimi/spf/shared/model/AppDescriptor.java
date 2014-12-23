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

/**
 * Describes an application to be registered in SPF. Contained details are:
 * <ul>
 * <li><b>appIdentifier</b>: the package identifier of the app within Android</li>
 * <li><b>appName</b>: the name of the app</li>
 * <li><b>version</b>: the version of the app</li>
 * <li><b>permissionCode</b>: the code representing the list of permission
 * required by the app</li>
 * </ul>
 * 
 * @author darioarchetti
 */
public class AppDescriptor implements Parcelable {

	private String mAppIdentifier;
	private String mAppName;
	private String mVersion;
	private int mPermissionCodes;

	public AppDescriptor(String appIdentifier, String appName, String version, int permissionCodes) {
		this.mAppIdentifier = appIdentifier;
		this.mAppName = appName;
		this.mVersion = version;
		this.mPermissionCodes = permissionCodes;
	}

	private AppDescriptor(Parcel source) {
		this.mAppIdentifier = source.readString();
		this.mAppName = source.readString();
		this.mVersion = source.readString();
		this.mPermissionCodes = source.readInt();
	}

	/**
	 * @return the identifier of the app
	 */
	public String getAppIdentifier() {
		return mAppIdentifier;
	}

	/**
	 * Return the name of the app
	 * @return
	 */
	public String getAppName() {
		return mAppName;
	}

	/**
	 * Return the version of the app
	 * @return
	 */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * The permission code of the app
	 * @return
	 */
	public int getPermissionCode() {
		return mPermissionCodes;
	}

	@Override
	public String toString() {
		return mAppIdentifier + "-" + mVersion;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mAppIdentifier);
		dest.writeString(mAppName);
		dest.writeString(mVersion);
		dest.writeInt(mPermissionCodes);
	}

	public final static Creator<AppDescriptor> CREATOR = new Creator<AppDescriptor>() {

		@Override
		public AppDescriptor[] newArray(int size) {
			return new AppDescriptor[size];
		}

		@Override
		public AppDescriptor createFromParcel(Parcel source) {
			return new AppDescriptor(source);
		}
	};

}
