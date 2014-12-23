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

import it.polimi.spf.framework.SPF;
import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFSearchDescriptor;

/**
 * Object that describes the properties of an instant search query.
 * 
 */
/*package*/ class QueryInfo {
	
	private String appName;
	private String queryId;
	private long signalPeriod;
	private short remainingSignals;
	private QueryContainer queryContainer;

	/**
	 * @param query
	 * @param appName
	 * @param signalPeriod
	 * @param remainingSignals
	 */
	public QueryInfo(SPFQuery query, String appName, long signalPeriod, short remainingSignals) {
		super();
		
		this.appName = appName;
		this.signalPeriod = signalPeriod;
		this.remainingSignals = remainingSignals;
		this.queryContainer = new QueryContainer(query,appName,SPF.get().getUniqueIdentifier());
	}


	/**
	 * @return the appName
	 */
	public String getAppName() {
		return appName;
	}


	/**
	 * @return the signalInterval
	 */
	public long getSignalPeriod() {
		return signalPeriod;
	}


	/**
	 * @return the remainingSignals
	 */
	public short getRemainingSignals() {
		return remainingSignals;
	}

	/**
	 * @param remainingSignals
	 *            the remainingSignals to set
	 */
	public void setRemainingSignals(short remainingSignals) {
		this.remainingSignals = remainingSignals;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public String getQueryId() {
		return this.queryId;
	}

	public void decrementRemainingSignals() {
		remainingSignals--;
	}

	public static QueryInfo create(String appIdentifier, SPFSearchDescriptor searchDescriptor) {
		SPFQuery query = searchDescriptor.getQuery();
		String appName = appIdentifier;
		short remainingSignals = (short) searchDescriptor.getNumberOfSignals();
		long signalPeriod = searchDescriptor.getIntervalBtwSignals();

		return new QueryInfo(query, appName, signalPeriod, remainingSignals);
	}


	public String getQueryJSONtoSend() {
		return queryContainer.toJSON();
	}

}
