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
package it.polimi.spf.lib.search;

import java.util.Hashtable;
import java.util.Map;
import android.content.Context;
import android.os.RemoteException;
import android.util.SparseArray;
import it.polimi.spf.lib.LooperUtils;
import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.shared.aidl.SPFSearchCallback;
import it.polimi.spf.shared.model.BaseInfo;
import it.polimi.spf.shared.model.SPFSearchDescriptor;

/**
 * A {@link SPFComponent} that allows applications to search for remote people
 * who match given criteria.
 * 
 */
public final class SPFSearch {

	private SparseArray<String> mTagToId;
	private SPF mSearchInterface;
	private Map<String, SearchCallback> mCallbacks;

	// TODO @hide
	public SPFSearch(Context context, SPF searchInterface) {
		mTagToId = new SparseArray<String>();
		mCallbacks = new Hashtable<String, SPFSearch.SearchCallback>();
		mSearchInterface = searchInterface;
	}

	/**
	 * Starts a search for remote people. The application must provide a tag,
	 * used to replace equivalent queries, a {@link SearchDescriptor} containing
	 * the configuration of the search, and a {@link SearchCallback} to be
	 * notified of found results.
	 * 
	 * @param tag
	 *            - the tag identifying this query
	 * @param searchDescriptor
	 *            - the descriptor of the search
	 * @param callback
	 *            - the callback to be notified of results.
	 */
	public void startSearch(final int tag,
			SPFSearchDescriptor searchDescriptor, final SearchCallback callback) {
		// replace equivalent query ( equivalent == same tag )
		if (mTagToId.get(tag) != null) {
			String queryId = mTagToId.get(tag);
			mTagToId.delete(tag);
			if (queryId != null && mCallbacks.remove(queryId) != null) {
				mSearchInterface.stopSearch(queryId);
			}
		}
		mSearchInterface.startSearch(searchDescriptor,
				new SPFSearchCallbackImpl(callback, tag));
	}

	/**
	 * Stops a previously registered search request performed by the
	 * application. The application must provide the tag it registered the
	 * search with. The callback associated to the search does not receive any
	 * further notification.
	 * 
	 * @param tag
	 *            - the tag used to register the search.
	 */
	public void stopSearch(int tag) {
		String queryId = mTagToId.get(tag);
		mTagToId.delete(tag);
		if (queryId != null && mCallbacks.remove(queryId) != null) {
			mSearchInterface.stopSearch(queryId);
		}
	}

	/**
	 * Stops all searches registered by the application.
	 * 
	 * @see SPFSearch#stopSearch(int)
	 */
	public void stopAllSearches() {
		mTagToId.clear();
		String[] queryIds = mCallbacks.keySet().toArray(new String[]{});
		mCallbacks.clear();
		for (String queryId : queryIds) {
			mSearchInterface.stopSearch(queryId);
		}
	}

	/**
	 * Allows to retrieve a reference to a remote person given its identifier.
	 * This reference is valid until the given person is reachable from the
	 * proximity middleware.
	 * 
	 * @param identifier
	 * @return
	 */
	public SPFPerson lookup(String identifier) {
		boolean isReachable = mSearchInterface.lookup(identifier);
		if (isReachable) {
			return new SPFPerson(identifier);
		} else {
			return null;
		}
	}

	void onResultLost(String queryId, String uniqueIdentifier) {
		SearchCallback callback = mCallbacks.get(queryId);
		if (callback == null) {
			return;
		}
		callback.onPersonLost(new SPFPerson(uniqueIdentifier));
	}

	void onResultFound(String queryId, String uniqueIdentifier,
			BaseInfo baseInfo) {
		SearchCallback callback = mCallbacks.get(queryId);
		if (callback == null) {
			mSearchInterface.stopSearch(queryId);
			return;
		}
		SPFPerson p = new SPFPerson(uniqueIdentifier, baseInfo);
		callback.onPersonFound(p);

	}

	void onStop(String queryId) {
		SearchCallback callback = mCallbacks.get(queryId);
		if (callback == null) {
			return;
		}
		// TODO remove tag
		mCallbacks.remove(queryId);
		callback.onSearchStop();
	}

	void onError(String queryId) {
		SearchCallback callback = mCallbacks.get(queryId);
		if (callback == null) {
			mSearchInterface.stopSearch(queryId);
			return;
		}

		callback.onSearchError();
	}

	void onSearchStart(String queryId) {
		SearchCallback callback = mCallbacks.get(queryId);
		if (callback == null) {
			mSearchInterface.stopSearch(queryId);
			return;
		}
		callback.onSearchStart();
	}

	public void recycle() {
		stopAllSearches();
	}

	/**
	 * Interface for components that can receive updates on a registered search
	 * request.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public interface SearchCallback {
		/**
		 * Called when a person matching the criteria of the search is found.
		 * 
		 * @param p
		 *            - the found person.
		 */
		void onPersonFound(SPFPerson p);

		/**
		 * Called when the contact with a previously found person is lost.
		 * 
		 * @param p
		 *            - the person we are no more in contact with.
		 */
		void onPersonLost(SPFPerson p);

		/**
		 * Called when the search is stopped.
		 */
		void onSearchStop();

		/**
		 * Called when an error occurs during the search.
		 */
		void onSearchError();

		void onSearchStart();
	}

	private final class SPFSearchCallbackImpl extends SPFSearchCallback.Stub {
		private final SearchCallback callback;
		private final int tag;
		private SPFSearchCallbackImpl(SearchCallback callback, int tag) {
			this.callback = callback;
			this.tag = tag;
		}
		@Override
		public void onSearchStop(String queryId) throws RemoteException {
			SPFSearch.this.onStop(queryId);
		}
		@Override
		public void onSearchStart(String queryId) throws RemoteException {
			if (queryId == null) {
				callback.onSearchError();
				return;
			}
			SearchCallback uiCallback = LooperUtils.onMainThread(
					SearchCallback.class, callback);
			mCallbacks.put(queryId, uiCallback);
			mTagToId.put(tag, queryId);
			SPFSearch.this.onSearchStart(queryId);
		}
		@Override
		public void onSearchResultReceived(String queryId, String userId,
				BaseInfo baseInfo) throws RemoteException {
			SPFSearch.this.onResultFound(queryId, userId, baseInfo);
		}
		@Override
		public void onSearchResultLost(String queryId, String userId)
				throws RemoteException {
			SPFSearch.this.onResultLost(queryId, userId);
		}
		@Override
		public void onSearchError(String queryId) throws RemoteException {
			SPFSearch.this.onError(queryId);
		}
	}

}
