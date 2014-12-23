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
package it.polimi.spf.app.fragments.appmanager;

import it.polimi.spf.shared.model.Permission;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PermissionArrayAdapter extends ArrayAdapter<Permission> {
	public PermissionArrayAdapter(Context context, Permission[] permissions) {
		super(context, android.R.layout.simple_list_item_1, permissions);

		if (context == null || permissions == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView == null ? LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false) : convertView;

		Permission p = getItem(position);
		ViewHolder.from(v).textView.setText(PermissionNameHelper.getPermissionFriendlyName(p, getContext()));
		return v;
	}

	private static class ViewHolder {
		public TextView textView;

		public static ViewHolder from(View v) {
			Object o = v.getTag();
			if (o != null && (o instanceof ViewHolder)) {
				return (ViewHolder) o;
			}

			ViewHolder holder = new ViewHolder();
			v.setTag(holder);
			holder.textView = (TextView) v.findViewById(android.R.id.text1);
			return holder;
		}
	}

}
