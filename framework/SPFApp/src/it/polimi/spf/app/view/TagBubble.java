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
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Jacopo
 *
 */
public class TagBubble extends LinearLayout {

	/**
	 * @param context
	 */
	public TagBubble(Context context) {
		super(context);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public TagBubble(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TagBubble(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		inflate(context, R.layout.view_tag_bubble, this);	
	}
	
	public void setText(CharSequence text){
		TextView txtView = (TextView)findViewById(R.id.textView1);
		txtView.setText(text);
	}
	
	public CharSequence getText(){
		TextView txtView = (TextView)findViewById(R.id.textView1);
		return txtView.getText();
	}
	
	public void setOnRemoveTagListener(final OnClickListener listener){
		Button btn=(Button)findViewById(R.id.remove_tag_button);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onClick(TagBubble.this);
				
			}
		});
	}
	
	public void setEditable(boolean editable){
		Button btn=(Button)findViewById(R.id.remove_tag_button);
		if (!editable){
			btn.setVisibility(GONE);
		}else{
			btn.setVisibility(VISIBLE);
		}
	}
	
	

}
