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

/*package*/ interface SearchPerformer {
	
	void sendSearchSignal(QueryInfo queryInfo);

	void notifyStoppedSearch(QueryInfo queryInfo);

	void dispatchSearchResult(QueryInfo queryInfo, SearchResult result);

	void notifyResultLost(QueryInfo queryInfo, String uniqueIdentifier);

	void notifySearchStarted(QueryInfo queryInfo);

	void registerSearchCallback(SPFSearchCallback callback, QueryInfo queryInfo);

	void unregisterSearchCallback(QueryInfo queryInfo);
	
}
