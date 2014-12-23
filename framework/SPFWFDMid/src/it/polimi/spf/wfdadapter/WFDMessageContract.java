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
package it.polimi.spf.wfdadapter;

public interface WFDMessageContract {

	public static final String KEY_METHOD_ID = "methodName";
	public static final String KEY_REQUEST = "request";
	public static final String KEY_RESPONSE = "request";
	public static final String KEY_TOKEN = "token";
	public static final String KEY_ADV_PROFILE = "advProfile";
	public static final String KEY_FIELD_IDENTIFIERS = "fieldIdentifiers";
	public static final String KEY_APP_IDENTIFIER = "appIdentifier";
	public static final String KEY_SENDER_IDENTIFIER = "senderIdentifier";
	public static final String KEY_ACTION = "action";
	public static final String KEY_QUERY_ID = "queryId";
	public static final String KEY_QUERY = "query";
	public static final String KEY_BASE_INFO = "baseInfo";
	public static final String KEY_ACTIVITY = "activity";

	public static final int ID_EXECUTE_SERVICE = 0;
	public static final int ID_GET_PROFILE_BULK = 1;
	public static final int ID_SEND_CONTACT_REQUEST = 2;
	public static final int ID_SEND_NOTIFICATION = 3;
	public static final int ID_SEND_SEARCH_SIGNAL = 4;
	public static final int ID_SEND_SEARCH_RESULT = 5;
	public static final int ID_SEND_SPF_ADVERTISING = 6;
	public static final int ID_SEND_ACTIVITY = 7;

}
