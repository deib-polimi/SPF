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
 * This class defines the standards errors that may be raised by the framework.
 */
public class SPFError implements Parcelable {

	public static final int NONE_ERROR_CODE = 0;
	public static final int TOKEN_NOT_VALID_ERROR_CODE = 1;
	public static final int PERMISSION_DENIED_ERROR_CODE = 2;
	public static final int REMOTE_EXC_ERROR_CODE = 3;
	public static final int INSTANCE_NOT_FOUND_ERROR_CODE = 4;
	public static final int NETWORK_ERROR_CODE = 5;
	public static final int ILLEGAL_ARGUMENT_ERROR_CODE = 6;
	public static final int REGISTRATION_REFUSED_ERROR_CODE = 7;
	public static final int INTERNAL_SPF_ERROR_CODE = 8;
	public static final int SPF_NOT_INSTALLED_ERROR_CODE = 9;

	private int code;
	private String message;

	/**
	 * Creates a new {@link SPFError} for the given error code, with
	 * <code>null</code> error message
	 * 
	 * @param errorCode
	 */
	public SPFError(int errorCode) {
		this.code = errorCode;
	}

	/**
	 * Creates an empty {@link SPFError} ({@link #isOk()} returns true)
	 */
	public SPFError() {
		this.code = NONE_ERROR_CODE;
	}

	public boolean codeEquals(int errorCode) {
		return this.code == errorCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SPFError)) {
			return false;
		}
		SPFError other = (SPFError) obj;
		if (code != other.code) {
			return false;
		}
		return true;
	}

	SPFError(Parcel source) {
		this.code = source.readInt();
	}

	public static Creator<SPFError> CREATOR = new Creator<SPFError>() {

		@Override
		public SPFError[] newArray(int size) {

			return new SPFError[size];
		}

		@Override
		public SPFError createFromParcel(Parcel source) {
			return new SPFError(source);
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(code);
		dest.writeString(message);
	}

	public void readFromParcel(Parcel source) {
		this.code = source.readInt();
		this.message = source.readString();
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Sets the code and the message of the error
	 * 
	 * @param code
	 *            - the code of the message
	 * @param message
	 *            - the message of the error
	 */
	public void setError(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * @return the message of the error
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return true if no error was raised
	 */
	public boolean isOk() {
		return code == NONE_ERROR_CODE;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[SPF ERROR - " );
		switch (code) {
		case NONE_ERROR_CODE:
			builder.append("NONE_ERROR");
			break;
		case TOKEN_NOT_VALID_ERROR_CODE:
			builder.append("TOKEN_NOT_VALID");
			break;
		case PERMISSION_DENIED_ERROR_CODE:
			builder.append("PERMISSION_DENIED");
			break;
		case REMOTE_EXC_ERROR_CODE:
			builder.append("REMOTE_EXCEPTION");
			break;
		case INSTANCE_NOT_FOUND_ERROR_CODE:
			builder.append("INSTANCE_NOT_FOUND");
			break;
		case NETWORK_ERROR_CODE:
			builder.append("NETWORK_ERROR");
			break;
		case ILLEGAL_ARGUMENT_ERROR_CODE:
			builder.append("ILLEGAL_ARUMENT");
			break;
		case REGISTRATION_REFUSED_ERROR_CODE:
			builder.append("REGISTRATION_REFUSED");
			break;
		case INTERNAL_SPF_ERROR_CODE:
			builder.append("INTERNAL_SPF_ERROR");
			break;
		case SPF_NOT_INSTALLED_ERROR_CODE:
			builder.append("SPF_NOT_INSTALLED");
			break;
		default:
			builder.append("ERROR CODE ");
			builder.append(code);
		}
		
		if(message != null){
			builder.append(": ");
			builder.append(message);
		} else {
			builder.append(" (no message)");
		}
		
		builder.append("]");
		return builder.toString();
	}

}
