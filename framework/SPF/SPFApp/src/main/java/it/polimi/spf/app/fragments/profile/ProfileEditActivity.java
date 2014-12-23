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
import it.polimi.spf.framework.profile.SPFPersona;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ProfileEditActivity extends Activity {

	public static final String EXTRA_PERSONA = "persona";

	private ProfileFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_edit);

		if (savedInstanceState == null) {
			SPFPersona persona = getIntent().getExtras().getParcelable(EXTRA_PERSONA);
			mFragment = ProfileFragment.createEditSelfProfileFragment(persona);
			getFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();
		} else {
			mFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.container);
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mFragment.onCreateOptionsMenu(menu, getMenuInflater());
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return mFragment.onOptionsItemSelected(item);
		}
	}

	@Override
	public void finish() {
		setResult(mFragment.isContainerModifiedAtLeastOnce() ? RESULT_OK : RESULT_CANCELED);
		
		if (mFragment.isContainerModified()) {
			new AlertDialog.Builder(this).setMessage(R.string.profileedit_confirm_message).setPositiveButton(R.string.profileedit_confirm_yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					setResult(RESULT_CANCELED);
					ProfileEditActivity.super.finish();
				}
			}).setNegativeButton(R.string.profileedit_confirm_no, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
		} else {
			super.finish();
		}
	}
}