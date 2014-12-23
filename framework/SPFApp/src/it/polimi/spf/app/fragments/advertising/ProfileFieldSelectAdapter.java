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
import it.polimi.spf.app.fragments.profile.Helper;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.notification.SPFAdvertisingManager;
import it.polimi.spf.shared.model.ProfileField;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ProfileFieldSelectAdapter extends ArrayAdapter<ProfileField<?>> implements OnCheckedChangeListener {

	private Helper mHelper;
	
	public ProfileFieldSelectAdapter(Context context, ProfileField<?>[] choiches) {
		super(context, android.R.layout.simple_list_item_1, choiches);
		mHelper = new Helper(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView != null ? convertView :
			LayoutInflater.from(getContext()).inflate(R.layout.advertising_fieldselect_listelement, parent, false);
		
		ViewHolder holder = ViewHolder.from(view);
		ProfileField<?> field = getItem(position);
		List<String> selectedFields = SPF.get().getAdvertiseManager().getFieldIdentifiers();
		
		holder.checkBox.setText(mHelper.getFriendlyNameOfField(field));
		holder.checkBox.setOnCheckedChangeListener(this);
		holder.checkBox.setTag(field);
		
		if(selectedFields.contains(field.getIdentifier())){
			holder.checkBox.setChecked(true);
		}
		
		return view;
	}

	private static class ViewHolder {
		public CheckBox checkBox;

		public static ViewHolder from(View v) {
			Object o = v.getTag();

			if (o != null && (o instanceof ViewHolder)) {
				return (ViewHolder) o;
			}

			ViewHolder holder = new ViewHolder();
			holder.checkBox = (CheckBox) v.findViewById(R.id.fieldselect_checkbox);
			v.setTag(holder);
			return holder;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ProfileField<?> field = (ProfileField<?>) buttonView.getTag();
		SPFAdvertisingManager manager = SPF.get().getAdvertiseManager();
		if(isChecked){
			manager.addFieldToAdvertising(field);
		} else {
			manager.removeFieldFromAdvertising(field);
		}
	}

}
