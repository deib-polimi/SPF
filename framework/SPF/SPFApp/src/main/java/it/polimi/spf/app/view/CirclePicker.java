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

import java.util.ArrayList;
import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.app.view.TagsViewer.OnRemovedListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class CirclePicker extends LinearLayout {

	public static interface ChangeListener {
		void onCircleAdded(String tag);

		void onCircleRemoved(String tag);
	}

	private TagsViewer mTagsViewer;
	private Spinner mSpinner;
	private Button mAddButton;

	private List<String> mSelectedCircles, mSelectableCircles;

	private Listener mListener;

	public CirclePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CirclePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CirclePicker(Context context) {
		super(context);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_circle_picker_spinner, this);
		mListener = new Listener();
		
		mTagsViewer = (TagsViewer) findViewById(R.id.tags_viewer);
		mTagsViewer.setOnRemovedTagListener(mListener);
		
		mSpinner = (Spinner) findViewById(R.id.tag_select);
		mSpinner.setOnItemSelectedListener(mListener);
		
		mAddButton = (Button) findViewById(R.id.circle_add_button);
		mAddButton.setOnClickListener(mListener);
	}

	public void setCircles(List<String> selectedCircles, List<String> selectableCircles) {
		this.mSelectedCircles = selectedCircles == null ? new ArrayList<String>() : selectedCircles;
		this.mSelectableCircles = selectableCircles == null ? new ArrayList<String>() : selectableCircles;

		mTagsViewer.setTags(mSelectedCircles);
		refreshSpinner();
	}

	public List<String> getSelectedCircles() {
		return new ArrayList<String>(mSelectedCircles);
	}
	
	private void refreshSpinner() {
		mSpinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mSelectableCircles));
	}

	public void setOnChangeListener(ChangeListener listener) {
		mListener.setChangeListener(listener);
	}

	private class Listener implements OnClickListener, OnRemovedListener, OnItemSelectedListener {

		private ChangeListener mListener;
		
		public void setChangeListener(ChangeListener listener){
			this.mListener = listener;
		}

		@Override
		public void onClick(View v) {
			String circle = (String) mSpinner.getSelectedItem();
			mSelectedCircles.add(circle);
			mSelectableCircles.remove(circle);

			mTagsViewer.addTag(circle);
			refreshSpinner();

			if (mListener != null) {
				mListener.onCircleAdded(circle);
			}
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mAddButton.setEnabled(true);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			mAddButton.setEnabled(false);
		}

		@Override
		public void onRemovedTag(String tag) {
			mSelectedCircles.remove(tag);
			mSelectableCircles.add(tag);

			refreshSpinner();

			if (mListener != null) {
				mListener.onCircleRemoved(tag);
			}
		}

	}
}
