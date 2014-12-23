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

import android.database.CursorJoiner.Result;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container for the result of an invocation. It may be in two state:
 * {@link #RESULT}, if the invocation completed successfully, or {@link #ERROR},
 * if an error prevented the invocation from completing normally.
 * 
 * In case of {@link Result}, the container holds the result in case of non void
 * methods. Otherwise, it may contain an error message detailing the error.
 * 
 * @author darioarchetti
 * 
 */
public class InvocationResponse implements Parcelable {

	/**
	 * State for successful invocation
	 */
	public final static int RESULT = 0;

	/**
	 * State for erroneous invocation
	 */
	public final static int ERROR = 1;

	private final int type;
	private String errorMessage;
	private String resultPayload;

	/**
	 * Creates a new {@link InvocationResponse} to hold the result of an
	 * invocation.
	 * 
	 * @param result
	 *            - the result of the invocation, or null if the method returns
	 *            void.
	 * @return the invocation request.
	 */
	public static InvocationResponse result(String payload) {
		InvocationResponse resp = new InvocationResponse(RESULT);
		resp.resultPayload = payload;
		return resp;
	}

	/**
	 * Creates a new {@link InvocationResponse} to hold the error that occurred
	 * during the execution of the service.
	 * 
	 * @param t
	 *            - the throwable that caused the error.
	 * @return the invocation request.
	 */
	public static InvocationResponse error(Throwable t) {
		InvocationResponse resp = new InvocationResponse(ERROR);
		resp.errorMessage = t.getClass().getName() + ":" + t.getLocalizedMessage();
		return resp;
	}

	/**
	 * Creates a new {@link InvocationResponse} to hold the error that occurred
	 * during the execution of the service.
	 * 
	 * @param errorMessage
	 *            - the error message that describes the occurred error.
	 * @return the invocation request.
	 */
	public static InvocationResponse error(String errorMessage) {
		InvocationResponse resp = new InvocationResponse(ERROR);
		resp.errorMessage = errorMessage;
		return resp;
	}

	private InvocationResponse(int type) {
		this.type = type;
	}

	private InvocationResponse(Parcel source) {
		type = source.readInt();
		switch (type) {
		case ERROR:
			errorMessage = source.readString();
			break;
		case RESULT:
			resultPayload = source.readString();
			break;
		}
	}

	/**
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the result
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the result
	 */
	public String getPayload() {
		return resultPayload;
	}

	/**
	 * Sets the result
	 * 
	 * @param result
	 */
	public void setPayload(String payload) {
		this.resultPayload = payload;
	}

	/**
	 * @return the type of the container
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return true if the type is {@link #RESULT}
	 */
	public boolean isResult() {
		return type == RESULT;
	}

	@Override
	public String toString() {
		return "Invocation response: " + (type == RESULT ? "RESULT\n" + resultPayload : "ERROR\n" + errorMessage);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(type);
		switch (type) {
		case ERROR:
			dest.writeString(errorMessage);
			break;
		case RESULT:
			dest.writeString(resultPayload);
			break;
		}
	}

	public final static Creator<InvocationResponse> CREATOR = new Creator<InvocationResponse>() {

		@Override
		public InvocationResponse[] newArray(int arg0) {
			return new InvocationResponse[arg0];
		}

		@Override
		public InvocationResponse createFromParcel(Parcel arg0) {
			return new InvocationResponse(arg0);
		}
	};
}
