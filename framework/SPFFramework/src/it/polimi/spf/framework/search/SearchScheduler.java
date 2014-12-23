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
import it.polimi.spf.shared.aidl.SPFSearchCallback;
import it.polimi.spf.shared.model.SPFSearchDescriptor;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/*package*/class SearchScheduler {

	/* package */SearchScheduler() {

	}

	private String TAG = "SearchScheduler";
	private Long id = 0L;

	/*
	 * Callback for asynchronous events and communication with other spf's
	 * components.
	 * 
	 * @see SearchPerformer
	 */
	private SearchPerformer performer;

	/**
	 * Constructor for the SearchManager object. Requires a SearchPerformer
	 * object.
	 * 
	 * @param performer
	 *            the performer to call for the execution of the SearchManager's
	 *            operations.
	 */
	SearchScheduler(SearchPerformer callback) {
		this.performer = callback;
	}

	/**
	 * Maps the query id to its QueryInfo object.
	 */
	private Hashtable<String, QueryInfo> queries = new Hashtable<String, QueryInfo>();

	/**
	 * Maps uniqueIdentifiers to queryIds.
	 */
	private Hashtable<String, List<String>> results = new Hashtable<String, List<String>>();

	/**
	 * Internal handler that manages the queries' schedule and emits the
	 * commands to send search signals.
	 */
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case SearchMessages.SEND_SIGNAL :
					String queryId = (String) msg.obj;
					sendSearchSignal(queryId);
					scheduleNextAction(queryId);
					break;
				case SearchMessages.RESULT_LOST :
					String[] args = (String[]) msg.obj;
					QueryInfo qi = queries.get(args[0]);
					String uniqueIdentifier = args[1];
					performer.notifyResultLost(qi, uniqueIdentifier);
					break;
				case SearchMessages.SEARCH_STARTED:
					performer.notifySearchStarted((QueryInfo) msg.obj);
					Message message = obtainMessage(SearchMessages.SEND_SIGNAL, ((QueryInfo)msg.obj).getQueryId());
					sendMessage(message);
					break;
				case SearchMessages.SEARCH_STOPPED:
					stopSearch((QueryInfo)msg.obj);
					performer.notifyStoppedSearch((QueryInfo) msg.obj);
					performer.unregisterSearchCallback((QueryInfo) msg.obj);
					break;
				default :
					break;
			}
		}
	};

	/**
	 * Class for the definition of messages used by the internal Handler.
	 * 
	 */
	private static class SearchMessages {
		public static final int SEND_SIGNAL = 0;
		public static final int RESULT_LOST = 1;
		public static final int SEARCH_STARTED = 2;
		public static final int SEARCH_STOPPED = 3;
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
	void startSearch(QueryInfo queryInfo) {
		Message msg = handler
				.obtainMessage(SearchMessages.SEARCH_STARTED, queryInfo);
		handler.sendMessage(msg);
	}

	/**
	 * Registers and starts the search operation as specified in queryInfo.
	 * @param callback 
	 * 
	 * @param queryInfo
	 * @return the query id
	 */
	QueryInfo registerSearch(String appIdentifier,
			SPFSearchDescriptor searchDescriptor, SPFSearchCallback callback) {
		// convert SearchDescriptor to internal representation
		// register the query and return the assigned id
		QueryInfo queryInfo = QueryInfo.create(appIdentifier, searchDescriptor);
		String queryId = generateQueryId(queryInfo);
		queries.put(queryId, queryInfo);
		performer.registerSearchCallback(callback, queryInfo);
		log(TAG, "registering query with queryId: " + queryId);
		return queryInfo;
	}

	/**
	 * Generate a query id for the specified query. The query id is assigned to
	 * the query descriptor.
	 * 
	 * @param queryInfo
	 * @return the query id
	 */
	private String generateQueryId(QueryInfo queryInfo) {
		String queryId = SPF.get().getUniqueIdentifier() + (++id);
		queryInfo.setQueryId(queryId);
		return queryId;
	}

	/**
	 * Emits the search signal through the {@link SearchPerformer} object.
	 * 
	 * @param queryId
	 *            the query for which the search signal has to be emitted.
	 */
	private void sendSearchSignal(String queryId) {
		log(TAG, "handler is asking for a search signal");
		QueryInfo qd = queries.get(queryId);
		if (qd != null) {
			performer.sendSearchSignal(qd);
		}
	}

	/**
	 * Schedules the next operation of the handler, according to the query
	 * settings. It uses and updates the information contained in the QueryInfo
	 * object. If the search operation is not terminated a message is sent to
	 * the handler with the proper delay; otherwise the query is eliminated and
	 * the event is notified to the {@link SearchPerformer}.
	 * 
	 * @param queryId
	 */
	private void scheduleNextAction(String queryId) {
		log(TAG, "handler is scheduling next action");
		QueryInfo qd = queries.get(queryId);
		if (qd == null) {
			return;
		}
		if (qd.getRemainingSignals() > 0) {
			Message msg = handler.obtainMessage(SearchMessages.SEND_SIGNAL,
					queryId);
			handler.sendMessageDelayed(msg, qd.getSignalPeriod());
			qd.decrementRemainingSignals();
		} else {
			Message message = handler.obtainMessage(SearchMessages.SEARCH_STOPPED,qd);
			handler.sendMessageDelayed(message, qd.getSignalPeriod());
		}
	}

	/**
	 * 
	 * Call this method when a search result signal is received from the network
	 * middleware. This method retrieves the query information and notifies the
	 * {@link SearchPerformer}. The result is added to the set of found
	 * instances to provide real time changes.
	 * 
	 * @param result
	 * 
	 */
	void onSearchResultReceived(SearchResult result) {
		log(TAG, "received search result for " + result.getQueryId()
				+ " uniqueIdentifier " + result.getUniqueIdentifier());
		QueryInfo qd = queries.get(result.getQueryId());
		if (qd == null) {
			return;
		}
		String uniqueIdentifier = result.getUniqueIdentifier();
		String queryId = qd.getQueryId();
		// retrieve the queries in which the result was dispatched
		List<String> dispatched_queries = results.get(result
				.getUniqueIdentifier());
		if (dispatched_queries == null) {
			dispatched_queries = new LinkedList<String>();
			results.put(uniqueIdentifier, dispatched_queries);
		}
		if (!dispatched_queries.contains(queryId)) {
			// the result was not already dispatched
			Log.d(TAG,
					"dispatching search result: queryId: "
							+ result.getQueryId() + " appName: "
							+ qd.getAppName());
			dispatched_queries.add(queryId);
			performer.dispatchSearchResult(qd, result);
		}
	}

	/**
	 * Call this method when the middleware notify that a spf instance is lost.
	 * It will notify the event to all the active searches that have the lost
	 * instance in their results.
	 * 
	 * @param uniqueIdentifier
	 *            the identifier of the lost instance
	 */
	void onInstanceLost(String uniqueIdentifier) {
		log(TAG, "instance lost " + uniqueIdentifier);
		List<String> queriesIds = results.get(uniqueIdentifier);
		if (queriesIds == null) {
			return;
		}
		for (String queryId : queriesIds) {
			String[] args = new String[2];
			args[0] = queryId;
			args[1] = uniqueIdentifier;
			Message msg = handler.obtainMessage(SearchMessages.RESULT_LOST,
					args);
			log(TAG, "sending message RESULT_LOST to handler for queryId: "
					+ queryId);
			handler.sendMessage(msg);
		}
	}

	/**
	 * Call this method to stop the search and release the associated resources.
	 * The application will not be notified about the event;
	 * 
	 * @param qd
	 * @return 
	 */
	void stopSearch(String queryId) {
		QueryInfo info = queries.get(queryId);
		if(info !=  null){
			stopSearch(info);
		}
	}

	private void stopSearch(QueryInfo queryInfo) {
		String queryId = queryInfo.getQueryId();
		log(TAG, "unregister search queryId: " + queryId);
		queries.remove(queryId);
		handler.removeMessages(SearchMessages.SEND_SIGNAL, queryId);
		List<String> userIds;
		synchronized (results) {
			userIds = new ArrayList<String>(results.keySet());
		}
		for (String uniqueIdentifier : userIds) {
			List<String> queryIds = results.get(uniqueIdentifier);
			if (queryIds != null) {
				queryIds.remove(queryId);
				if (queryIds.isEmpty()) {
					results.remove(uniqueIdentifier);
				}
			}
		}		
	}

	/**
	 * Unregister all the active queries associated with the given
	 * appIdentifier. Application will not be notified about the event.
	 */
	void stopAllSearches(String appIdentifier) {
		List<QueryInfo> qinfos;
		synchronized (queries) {
			qinfos = new ArrayList<QueryInfo>(queries.values());
		}
		for (QueryInfo queryInfo : qinfos) {
			if (queryInfo.getAppName().equals(appIdentifier)) {
				stopSearch(queryInfo);
			}
		}
	
	}

	private void log(String tag, String msg) {
		Log.d(tag, msg);
	}

}
