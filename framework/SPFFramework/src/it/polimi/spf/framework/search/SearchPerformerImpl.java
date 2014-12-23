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

import java.util.Hashtable;

import android.util.Log;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.shared.aidl.SPFSearchCallback;

/*package*/ public class SearchPerformerImpl implements SearchPerformer {

	private final static String TAG = "SearchPerformerImpl";

	private Hashtable<String, SPFSearchCallback> callbacks = new Hashtable<String, SPFSearchCallback>();

	public SearchPerformerImpl() {

	}

	@Override
	public void registerSearchCallback(SPFSearchCallback callback, QueryInfo queryInfo) {
		callbacks.put(queryInfo.getQueryId(), callback);
	}

	@Override
	public void unregisterSearchCallback(QueryInfo queryInfo) {
		try{
			callbacks.remove(queryInfo.getQueryId());
		}catch(Throwable t){
			//TODO handle error 
		}
	}

	@Override
	public void notifySearchStarted(QueryInfo obj) {
		try {
			getAppCallback(obj).onSearchStart(obj.getQueryId());
		} catch (Throwable e) {
			// TODO handle error
		}
	}

	@Override
	public void sendSearchSignal(QueryInfo queryinfo) {
		log(TAG, "asking SPF to send search signal");
		String queryId = queryinfo.getQueryId();
		String queryJSON = queryinfo.getQueryJSONtoSend();
		SPF.get().sendSearchSignal(queryId, queryJSON);
	}

	@Override
	public void dispatchSearchResult(QueryInfo qd, SearchResult result) {
		log(TAG, "dispatching SearchResult to AppCallback ");
		try {
			getAppCallback(qd).onSearchResultReceived(qd.getQueryId(), result.getUniqueIdentifier(), result.getBaseInfo());
		} catch (Throwable e) {
			// TODO handle error
		}
	}

	@Override
	public void notifyResultLost(QueryInfo qi, String uniqueIdentifier) {
		log(TAG, "notifying Result Lost to App callback");
		try {
			getAppCallback(qi).onSearchResultLost(qi.getQueryId(), uniqueIdentifier);
		} catch (Throwable e) {
			// TODO handle error
		}
	
	}

	@Override
	public void notifyStoppedSearch(QueryInfo qd) {
		log(TAG, "notifyStoppedSearch to AppCallback");
		try {
			getAppCallback(qd).onSearchStop(qd.getQueryId());
		} catch (Throwable e) {
			// TODO handle error
		}
	}

	private SPFSearchCallback getAppCallback(QueryInfo queryInfo) {
		return callbacks.get(queryInfo.getQueryId());
	}

	private static void log(String tag, String msg) {
		Log.d(tag, msg);
	}

}
