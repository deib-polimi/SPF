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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Jacopo Aliprandi
 * 
 */
public class FlowLayout extends ViewGroup {

	public FlowLayout(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public FlowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    //from http://adilatwork.blogspot.it/2012/12/android-horizontal-flow-layout.html
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		// increment the x position as we progress through a line
		int xpos = getPaddingLeft();
		// increment the y position as we progress through the lines
		int ypos = getPaddingTop();
		// the height of the current line
		int line_height = 0;

		// go through children
		// to work out the height required for this view
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		View child;
		MarginLayoutParams childMarginLayoutParams;
		int childWidth, childHeight, childMarginLeft, childMarginRight, childMarginTop, childMarginBottom;

		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);

			if (child.getVisibility() != GONE) {
				childWidth = child.getMeasuredWidth();
				childHeight = child.getMeasuredHeight();

				if (child.getLayoutParams() != null
						&& child.getLayoutParams() instanceof MarginLayoutParams) {
					childMarginLayoutParams = (MarginLayoutParams) child
							.getLayoutParams();

					childMarginLeft = childMarginLayoutParams.leftMargin;
					childMarginRight = childMarginLayoutParams.rightMargin;
					childMarginTop = childMarginLayoutParams.topMargin;
					childMarginBottom = childMarginLayoutParams.bottomMargin;
				} else {
					childMarginLeft = 0;
					childMarginRight = 0;
					childMarginTop = 0;
					childMarginBottom = 0;
				}

				if (xpos + childMarginLeft + childWidth + childMarginRight
						+ getPaddingRight() > width) {
					// this child will need to go on a new line

					xpos = getPaddingLeft();
					ypos += line_height;

					line_height = childMarginTop + childHeight
							+ childMarginBottom;
				} else
					// enough space for this child on the current line
					line_height = Math.max(line_height, childMarginTop
							+ childHeight + childMarginBottom);

				xpos += childMarginLeft + childWidth + childMarginRight;
			}
		}

		ypos += line_height + getPaddingBottom();

		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED)
			// set height as measured since there's no height restrictions
			height = ypos;
		else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
				&& ypos < height)
			// set height as measured since it's less than the maximum allowed
			height = ypos;

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// increment the x position as we progress through a line
		int xpos = getPaddingLeft();
		// increment the y position as we progress through the lines
		int ypos = getPaddingTop();
		// the height of the current line
		int line_height = 0;

		View child;
		MarginLayoutParams childMarginLayoutParams;
		int childWidth, childHeight, childMarginLeft, childMarginRight, childMarginTop, childMarginBottom;

		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);

			if (child.getVisibility() != GONE) {
				childWidth = child.getMeasuredWidth();
				childHeight = child.getMeasuredHeight();

				if (child.getLayoutParams() != null
						&& child.getLayoutParams() instanceof MarginLayoutParams) {
					childMarginLayoutParams = (MarginLayoutParams) child
							.getLayoutParams();

					childMarginLeft = childMarginLayoutParams.leftMargin;
					childMarginRight = childMarginLayoutParams.rightMargin;
					childMarginTop = childMarginLayoutParams.topMargin;
					childMarginBottom = childMarginLayoutParams.bottomMargin;
				} else {
					childMarginLeft = 0;
					childMarginRight = 0;
					childMarginTop = 0;
					childMarginBottom = 0;
				}

				if (xpos + childMarginLeft + childWidth + childMarginRight
						+ getPaddingRight() > r - l) {
					// this child will need to go on a new line

					xpos = getPaddingLeft();
					ypos += line_height;

					line_height = childHeight + childMarginTop
							+ childMarginBottom;
				} else
					// enough space for this child on the current line
					line_height = Math.max(line_height, childMarginTop
							+ childHeight + childMarginBottom);

				child.layout(xpos + childMarginLeft, ypos + childMarginTop,
						xpos + childMarginLeft + childWidth, ypos
								+ childMarginTop + childHeight);

				xpos += childMarginLeft + childWidth + childMarginRight;
			}
		}
	}

}
