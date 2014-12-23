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

import it.polimi.spf.shared.aidl.SPFSearchCallback;
import it.polimi.spf.shared.model.SPFSearchDescriptor;

/**
 * 
 *  
 */
public class SPFSearchManager {

	private SearchPerformerImpl mSearchPerformer;
	private SearchScheduler mSearchScheduler;

	public SPFSearchManager() {
		mSearchPerformer = new SearchPerformerImpl();
		mSearchScheduler = new SearchScheduler(mSearchPerformer);
	}

	/**
	 * Call this method to stop the search and release the associated resources.
	 * The application will not be notified about the event;
	 * 
	 * @param queryId
	 */
	public void stopSearch(String queryId) {
		mSearchScheduler.stopSearch(queryId);
	}

	/**
	 * Start a new search.
	 * 
	 * @param appIdentifier
	 *            - the package name of the caller app
	 * @param searchDescriptor
	 *            - a descriptor specifying query and settings
	 * @return the identifier of the query
	 */
	public String startSearch(String appIdentifier, SPFSearchDescriptor searchDescriptor, SPFSearchCallback callback) {
		QueryInfo queryInfo = mSearchScheduler.registerSearch(appIdentifier, searchDescriptor,callback);
		mSearchScheduler.startSearch(queryInfo);
		return queryInfo.getQueryId();
	}

	public void onInstanceLost(String uniqueIdentifier) {
		mSearchScheduler.onInstanceLost(uniqueIdentifier);
	}

	public void onSearchResultReceived(SearchResult searchResult) {
		mSearchScheduler.onSearchResultReceived(searchResult);
	}

}
