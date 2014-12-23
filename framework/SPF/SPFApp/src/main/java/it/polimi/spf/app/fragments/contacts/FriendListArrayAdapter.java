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

import it.polimi.spf.app.view.PersonCard;
import it.polimi.spf.framework.security.PersonInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class FriendListArrayAdapter extends ArrayAdapter<PersonInfo> {

	public FriendListArrayAdapter(Context context) {
		super(context, android.R.layout.simple_expandable_list_item_1);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PersonCard card = convertView == null ? new PersonCard(getContext()) : (PersonCard) convertView;
		card.show(getItem(position));
		return card;
	}
}
