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
 * Container for basic information about a person that is transmitted together
 * with search results.
 * 
 * BaseInfo carries the following details:
 * <ul>
 * <li>Identifier</li>
 * <li>Display name</li>
 * </ul>
 * 
 * @author darioarchetti
 */
public class BaseInfo implements Parcelable {

	private final String mIdentifier, mDisplayName;

	/**
	 * Creates a new instance of {@link BaseInfo} with the provided details.
	 * 
	 * @param mIdentifier
	 *            - the identifier (see {@link ProfileField#IDENTIFIER})
	 * @param mDisplayName
	 *            - the display name (see {@link ProfileField#DISPLAY_NAME})
	 */
	public BaseInfo(String mIdentifier, String mDisplayName) {
		this.mIdentifier = mIdentifier;
		this.mDisplayName = mDisplayName;
	}

	public BaseInfo(Parcel source) {
		mIdentifier = source.readString();
		mDisplayName = source.readString();
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return mIdentifier;
	}

	/**
	 * @return the display name
	 */
	public String getDisplayName() {
		return mDisplayName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mIdentifier);
		dest.writeString(mDisplayName);
	}

	public static final Creator<BaseInfo> CREATOR = new Creator<BaseInfo>() {

		@Override
		public BaseInfo[] newArray(int size) {
			return new BaseInfo[size];
		}

		@Override
		public BaseInfo createFromParcel(Parcel source) {
			return new BaseInfo(source);
		}
	};
}
