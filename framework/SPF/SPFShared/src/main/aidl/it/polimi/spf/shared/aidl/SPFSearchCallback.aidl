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
 
package it.polimi.spf.shared.aidl;

import it.polimi.spf.shared.model.BaseInfo;

/**
 * Callback interface for local applications to be notified of events occurred
 * while discovering people in proximity. A callback is registered in SPF using
 * {@link SPFProximityService#registerCallback(String, SPFSearchCallback, SPFError)}
 * , and after search is started with
 * {@link SPFProximityService#startNewSearch(String, SPFSearchDescriptor, SPFError)}
 * , SPF will use the methods of this callback to notify the local app.
 */
oneway interface SPFSearchCallback{

	/**
	 * Called by SPF when a search starts.
	 * 
	 */
    void onSearchStart(String queryId);

  /**
	 * Called by SPF when a search is stopped, either by the local app with
	 * {@link SPFProximityService#stopSearch(String, String, it.polimi.spf.shared.model.SPFError)}
	 * or because it was completed.
	 * 
	 */
    void onSearchStop(String queryId);
    
  /**
	 * Called by SPF when an error occurred while discovering person in
	 * proximity search. The search was stopped as a consequence.
	 * 
	 * @param queryId
	 *            - the id of the search that was stopped after an error.
	 */
    void onSearchError(String queryId);
    
  /**
	 * Called when a person is found in proximity.
	 * 
	 * @param uniqueIdentifier - the identifier of the found instance
	 * @param baseInfo - the {@link BaseInfo} of the found person
	 */
    void onSearchResultReceived(String queryId, String userIdentifier, in BaseInfo baseInfo);
    
    /**
	 * Called when a result previously found is no more available.
	 * 
	 * @param uniqueIdentifier
	 *            - the identifier of the instance which is no more available
	 */
    void onSearchResultLost(String queryId, String userIdentifier);
}