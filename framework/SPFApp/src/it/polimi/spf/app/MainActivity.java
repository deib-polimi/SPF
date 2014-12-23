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
package it.polimi.spf.app;

import it.polimi.spf.app.fragments.ActivityFragment;
import it.polimi.spf.app.fragments.NotificationFragment;
import it.polimi.spf.app.fragments.advertising.AdvertisingFragment;
import it.polimi.spf.app.fragments.appmanager.AppManagerFragment;
import it.polimi.spf.app.fragments.contacts.ContactsFragment;
import it.polimi.spf.app.fragments.personas.PersonasFragment;
import it.polimi.spf.app.fragments.profile.ProfileFragment;
import it.polimi.spf.app.navigation.NavigationDrawerFragment;
import it.polimi.spf.app.navigation.NavigationFragment;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity implements NavigationFragment.ItemSelectedListener {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	/**
	 * Array that contains the names of sections
	 */
	private String[] mSectionNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTitle = getTitle();
		mSectionNames = getResources().getStringArray(R.array.content_fragments_titles);
		mNavigationDrawerFragment = (NavigationFragment) getFragmentManager().findFragmentById(R.id.navigation);

		getFragmentManager().executePendingTransactions();

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawerLayout != null) {
			// We are in two panes mode, thus set up the drawer.
			((NavigationDrawerFragment) mNavigationDrawerFragment).setUpDrawer(R.id.navigation, drawerLayout);
		}
	}

	@Override
	public void onItemSelect(int position, boolean replace) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		mTitle = getPageTitle(position);
		if (replace) {
			fragmentManager.beginTransaction().replace(R.id.container, createFragment(position)).commit();
		}
		invalidateOptionsMenu();
	}

	private String getPageTitle(int position) {
		return mSectionNames[position];
	}

	private Fragment createFragment(int position) {
		switch (position) {
		case 0:
			// Displays the profile
			return ProfileFragment.createViewSelfProfileFragment();
		case 1:
			// Displays available personas
			return new PersonasFragment();
		case 2:
			// Displays the list of friends
			return new ContactsFragment();
		case 3:
			// Displays the list of notifications
			return new NotificationFragment();
		case 4:
			// Displays advertising options
			return new AdvertisingFragment();
		case 5:
			// Displays the list of apps authorized to interact with SPF
			return new AppManagerFragment();
		case 6:
			return new ActivityFragment();
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		restoreActionBar();
		if (mNavigationDrawerFragment.hasOptionsMenu()) {
			mNavigationDrawerFragment.onCreateOptionsMenu(menu, getMenuInflater());
			return true;
		}

		getCurrentFragment().onCreateOptionsMenu(menu, getMenuInflater());
		return true;

	}

	private Fragment getCurrentFragment() {
		getFragmentManager().executePendingTransactions();
		return getFragmentManager().findFragmentById(R.id.container);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mNavigationDrawerFragment.hasOptionsMenu()) {
			mNavigationDrawerFragment.onPrepareOptionsMenu(menu);
			return true;
		}

		getCurrentFragment().onPrepareOptionsMenu(menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mNavigationDrawerFragment.onOptionsItemSelected(item)) {
			return true;
		}

		return getCurrentFragment().onOptionsItemSelected(item);
	}
}