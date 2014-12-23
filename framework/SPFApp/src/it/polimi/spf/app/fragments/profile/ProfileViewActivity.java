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
package it.polimi.spf.app.fragments.profile;

import it.polimi.spf.app.R;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ProfileViewActivity extends Activity {

	private static final String PERSON_IDENTIFIER_KEY = "personIdentifier";

	Fragment mFragment;

	public static Intent getIntent(Context context, String uuid) {
		if (context == null || uuid == null) {
			throw new NullPointerException();
		}

		Intent i = new Intent(context, ProfileViewActivity.class);
		i.putExtra(PERSON_IDENTIFIER_KEY, uuid);
		return i;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_view);
		String identifier = getIntent().getStringExtra(PERSON_IDENTIFIER_KEY);

		if (identifier == null) {
			mFragment = ProfileFragment.createViewSelfProfileFragment();
		} else {
			mFragment = ProfileFragment.createRemoteProfileFragment(identifier);
		}

		getFragmentManager().beginTransaction().add(R.id.container, mFragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mFragment.onCreateOptionsMenu(menu, getMenuInflater());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mFragment.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		mFragment.onPrepareOptionsMenu(menu);
		return true;
	}
}
