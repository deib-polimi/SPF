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
package it.polimi.spf.app.navigation;

import it.polimi.spf.app.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NavigationEntryView extends LinearLayout {

	private TextView mNameView, mNotificationView;

	public NavigationEntryView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public NavigationEntryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NavigationEntryView(Context context) {
		super(context);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.navigation_fragment_entry, this);
		mNameView = (TextView) findViewById(R.id.navigation_entry_name);
		mNotificationView = (TextView) findViewById(R.id.navigation_entry_notification);
	}

	public void setName(String name) {
		mNameView.setText(name);
	}

	public void showNotification(String text) {
		mNotificationView.setVisibility(View.VISIBLE);
		mNotificationView.setText(text);
	}

	public void clearNotification() {
		mNotificationView.setText("");
		mNotificationView.setVisibility(View.GONE);
	}

}
