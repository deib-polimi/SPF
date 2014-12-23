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

import java.util.Collection;

import it.polimi.spf.app.R;
import it.polimi.spf.app.R.id;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.DefaultCircles;
import it.polimi.spf.framework.security.PersonRegistry;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CircleFragment extends Fragment implements OnClickListener, LoaderManager.LoaderCallbacks<Collection<String>> {

	private static final int LOAD_CIRCLE_LOADER = 0;
	private static final int ADD_CIRCLE_LOADER = 1;
	private static final int DELETE_CIRCLE_LOADER = 2;
	protected static final String EXTRA_CIRCLE = "circle";

	private CircleArrayAdapter mAdapter;
	private EditText mNewCircleName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.contacts_circle_page, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView circleList = (ListView) getView().findViewById(R.id.contacts_circle_list);
		circleList.setEmptyView(getView().findViewById(R.id.contacts_circle_emptyview));

		mAdapter = new CircleArrayAdapter(getActivity());
		circleList.setAdapter(mAdapter);

		mNewCircleName = (EditText) getView().findViewById(R.id.contacts_circle_add_name);
		ImageButton addButton = (ImageButton) getView().findViewById(R.id.contacts_circle_add_button);
		addButton.setOnClickListener(this);
		
		startLoader(LOAD_CIRCLE_LOADER, null, false);
	}

	private class CircleArrayAdapter extends ArrayAdapter<String> {

		public CircleArrayAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView != null ? convertView : LayoutInflater.from(getContext()).inflate(R.layout.personas_listelement, parent, false);
			ViewHolder holder = ViewHolder.from(view);

			String item = getItem(position);
			holder.name.setText(item);

			if (DefaultCircles.isDefault(item)) {
				holder.deletebutton.setVisibility(View.GONE);
			} else {
				holder.deletebutton.setVisibility(View.VISIBLE);
				holder.deletebutton.setOnClickListener(CircleFragment.this);
				holder.deletebutton.setTag(item);
			}
			return view;
		}

		@Override
		public boolean isEnabled(int position) {
			return false; // Prevent click on item
		}

	}

	private static class ViewHolder {

		public static ViewHolder from(View view) {
			Object o = view.getTag();
			if (o != null & (o instanceof ViewHolder)) {
				return (ViewHolder) o;
			}

			ViewHolder holder = new ViewHolder();
			view.setTag(holder);

			holder.name = (TextView) view.findViewById(R.id.personas_entry_name);
			holder.deletebutton = (ImageButton) view.findViewById(id.personas_entry_delete);

			return holder;
		}

		public TextView name;
		public ImageButton deletebutton;

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contacts_circle_add_button: {
			String newCircleName = mNewCircleName.getText().toString();
			if (newCircleName.length() == 0) {
				makeToast("Circle name must not be empty");
				return;
			}

			if (mAdapter.getPosition(newCircleName) > -1) {
				makeToast("A circle with this name already exists");
				return;
			}

			mNewCircleName.setText("");

			Bundle args = new Bundle();
			args.putString(EXTRA_CIRCLE, newCircleName);
			startLoader(ADD_CIRCLE_LOADER, args, true);
			return;
		}
		case R.id.personas_entry_delete: {
			Bundle args = new Bundle();
			args.putString(EXTRA_CIRCLE, (String) v.getTag());
			startLoader(DELETE_CIRCLE_LOADER, args, true);
			return;
		}
		}
	}

	private void makeToast(String string) {
		Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();
	}

	@Override
	public Loader<Collection<String>> onCreateLoader(int id, final Bundle args) {
		final PersonRegistry registry = SPF.get().getSecurityMonitor().getPersonRegistry();
		switch (id) {
		case LOAD_CIRCLE_LOADER:
			return new AsyncTaskLoader<Collection<String>>(getActivity()) {

				@Override
				public Collection<String> loadInBackground() {
					return registry.getCircles();
				}
			};
		case ADD_CIRCLE_LOADER:
			return new AsyncTaskLoader<Collection<String>>(getActivity()) {

				@Override
				public Collection<String> loadInBackground() {
					String circle = args.getString(EXTRA_CIRCLE);
					registry.addCircle(circle);
					return registry.getCircles();
				}
			};
		case DELETE_CIRCLE_LOADER:
			return new AsyncTaskLoader<Collection<String>>(getActivity()) {

				@Override
				public Collection<String> loadInBackground() {
					String circle = args.getString(EXTRA_CIRCLE);
					registry.removeCircle(circle);
					return registry.getCircles();
				}
			};

		default:
			return null;
		}
	}

	private void startLoader(int id, Bundle args, boolean destroyPrevious){
		if(destroyPrevious){
			getLoaderManager().destroyLoader(id);
		}
		getLoaderManager().initLoader(id, args, this).forceLoad();
	}
	
	@Override
	public void onLoadFinished(Loader<Collection<String>> loader, Collection<String> data) {
		mAdapter.clear();
		mAdapter.addAll(data);
	}

	@Override
	public void onLoaderReset(Loader<Collection<String>> loader) {
		// Do nothing
	}
}
