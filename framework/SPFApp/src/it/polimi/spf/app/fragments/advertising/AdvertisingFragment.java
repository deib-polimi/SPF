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
package it.polimi.spf.app.fragments.advertising;

import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.notification.SPFAdvProfile;
import it.polimi.spf.framework.notification.SPFAdvertisingManager;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.ProfileField;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

public class AdvertisingFragment extends Fragment {

	private SPFAdvertisingManager mAdvertiseManager = SPF.get().getAdvertiseManager();
	private boolean mSpinnerEnabled = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_advertising, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Set up switch
		Switch advSwitch = (Switch) getView().findViewById(R.id.advertising_switch);
		advSwitch.setOnCheckedChangeListener(mAdvertisingToggleListener);
		advSwitch.setChecked(mAdvertiseManager.isAdvertisingEnabled());
		
		// Set up persona spinner
		List<SPFPersona> personas = SPF.get().getProfileManager().getAvailablePersonas();
		ArrayAdapter<SPFPersona> adapter = new ArrayAdapter<SPFPersona>(getActivity(), android.R.layout.simple_list_item_1, personas);
		Spinner spinner = (Spinner) getView().findViewById(R.id.advertising_persona);
		spinner.setAdapter(adapter);
		SPFPersona persona = mAdvertiseManager.getPersonaToAdvertise();
		if(persona != null){
			int index = personas.indexOf(persona);
			spinner.setSelection(index, false);
		}
		spinner.setOnItemSelectedListener(mPersonaSelectedListener);
		mSpinnerEnabled = true;
		
		// Set up applications advertisement checkbox
		CheckBox checkbox = (CheckBox) getView().findViewById(R.id.advertising_application);
		checkbox.setChecked(mAdvertiseManager.isAdvertisingApplications());
		checkbox.setOnCheckedChangeListener(mApplicationToggleListener);
		
		// Set up field list
		ListView list = (ListView) getView().findViewById(R.id.advertising_field_list);
		ProfileField<?>[] choiches = {
			ProfileField.DISPLAY_NAME,
			ProfileField.INTERESTS,
			ProfileField.EMAILS,
			ProfileField.ABOUT_ME,
			ProfileField.STATUS,
			ProfileField.GENDER,
			ProfileField.LOCATION,
			ProfileField.BIRTHDAY
		};
		
		list.setAdapter(new ProfileFieldSelectAdapter(getActivity(), choiches));	
	}

	private final OnCheckedChangeListener mAdvertisingToggleListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				mAdvertiseManager.registerAdvertising();
			} else {
				mAdvertiseManager.unregisterAdvertising();
			}
		}
	};
	
	private final OnCheckedChangeListener mApplicationToggleListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mAdvertiseManager.setApplicationAdvertisingEnabled(isChecked);
		}
	};
	
	private OnItemSelectedListener mPersonaSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if(!mSpinnerEnabled){
				return;
			}
			
			SPFPersona persona = (SPFPersona) parent.getItemAtPosition(position);
			mAdvertiseManager.setPersonaToAdvertise(persona);
			
			SPFAdvProfile profile = mAdvertiseManager.generateAdvProfile();
			Log.d("Advertising", "Fields: " + profile.getFieldKeySet() + ", " + profile.getFieldsValues());
			Log.d("Advertising", "Applications: " + profile.getApplications());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// Do nothing
		}
	};
}