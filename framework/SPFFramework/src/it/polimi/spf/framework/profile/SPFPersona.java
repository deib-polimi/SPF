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
package it.polimi.spf.framework.profile;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Jacopo Aliprandi
 * 
 */
public class SPFPersona implements Parcelable {
	private static final String DEFAULT_IDENTIFIER = "default";
	public static final SPFPersona DEFAULT = new SPFPersona();

	private String mDisplayName;

	public SPFPersona(String displayName) {
		this.mDisplayName = displayName;
	}

	public SPFPersona() {
		this.mDisplayName = DEFAULT_IDENTIFIER;
	}

	@Override
	public String toString() {
		return mDisplayName;
	}

	public String getIdentifier() {
		return mDisplayName;
	}

	public boolean isDefault() {
		return DEFAULT_IDENTIFIER.equals(mDisplayName);
	}

	// parcelable methods

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mDisplayName);
	}

	private SPFPersona(Parcel source) {
		mDisplayName = source.readString();
	}

	public static final Parcelable.Creator<SPFPersona> CREATOR = new Creator<SPFPersona>() {

		@Override
		public SPFPersona[] newArray(int size) {

			return new SPFPersona[size];
		}

		@Override
		public SPFPersona createFromParcel(Parcel source) {

			return new SPFPersona(source);
		}
	};

	@Override
	public boolean equals(Object o) {
		if (o instanceof SPFPersona) {
			return ((SPFPersona) o).mDisplayName.equals(mDisplayName);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return mDisplayName.hashCode();
	}

	public static SPFPersona getDefault() {
		return new SPFPersona();
	}

}
