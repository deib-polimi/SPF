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

import android.app.Service;
import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describes a SPF Service that may be registered in SPF or executed. Contained
 * details are:
 * <ul>
 * <li><b>serviceName</b>: the name of the service to invoke/register
 * <b>(Required)</b></li>
 * <li><b>appIdentifier</b>: the packageIdentifier of the app who owns the
 * service<b>(Required)</b></li>
 * <li><b>version</b>: the version of the service<b>(Required)</b></li>
 * <li><b>componentName</b>: the String version of the {@link ComponentName} of
 * the Android {@link Service} implementing the SPF service described by
 * <code>this</code> descriptor, as returned by flattened String (
 * {@link ComponentName#flattenToString()}) <b>(Required for registration)</b></li>
 * <li><b>consumedVerbs</b>: the array of {@link SPFActivity#VERB} that can be
 * handled by this service</li>
 * </ul>
 * 
 * @author darioarchetti
 */
public class SPFServiceDescriptor implements Parcelable {

	private String mServiceName;
	private String mDescription;
	private String mAppIdentifier;
	private String mVersion;
	private String mComponentName;
	private String[] mConsumedVerbs;

	/**
	 * Creates a new {@link SPFServiceDescriptor}
	 * 
	 * @param svcName
	 *            - the name of the service
	 * @param appIdentifier
	 *            - the identifier of the app owner of the service
	 * @param version
	 *            - the version of the service
	 * @param componentName
	 *            - the componentName of the Android {@link Service}
	 *            implementing the SPF service described
	 * @param consumedVerbs
	 *            - the verbs supported by the described SPF Service
	 */
	public SPFServiceDescriptor(String name, String description, String appIdentifier, String version, String componentName, String[] consumedVerbs) {
		this.mServiceName = name;
		this.mDescription = description;
		this.mAppIdentifier = appIdentifier;
		this.mVersion = version;
		this.mComponentName = componentName;
		this.mConsumedVerbs = consumedVerbs;
	}

	private SPFServiceDescriptor(Parcel source) {
		this.mServiceName = source.readString();
		this.mDescription = source.readString();
		this.mAppIdentifier = source.readString();
		this.mVersion = source.readString();
		this.mComponentName = source.readString();
		this.mConsumedVerbs = source.createStringArray();
	}

	/**
	 * @return the name of the SPF Service
	 */
	public String getServiceName() {
		return mServiceName;
	}

	/**
	 * Return the description of the SPFService
	 * 
	 * @return
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * @return the identifier of the app
	 */
	public String getAppIdentifier() {
		return mAppIdentifier;
	}

	/**
	 * @return the version of the SPF Service
	 */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * @return the componentName of the implementation
	 */
	public String getComponentName() {
		return mComponentName;
	}

	/**
	 * @return the supported verbs
	 */
	public String[] getConsumedVerbs() {
		return mConsumedVerbs;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mServiceName);
		dest.writeString(mDescription);
		dest.writeString(mAppIdentifier);
		dest.writeString(mVersion);
		dest.writeString(mComponentName);
		dest.writeStringArray(mConsumedVerbs);
	}

	public final static Creator<SPFServiceDescriptor> CREATOR = new Creator<SPFServiceDescriptor>() {

		@Override
		public SPFServiceDescriptor createFromParcel(Parcel source) {
			return new SPFServiceDescriptor(source);
		}

		@Override
		public SPFServiceDescriptor[] newArray(int size) {
			return new SPFServiceDescriptor[size];
		}

	};
}