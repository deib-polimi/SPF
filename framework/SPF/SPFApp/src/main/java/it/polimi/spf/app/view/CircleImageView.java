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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * View to display a profile picture in a circle shape
 * 
 * @author darioarchetti
 * 
 */
public class CircleImageView extends View {

	private final static int PICTURE_RADIUS_DP = 130;

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleImageView(Context context) {
		super(context);
	}

	private boolean hasBitmap;
	private int mRadius;
	private Paint mPaint, mBackgroundPaint;

	@Override
	protected void onDraw(Canvas canvas) {
		if (!hasBitmap) {
			return;
		}

		int h = getHeight(), w = getWidth();
		canvas.drawCircle(w / 2, h / 2, h / 2, mBackgroundPaint);
		canvas.drawCircle(w / 2, h / 2, h / 2 * 0.95f, mPaint);
	}
	
	public void setBackground(Bitmap image) {
		Resources r = getContext().getResources();
		
		if(image == null){
			image = BitmapFactory.decodeResource(r, R.drawable.empty_profile_picture);
		}
		
		mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PICTURE_RADIUS_DP, r.getDisplayMetrics());

		Bitmap b = Bitmap.createScaledBitmap(image, mRadius, mRadius, false);
		BitmapShader bs = new BitmapShader(b, TileMode.CLAMP, TileMode.CLAMP);
		
		
		mPaint = new Paint();
		mPaint.setShader(bs);

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setColor(0x8500000);

		hasBitmap = true;
	}

}
