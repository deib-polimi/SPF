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
 * Container for a request of invocation that can be dispatched by a client app
 * to another application. It contains information to identify the target of the
 * invocation, the name of the method to invoke and the array of parameters.
 * 
 * @author darioarchetti
 * 
 */
public class InvocationRequest implements Parcelable {

	private String appName;
	private String serviceName;
	private String methodName;
	private String payload;

	public InvocationRequest(String appName, String serviceName, String methodName, String payload) {
		this.appName = appName;
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.payload = payload;
	}

	private InvocationRequest(Parcel source) {
		appName = source.readString();
		serviceName = source.readString();
		methodName = source.readString();
		payload = source.readString();
	}

	/**
	 * @return the name of the app
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * @return the name of the service to invoke
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @return the name of the method to invoke
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the array of parameters to pass during invocation
	 */
	public String getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return appName + " - " + serviceName + "." + methodName + payload;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appName);
		dest.writeString(serviceName);
		dest.writeString(methodName);
		dest.writeString(payload);
	}

	public static Creator<InvocationRequest> CREATOR = new Creator<InvocationRequest>() {

		@Override
		public InvocationRequest[] newArray(int size) {
			return new InvocationRequest[size];
		}

		@Override
		public InvocationRequest createFromParcel(Parcel source) {
			return new InvocationRequest(source);
		}
	};
}