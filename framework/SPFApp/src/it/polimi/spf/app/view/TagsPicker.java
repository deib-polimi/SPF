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

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * @author Jacopo
 * 
 */
public class TagsPicker extends RelativeLayout {

	private TagsViewer tv;
	private EditText et;
	private Button btn;
	private OnChangeListener changeListener;

	/**
	 * @param context
	 */
	public TagsPicker(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public TagsPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TagsPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_tags_picker, this);
		tv = ((TagsViewer) findViewById(R.id.tags_viewer));
		btn = (Button) findViewById(R.id.add_tag_button);
		et = (EditText) findViewById(R.id.edit_tag);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = et.getText().toString();
				if (text != null) {
					text = text.trim();
					if (!text.equals("")) {
						tv.addTag(text);
						et.setText("");
						notifyChangeListener();
					}
				}
			}
		});

	}

	public void setEditable(boolean editable){
		int visibility = editable ? View.VISIBLE : View.GONE;
		et.setVisibility(visibility);
		btn.setVisibility(visibility);
		tv.setEditable(editable);
	}
	
	private void notifyChangeListener() {
		if (changeListener != null) {
			final OnChangeListener listener = changeListener;
			post(new Runnable() {

				@Override
				public void run() {
					listener.onChange(getTags());
				}
			});
		}
	}
	
	/**
	 * Set the initial list of tags. Register a OnChangeListener to be notified about changes
	 * @param list
	 */
	public void setInitialTags(List<String> list) {
		tv.setTags(list);
	}

	/**
	 * Return the list of tags.
	 * 
	 * @return
	 */
	public List<String> getTags() {
		return tv.getTags();
	}

	/**
	 * OnChangeListenr is the container object of the callbacks that notifies
	 * about changes in the list of tags.
	 * 
	 * @author Jacopo
	 * 
	 */
	public interface OnChangeListener {
		/**
		 * 
		 * @param tags
		 *            - the updated list of tags
		 */
		void onChange(List<String> tags);
	}

	/* package */OnChangeListener getChangeListener() {
		return changeListener;
	}

	/**
	 * Set the change listener.
	 * 
	 * @param listener
	 */
	public void setChangeListener(OnChangeListener listener) {
		changeListener = listener;
		if (changeListener != null) {
			// add a listener to TagViewer for removed tags events
			tv.setOnRemovedTagListener(new TagsViewer.OnRemovedListener() {
				@Override
				public void onRemovedTag(String tag) {
					notifyChangeListener();
				}
			});
		}
	}

}
