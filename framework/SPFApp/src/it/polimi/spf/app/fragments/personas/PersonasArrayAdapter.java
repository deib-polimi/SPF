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
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class PersonasArrayAdapter extends ArrayAdapter<SPFPersona> implements OnClickListener {

	public static interface OnPersonaDeletedListener {
		public void onPersonaDeleted(SPFPersona persona);
	}

	private final OnPersonaDeletedListener mListener;

	public PersonasArrayAdapter(Context context, OnPersonaDeletedListener listener) {
		super(context, android.R.layout.simple_list_item_1);
		mListener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView != null ? convertView :
			LayoutInflater.from(getContext()).inflate(R.layout.personas_listelement, parent, false);
		
		ViewHolder holder = ViewHolder.from(view);
		SPFPersona persona = getItem(position);
		holder.name.setText(persona.getIdentifier());
		if(persona.isDefault()){
			holder.deleteButton.setVisibility(View.GONE);
			holder.deleteButton.setTag(null);
		} else {
			holder.deleteButton.setVisibility(View.VISIBLE);
			holder.deleteButton.setTag(persona);
			holder.deleteButton.setOnClickListener(this);
		}
		
		return view;
	}

	@Override
	public void onClick(View v) {
		SPFPersona persona = (SPFPersona) v.getTag();
		if (mListener != null) {
			mListener.onPersonaDeleted(persona);
		}
	}
	
	@Override
	public boolean isEnabled(int position) {
		
		return true;
	}
	
	private static class ViewHolder {

		public ImageButton deleteButton;
		public TextView name;

		public static ViewHolder from(View view) {
			Object o = view.getTag();
			if(o != null && (o instanceof ViewHolder)){
				return (ViewHolder) o;
			}
			
			ViewHolder holder = new ViewHolder();
			view.setTag(holder);
			holder.name = (TextView) view.findViewById(R.id.personas_entry_name);
			holder.deleteButton = (ImageButton) view.findViewById(R.id.personas_entry_delete);
			return holder;
		}

	}



}
