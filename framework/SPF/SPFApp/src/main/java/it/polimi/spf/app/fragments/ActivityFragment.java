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
package it.polimi.spf.app.fragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.services.ServiceIdentifier;
import it.polimi.spf.framework.services.VerbSupport;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class ActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Collection<VerbSupport>> {

	private static final int LOAD_LIST_LOADER_ID = 0;

	private VerbSupportAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_activities, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		ListView activities = findView(R.id.activities_list);
		activities.setEmptyView(findView(R.id.activities_list_emptyview));
		mAdapter = new VerbSupportAdapter(getActivity());
		activities.setAdapter(mAdapter);

		getLoaderManager().initLoader(LOAD_LIST_LOADER_ID, null, this).forceLoad();
	}

	@SuppressWarnings("unchecked")
	private <V extends View> V findView(int id) {
		return (V) getView().findViewById(id);
	}

	@Override
	public Loader<Collection<VerbSupport>> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_LIST_LOADER_ID:
			return new AsyncTaskLoader<Collection<VerbSupport>>(getActivity()) {

				@Override
				public Collection<VerbSupport> loadInBackground() {
					return SPF.get().getServiceRegistry().getVerbSupportList();
				}

			};

		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Collection<VerbSupport>> loader, Collection<VerbSupport> data) {
		switch (loader.getId()) {
		case LOAD_LIST_LOADER_ID:
			mAdapter.clear();
			mAdapter.addAll(data);
			break;

		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Collection<VerbSupport>> loader) {
		// Do nothing
	}

	private static class VerbSupportAdapter extends ArrayAdapter<VerbSupport> implements OnItemSelectedListener {

		public VerbSupportAdapter(Context c) {
			super(c, android.R.layout.simple_list_item_1);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(R.layout.activities_listelement, parent, false);
			ViewHolder holder = ViewHolder.from(view);
			VerbSupport item = getItem(position);
			ServiceIdentifierAdapter adapter = new ServiceIdentifierAdapter(getContext(), item.getSupportingServices());

			holder.verb.setText(item.getVerb());
			holder.appSelect.setAdapter(adapter);
			holder.appSelect.setTag(item.getVerb());
			holder.appSelect.setSelection(adapter.getPosition(item.getDefaultService()), false);
			holder.appSelect.setOnItemSelectedListener(this);
			return view;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			ServiceIdentifier identifier = (ServiceIdentifier) view.getTag();
			String verb = (String) parent.getTag();

			SPF.get().getServiceRegistry().setDefaultConsumerForVerb(verb, identifier);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing
		}
	}

	private static class ServiceIdentifierAdapter extends ArrayAdapter<ServiceIdentifier> {
		private Map<String, String> mAppNames;

		public ServiceIdentifierAdapter(Context c, Set<ServiceIdentifier> apps) {
			super(c, android.R.layout.simple_list_item_2);
			mAppNames = new HashMap<String, String>();
			addAll(apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
			ServiceIdentifier id = getItem(position);

			((TextView) v.findViewById(android.R.id.text1)).setText(id.getServiceName());
			((TextView) v.findViewById(android.R.id.text2)).setText(getAppName(id.getAppId()));
			v.setTag(id);

			return v;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent);
		}

		private String getAppName(String appId) {
			if (mAppNames.containsKey(appId)) {
				return mAppNames.get(appId);
			}

			try {
				PackageManager pm = getContext().getPackageManager();
				String name = pm.getApplicationLabel(pm.getApplicationInfo(appId, 0)).toString();
				mAppNames.put(appId, name);
				return name;
			} catch (NameNotFoundException e) {
				// This won't happen
				return null;
			}

		}
	}

	private static class ViewHolder {
		public TextView verb;
		public Spinner appSelect;

		public static ViewHolder from(View view) {
			Object o = view.getTag();
			if (o != null && (o instanceof ViewHolder)) {
				return (ViewHolder) o;
			}

			ViewHolder holder = new ViewHolder();
			view.setTag(holder);

			holder.verb = (TextView) view.findViewById(R.id.activities_verb_name);
			holder.appSelect = (Spinner) view.findViewById(R.id.activities_verb_apps);

			return holder;
		}

	}

}
