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
package it.polimi.spf.app.fragments.personas;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.profile.SPFPersona;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class PersonasCirclesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_persona_circles);
		SPFPersona persona = getIntent().getParcelableExtra("persona");
		PersonasCirclesFragment fragment = PersonasCirclesFragment.newInstance(persona);
		getFragmentManager().beginTransaction().replace(R.id.activity_persona_circles_container, fragment).commit();

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public static void start(Activity callingActivity, SPFPersona persona) {
		Intent intent = new Intent(callingActivity, PersonasCirclesActivity.class);
		intent.putExtra("persona", persona);
		callingActivity.startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
