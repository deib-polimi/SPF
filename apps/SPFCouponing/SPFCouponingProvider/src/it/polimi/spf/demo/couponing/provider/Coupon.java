package it.polimi.spf.demo.couponing.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public class Coupon {

	public interface Contract extends BaseColumns {
		public static final String TABLE_NAME = "coupons";
		public static final String COLUMN_TRIGGER_ID = "triggerId";
		public static final String COLUMN_TITLE = "coupon_title";
		public static final String COLUMN_TEXT = "coupon_text";
		public static final String COLUMN_PHOTO = "coupon_photo";
		public static final String COLUMN_CATEGORY = "coupon_category";
	}
	
	public static Coupon fromCursor(Cursor cursor){
		Coupon coupon = new Coupon();
		coupon.mId = cursor.getLong(cursor.getColumnIndex(Contract._ID));
		coupon.mTriggerId = cursor.getLong(cursor.getColumnIndex(Contract.COLUMN_TRIGGER_ID));
		coupon.mTitle = cursor.getString(cursor.getColumnIndex(Contract.COLUMN_TITLE));
		coupon.mText = cursor.getString(cursor.getColumnIndex(Contract.COLUMN_TEXT));
		coupon.mCategory = cursor.getString(cursor.getColumnIndex(Contract.COLUMN_CATEGORY));
		coupon.mPhoto = cursor.getBlob(cursor.getColumnIndex(Contract.COLUMN_PHOTO));
		return coupon;
	}
	
	private long mId, mTriggerId;
	private String mTitle, mText, mCategory;
	private byte[] mPhoto;
	
	public Coupon(){
		mId = -1;
		mTriggerId = -1;
	}
	
	public long getId() {
		return mId;
	}
	
	public long getTriggerId() {
		return mTriggerId;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getText() {
		return mText;
	}
	
	public String getCategory() {
		return mCategory;
	}
	
	public byte[] getPhoto() {
		return mPhoto;
	}
	
	void setId(long id) {
		this.mId = id;
	}
	
	public void setTriggerId(long triggerId) {
		this.mTriggerId = triggerId;
	}
	
	public void setTitle(String title) {
		this.mTitle = title;
	}
	
	public void setText(String text) {
		this.mText = text;
	}
	
	public void setCategory(String category) {
		this.mCategory = category;
	}
	
	public void setPhoto(byte[] photo) {
		this.mPhoto = photo;
	}
	
	public ContentValues toContentValues(){
		ContentValues cv =new ContentValues();
		cv.put(Contract.COLUMN_TRIGGER_ID, mTriggerId);
		cv.put(Contract.COLUMN_TITLE, mTitle);
		cv.put(Contract.COLUMN_TEXT, mText);
		cv.put(Contract.COLUMN_CATEGORY, mCategory);
		cv.put(Contract.COLUMN_PHOTO, mPhoto);
		return cv;
	}
	
	@Override
	public String toString() {
		return String.format(
				"Coupon {id: %s, triggerId: %s, title: %s, text: %s, category: %s, photo: %s", 
				mId, mTriggerId, mTitle, mText, mCategory, mPhoto == null ? "no" : "yes");
	}
}
