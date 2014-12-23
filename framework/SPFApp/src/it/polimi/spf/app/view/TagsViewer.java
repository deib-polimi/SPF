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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Jacopo
 * 
 */
public class TagsViewer extends FlowLayout {
	boolean editable;
	private OnRemovedListener removedTaglistener;

	/**
	 * Used by xml inflater
	 * 
	 * @param context
	 * @param attrs
	 */
	public TagsViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * Used manually by code
	 * 
	 * @param context
	 * @param editable
	 */
	public TagsViewer(Context context, boolean editable) {
		super(context);
		init(context, editable);
	}

	private void init(Context context, boolean editable) {
		inflate(context, R.layout.view_tags_viewer, null);
		this.editable = editable;
	}

	private void init(Context context) {
		init(context, true);
	}

	private void notifyRemovedTagListener(final OnRemovedListener rl, final String tag) {
		post(new Runnable() {
			public void run() {
				rl.onRemovedTag(tag);
			}
		});

	}

	private OnClickListener bubbleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof TagBubble) {
				String tag = ((TagBubble) v).getText().toString();
				tags.remove(tag);
				removeView(v);
				final OnRemovedListener rl = removedTaglistener;
				if (rl != null) {
					notifyRemovedTagListener(rl, tag);
				}
			}
		}
	};

	/**
	 * Adds and shows a new tag.
	 * 
	 * @param tag
	 */
	public void addTag(String tag) {
		TagBubble tb = new TagBubble(getContext());
		tb.setText(tag);
		tb.setEditable(editable);
		tb.setOnRemoveTagListener(bubbleClickListener);
		tags.add(tag.toString());
		addView(tb);
	}

	public void setEditable(boolean editable) {
		if(this.editable == editable){
			return;
		}
		
		this.editable = editable;
		removeAllViews();
		for(String tag : tags){
			addTag(tag);
		}
	}
	
	private List<String> tags = new LinkedList<String>();

	/**
	 * returns the list of displayed tags.
	 * 
	 * @return
	 */
	public List<String> getTags() {
		return new ArrayList<String>(tags);
	}

	/**
	 * Set the list of tags to be displayed.
	 * 
	 * @param tags
	 */
	public void setTags(List<String> tags) {
		this.tags.clear();
		removeAllViews();
		for (String tag : tags) {
			addTag(tag);
		}
	}

	/**
	 * 
	 * {@link OnRemovedListener} contains the callback signature to be notified
	 * about tag removal events.
	 * 
	 */
	public interface OnRemovedListener {
		/**
		 * 
		 * @param tag
		 *            - the removed tag
		 */
		void onRemovedTag(String tag);
	}

	/**
	 * Set the {@link OnRemovedListener}.
	 * 
	 * @param listener
	 */
	public void setOnRemovedTagListener(OnRemovedListener listener) {
		this.removedTaglistener = listener;
	}

}
