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
package it.polimi.spf.app.fragments.contacts;

import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.PersonInfo;
import it.polimi.spf.framework.security.PersonRegistry;
import it.polimi.spf.framework.security.TokenCipher.WrongPassphraseException;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PeopleFragment extends Fragment implements OnItemClickListener, SPFContext.OnEventListener, LoaderManager.LoaderCallbacks<List<PersonInfo>> {

	private static final int LOAD_CONTACTS_LOADER = 0;
	private static final int LOAD_REQUEST_LOADER = 1;

	private PersonRegistry mPersonRegistry = SPF.get().getSecurityMonitor().getPersonRegistry();
	private FriendListArrayAdapter mFriendsAdapter, mRequestAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.contacts_people_page, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView friendList = (ListView) getView().findViewById(R.id.contacts_people_listview);
		mFriendsAdapter = new FriendListArrayAdapter(getActivity());
		friendList.setAdapter(mFriendsAdapter);
		friendList.setEmptyView(getView().findViewById(R.id.contacts_people_emptyview));
		friendList.setOnItemClickListener(this);

		ListView requestList = (ListView) getView().findViewById(R.id.contacts_people_requests_listview);
		mRequestAdapter = new FriendListArrayAdapter(getActivity());
		requestList.setAdapter(mRequestAdapter);
		requestList.setEmptyView(getView().findViewById(R.id.contacts_people_requests_emptyview));
		requestList.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		onRefresh();
		SPFContext.get().registerEventListener(this);
	}

	private void onRefresh(){
		getLoaderManager().initLoader(LOAD_CONTACTS_LOADER, null, PeopleFragment.this).forceLoad();
		getLoaderManager().initLoader(LOAD_REQUEST_LOADER, null, PeopleFragment.this).forceLoad();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		SPFContext.get().unregisterEventListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PersonInfo entry = (PersonInfo) parent.getItemAtPosition(position);
		switch (parent.getId()) {
		case R.id.contacts_people_listview:
			onFriendSelected(entry);
			break;
		case R.id.contacts_people_requests_listview:
			onRequestReview(entry);
			break;
		}
	}

	@Override
	public void onEvent(int eventCode, Bundle payload) {
		if (eventCode == SPFContext.EVENT_CONTACT_REQUEST_RECEIVED) {
			getLoaderManager().initLoader(LOAD_REQUEST_LOADER, null, PeopleFragment.this).forceLoad();
		}
	}

	private void onFriendSelected(PersonInfo entry) {
		Intent i = new Intent(getActivity(), ContactEditActivity.class);
		i.putExtra(ContactEditActivity.PERSON_IDENTIFER_EXTRA, entry.getIdentifier());
		startActivity(i);
	}

	private void onRequestReview(final PersonInfo entry) {
		final ContactConfirmDialogView dialogView = new ContactConfirmDialogView(getActivity(), entry);
		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle("Confirm contact request")
			.setView(dialogView)
			.setPositiveButton("Accept", null)
			.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setNegativeButton("Refuse", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mPersonRegistry.deleteRequest(entry);
					onRefresh();
				}

			}).show();
		
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					mPersonRegistry.confirmRequest(entry, dialogView.getPassphrase(), dialogView.getSelectedCircles());
				} catch (WrongPassphraseException e) {
					Toast.makeText(getActivity(), "Wrong passphrase", Toast.LENGTH_SHORT).show();
					return;
				}

				dialog.dismiss();
				onRefresh();
			}
		});
	}

	@Override
	public Loader<List<PersonInfo>> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_CONTACTS_LOADER:
			return new AsyncTaskLoader<List<PersonInfo>>(getActivity()) {

				@Override
				public List<PersonInfo> loadInBackground() {
					return SPF.get().getSecurityMonitor().getPersonRegistry().getAvailableContacts();
				}
			};
		case LOAD_REQUEST_LOADER:
			return new AsyncTaskLoader<List<PersonInfo>>(getActivity()) {

				@Override
				public List<PersonInfo> loadInBackground() {
					return SPF.get().getSecurityMonitor().getPersonRegistry().getPendingRequests();
				}
			};
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<List<PersonInfo>> loader, List<PersonInfo> data) {
		switch (loader.getId()) {
		case LOAD_CONTACTS_LOADER:
			mFriendsAdapter.clear();
			mFriendsAdapter.addAll(data);
			break;
		case LOAD_REQUEST_LOADER:
			mRequestAdapter.clear();
			mRequestAdapter.addAll(data);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<List<PersonInfo>> loader) {
		// Do nothing
	}
}