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

import it.polimi.spf.app.R;
import it.polimi.spf.app.view.CirclePicker;
import it.polimi.spf.app.view.PersonCard;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.PersonInfo;
import it.polimi.spf.framework.security.PersonRegistry;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ContactEditActivity extends Activity implements CirclePicker.ChangeListener {

	public static final String PERSON_IDENTIFER_EXTRA = "personIdentifier";
	private PersonRegistry mPersonRegistry;
	private String mPersonIdentifier;
	private CirclePicker mCirclePicker;
	private PersonInfo mInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_edit);

		Bundle b;
		if (savedInstanceState == null) {
			b = getIntent().getExtras();
		} else {
			b = savedInstanceState;
		}

		if (!b.containsKey(PERSON_IDENTIFER_EXTRA)) {
			throw new IllegalStateException("Cant' find person identifier");
		}

		this.mPersonIdentifier = b.getString(PERSON_IDENTIFER_EXTRA);
		this.mPersonRegistry = SPF.get().getSecurityMonitor().getPersonRegistry();
		this.mInfo = mPersonRegistry.lookup(mPersonIdentifier);

		PersonCard personCard = (PersonCard) findViewById(R.id.contacts_person_card);
		personCard.show(mInfo);

		mCirclePicker = (CirclePicker) findViewById(R.id.contacts_person_circle_picker);
		mCirclePicker.setOnChangeListener(this);

		List<String> selectedCircles = new ArrayList<String>(mInfo.getPersonAuth().getCircles());
		List<String> selectableCircles = new ArrayList<String>(mPersonRegistry.getCircles());
		mCirclePicker.setCircles(selectedCircles, selectableCircles);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit_contact, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.contacts_delete_contact:
			new AlertDialog.Builder(this)
				.setTitle("Confirm removal")
				.setMessage("Do you want to remove " + mInfo.getDisplayName() + " from your contact list?")
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SPF.get().getSecurityMonitor().getPersonRegistry().deletePerson(mInfo.getIdentifier());
						finish();
					}
				}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	@Override
	public void onCircleAdded(String tag) {
		mPersonRegistry.addPersontoCircle(mPersonIdentifier, tag);
	}

	@Override
	public void onCircleRemoved(String tag) {
		mPersonRegistry.removePersonFromCircle(mPersonIdentifier, tag);
	}
}
