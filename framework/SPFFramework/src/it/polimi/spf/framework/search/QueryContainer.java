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
package it.polimi.spf.framework.search;

import it.polimi.spf.shared.model.SPFQuery;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Jacopo
 * Contains all the information needed to perform queries.
 */
/*package*/ class QueryContainer {
	
	/*
	 * The query  
	 */
	private final SPFQuery query;
	
	/*
	 * The caller app identifier. Used to select the right SPFPersona.
	 */
	private final String callerAppId;
	
	/*
	 * The user identifier.
	 */
	private final String userUID;
	
	
	QueryContainer(SPFQuery query, String callerAppId, String userUID) {
		super();
		this.query = query;
		this.callerAppId = callerAppId;
		this.userUID = userUID;
	}
	
	String toJSON(){
		JSONObject o = new JSONObject();
		try {
			o.put("query", query.toQueryString())
			.put("callerAppId", callerAppId)
			.put("userUID", userUID);
		} catch (JSONException e) {
			return null;
		}
		return o.toString();
	}
	
	static QueryContainer fromJSON(String json) throws JSONException{
		JSONObject o = new JSONObject(json);
		SPFQuery query = SPFQuery.fromQueryString(o.getString("query"));
		String callerAppId = o.getString("callerAppId");
		String userUID = o.getString("userUID");
		return new QueryContainer(query, callerAppId, userUID);
	}

	/**
	 * @return the query
	 */
	protected SPFQuery getQuery() {
		return query;
	}

	/**
	 * @return the callerAppId
	 */
	protected String getCallerAppId() {
		return callerAppId;
	}

	/**
	 * @return the userUID
	 */
	protected String getUserUID() {
		return userUID;
	}
	
	
	
	

}
