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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.app.view.CirclePicker;
import it.polimi.spf.app.view.PersonCard;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.PersonInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ContactConfirmDialogView extends LinearLayout {

	private CirclePicker mCirclePicker;
	private PersonCard mPersonCard;
	private EditText mPassphrase;

	public ContactConfirmDialogView(Context context, PersonInfo personInfo) {
		super(context);
		init(context);
		mPersonCard.show(personInfo);
	}

	public ContactConfirmDialogView(Context context, String displayName, Bitmap picture) {
		super(context);
		init(context);
		mPersonCard.setName(displayName);
		if(picture == null){
			mPersonCard.setPictureFromResource(R.drawable.empty_profile_picture);
		} else {
			mPersonCard.setPicture(new BitmapDrawable(getResources(), picture));
		}
	}

	private void init(Context context) {
		inflate(context, R.layout.contacts_people_confirm_alert, this);
		mPersonCard = (PersonCard) findViewById(R.id.contacts_people_confirm_card);

		mCirclePicker = (CirclePicker) findViewById(R.id.contacts_people_confirm_circles);
		Collection<String> availableCircles = SPF.get().getSecurityMonitor().getPersonRegistry().getCircles();
		mCirclePicker.setCircles(null, new ArrayList<String>(availableCircles));
		
		mPassphrase = (EditText) findViewById(R.id.contacts_people_confirm_passphrase);
	}

	public List<String> getSelectedCircles() {
		return mCirclePicker.getSelectedCircles();
	}
	
	public String getPassphrase(){
		return mPassphrase.getText().toString();
	}
}
