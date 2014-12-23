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

import it.polimi.spf.app.R;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.notification.SPFAdvertisingManager;
import it.polimi.spf.shared.model.ProfileField;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;

public class AdvertisingFragment extends Fragment implements OnCheckedChangeListener{

	private SPFAdvertisingManager mAdvertiseManager = SPF.get().getAdvertiseManager();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.content_fragment_advertising, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
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
		
		Switch advSwitch = (Switch) getView().findViewById(R.id.advertising_switch);
		advSwitch.setOnCheckedChangeListener(this);
		advSwitch.setChecked(mAdvertiseManager.isAdvertisingEnabled());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			mAdvertiseManager.registerAdvertising();
		} else {
			mAdvertiseManager.unregisterAdvertising();
		}
	}
}