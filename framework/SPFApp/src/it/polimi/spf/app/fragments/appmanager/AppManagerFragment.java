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
package it.polimi.spf.app.fragments.appmanager;

import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.AppAuth;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class AppManagerFragment extends Fragment implements ListView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<AppAuth>> {

	
	private static final int APP_LOADER = 0;
	private AppManagerListAdapter mAdapter;
	private ListView mAppList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_appmanager, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new AppManagerListAdapter(getActivity());
		mAppList = (ListView) getView().findViewById(R.id.app_manager_list);
		mAppList.setAdapter(mAdapter);
		mAppList.setEmptyView(getView().findViewById(R.id.app_manager_list_emptyview));
		mAppList.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(APP_LOADER, null, this).forceLoad();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppAuth auth = mAdapter.getItem(position);
		Intent i = new Intent(getActivity(), AppDetailActivity.class);
		i.putExtra(AppDetailActivity.APP_AUTH_KEY, auth);
		startActivity(i);
	}

	@Override
	public Loader<List<AppAuth>> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case APP_LOADER:
			return new AsyncTaskLoader<List<AppAuth>>(getActivity()) {

				@Override
				public List<AppAuth> loadInBackground() {
					return SPF.get().getSecurityMonitor().getAvailableApplications();
				}
			};

		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<List<AppAuth>> loader, List<AppAuth> items) {
		mAdapter.clear();
		mAdapter.addAll(items);
	}

	@Override
	public void onLoaderReset(Loader<List<AppAuth>> loader) {
	}

}