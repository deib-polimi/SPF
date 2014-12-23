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
package it.polimi.spf.app.view;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.security.ContactsDetailStorage;
import it.polimi.spf.framework.security.PersonInfo;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PersonCard extends LinearLayout {

	private ImageView mPicView;
	private TextView mNameView;

	public PersonCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PersonCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PersonCard(Context context) {
		super(context);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_person_card, this);
		
		mPicView = (ImageView) findViewById(R.id.person_card_picture);
		mNameView = (TextView) findViewById(R.id.person_card_name);
	}

	public void setName(String name) {
		mNameView.setText(name);
	}

	public void setPictureFromResource(int resourceId) {
		mPicView.setBackgroundResource(resourceId);
	}

	public void setPicture(Drawable drawable) {
		mPicView.setBackground(drawable);
	}

	public void show(PersonInfo personInfo) {
		mNameView.setText(personInfo.getDisplayName());
		mPicView.setBackground(ContactsDetailStorage.getProfilePicture(getContext(), personInfo));
	}
}
