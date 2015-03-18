package it.polimi.spf.demo.couponing.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CouponDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "CouponDatabase.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String BLOB_TYPE = " BLOB";

	private static final String[] CATEGORIES = {
		"Smartphones",
		"Computer",
		"DSLR"
	};
	
	private static final String COMMA_SEP = ",";

	//@formatter:off
	private static final String CREATE_SQL = "CREATE TABLE " + Coupon.Contract.TABLE_NAME + " ("
			+ Coupon.Contract._ID               + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP
			+ Coupon.Contract.COLUMN_TRIGGER_ID + INTEGER_TYPE + COMMA_SEP
			+ Coupon.Contract.COLUMN_TITLE      + TEXT_TYPE    + COMMA_SEP
			+ Coupon.Contract.COLUMN_TEXT       + TEXT_TYPE    + COMMA_SEP
			+ Coupon.Contract.COLUMN_CATEGORY   + TEXT_TYPE    + COMMA_SEP
			+ Coupon.Contract.COLUMN_PHOTO      + BLOB_TYPE + ")";
	
	//@formatter:on

	/**
	 * Constructor for {@link ChatStorage}
	 * 
	 * @param ctx
	 *            - the Context in which the database should be created
	 */
	public CouponDatabase(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// No code here for the first version
	}

	public List<Coupon> getAllCoupons() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(Coupon.Contract.TABLE_NAME, null,// columns
				null,// selection
				null,// selection args
				null,// groupby
				null,// having
				Coupon.Contract.COLUMN_TITLE + " DESC"// orderby
		);

		List<Coupon> result = new ArrayList<Coupon>();
		while (c.moveToNext()) {
			result.add(Coupon.fromCursor(c));
		}

		c.close();
		return result;
	}

	public boolean saveCoupon(Coupon coupon) {
		if (coupon == null) {
			throw new NullPointerException("Coupon to save is null");
		}

		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = coupon.toContentValues();

		if (coupon.getId() == -1) {
			long id = db.insert(Coupon.Contract.TABLE_NAME, "NULL", values);
			if (id == -1) {
				return false;
			}

			coupon.setId(id);
			return true;
		} else {
			String where = Coupon.Contract._ID + " = ?";
			String[] args = { Long.toString(coupon.getId()) };
			int updated = db.update(Coupon.Contract.TABLE_NAME, values, where, args);
			return updated > 0;
		}
	}

	public Coupon getCouponById(long couponId) {
		String where = Coupon.Contract._ID + " = ?";
		String args[] = { Long.toString(couponId) };
		Cursor c = getReadableDatabase().query(Coupon.Contract.TABLE_NAME, null, where, args, null, null, null);
		Coupon coupon = c.moveToFirst() ? Coupon.fromCursor(c) : null;
		c.close();
		return coupon;
	}

	public boolean deleteCoupon(Coupon coupon) {
		SQLiteDatabase db = getWritableDatabase();
		final String where = Coupon.Contract._ID + " = ?";
		final String[] args = { Long.toString(coupon.getId()) };
		return db.delete(Coupon.Contract.TABLE_NAME, where, args) > 0;
	}

	public String[] getCategories(){
		return CATEGORIES;
	}

	public Coupon getCouponByTriggerId(long triggerId) {
		String where = Coupon.Contract.COLUMN_TRIGGER_ID + " = ?";
		String[] args = { Long.toString(triggerId) };
		Cursor c = getReadableDatabase().query(Coupon.Contract.TABLE_NAME, null, where, args, null, null, null);
		
		if(!c.moveToFirst()){
			return null;
		}
		
		Coupon coupon = Coupon.fromCursor(c);
		c.close();
		return coupon;
	}
}
