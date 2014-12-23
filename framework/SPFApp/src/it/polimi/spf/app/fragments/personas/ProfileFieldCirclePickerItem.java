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

import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.app.fragments.profile.Helper;
import it.polimi.spf.app.view.CirclePicker;
import it.polimi.spf.shared.model.ProfileField;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ProfileFieldCirclePickerItem extends FrameLayout {
	Context context;
	TextView mTextView;
	CirclePicker mCirclePicker;
	private ProfileField<?> f;
	
	public ProfileFieldCirclePickerItem(Context context) {
		super(context);
		init(context);
	}

	public ProfileFieldCirclePickerItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ProfileFieldCirclePickerItem(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	void init(Context context){
		inflate(context, R.layout.personas_circle_fragment_item, this);
		mTextView = (TextView) findViewById(R.id.profile_field_name_text);
		mCirclePicker = (CirclePicker) findViewById(R.id.circle_picker);
		this.context=context;
	}

	public void setProfileField(ProfileField<?> f) {
		Helper helper = new Helper(context);
		this.f = f;
		mTextView.setText(helper.getFriendlyNameOfField(f));
	}
	
	public void setCircles(List<String> selected, List<String> selectable){
		mCirclePicker.setCircles(selected, selectable);
	}
	
	public void setOnChangeListener( final OnChangeListener listener){
		mCirclePicker.setOnChangeListener(new CirclePicker.ChangeListener() {
			
			@Override
			public void onCircleRemoved(String tag) {
				listener.onRemove(f, tag);
				
			}
			
			@Override
			public void onCircleAdded(String tag) {
				listener.onAdd(f, tag);
			}
		});
	}
	
	interface OnChangeListener{
		void onAdd(ProfileField<?> f,String circle);
		void onRemove(ProfileField<?> f, String circle);
	}

}
