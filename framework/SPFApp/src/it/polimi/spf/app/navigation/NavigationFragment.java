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
package it.polimi.spf.app.navigation;

import it.polimi.spf.app.R;
import it.polimi.spf.app.navigation.Navigation.Entry;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.local.SPFService;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Base navigation fragment without drawer functionalities to be used in two
 * panes version of main activity
 * 
 * @author darioarchetti
 * 
 */
public class NavigationFragment extends Fragment {

	/**
	 * Interface for components (main activity) called when user an item is
	 * selected
	 * 
	 * @author darioarchetti
	 * 
	 */
	public static interface ItemSelectedListener {
		public void onItemSelect(int position, boolean replace);
	}

	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	private ListView mNavigationListView;
	private int mCurrentSelectedPosition = 0;
	private ItemSelectedListener mCallback;
	private Navigation mNavigation;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (ItemSelectedListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
		}
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

		// Set up navigation entries
		mNavigationListView = (ListView) root.findViewById(R.id.navigation_entries);
		mNavigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position, true);
			}
		});

		String[] pageTitles = getResources().getStringArray(R.array.content_fragments_titles);
		mNavigationListView.setAdapter(new NavigationArrayAdapter(getActivity(), pageTitles));
		mNavigationListView.setItemChecked(mCurrentSelectedPosition, true);

		// Set up connect switch
		Switch connectSwitch = (Switch) root.findViewById(R.id.connect_switch);
		connectSwitch.setChecked(SPF.get().isConnected());

		connectSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					SPFService.startForeground(getActivity());
				} else {
					SPFService.stopForeground(getActivity());
				}
			}
		});

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		selectItem(mCurrentSelectedPosition, savedInstanceState == null);
		mNavigation = new Navigation(getActivity());
		SPFContext.get().registerEventListener(mNavigation);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		SPFContext.get().unregisterEventListener(mNavigation);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	public boolean hasOptionsMenu() {
		return false;
	}

	protected void selectItem(int position, boolean replace) {
//		mNavigationListView.setItemChecked(mCurrentSelectedPosition, false);
//		mNavigationListView.setItemChecked(position, true);
		mCurrentSelectedPosition = position;

		if (mNavigationListView == null || mCallback == null) {
			return;
		}

		mCallback.onItemSelect(position, replace);
	}

	protected ActionBar getActionBar() {
		return getActivity().getActionBar();
	}

	private class NavigationArrayAdapter extends ArrayAdapter<String> {

		public NavigationArrayAdapter(Context context, String[] pageTitles) {
			super(context, android.R.layout.simple_list_item_1, pageTitles);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return mNavigation.createEntryView(Entry.values()[position]);
		}
	}
}
