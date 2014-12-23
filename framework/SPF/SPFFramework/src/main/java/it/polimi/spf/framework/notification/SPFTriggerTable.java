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
package it.polimi.spf.framework.notification;

import it.polimi.spf.shared.model.SPFAction;
import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFTrigger;
import it.polimi.spf.shared.model.SPFTrigger.IllegalTriggerException;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * @author Jacopo Aliprandi
 * 
 */
/*package*/ class SPFTriggerTable extends SQLiteOpenHelper {

	private static class Contract implements BaseColumns {
		public static final String TABLE_NAME = "triggers";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_QUERY = "query";
		public static final String COLUMN_ACTION = "action";
		public static final String COLUMN_ONESHOT = "oneshot";
		public static final String COLUMN_EXPIRATION = "expiration";
		public static final String COLUMN_APP_IDENTIFIER = "app_identifier";
	}

	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Trigger.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
	// Table name
	Contract.TABLE_NAME + " (" +
	// Trigger ID
	Contract._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
	// Trigger name
	Contract.COLUMN_NAME + TEXT_TYPE + " UNIQUE ON CONFLICT REPLACE" + COMMA_SEP +
	// Trigger query
	Contract.COLUMN_QUERY + TEXT_TYPE + COMMA_SEP +
	// Trigger action
	Contract.COLUMN_ACTION + TEXT_TYPE + COMMA_SEP +
	// Trigger oneshot
	Contract.COLUMN_ONESHOT + INTEGER_TYPE + COMMA_SEP +
	// Trigger expiration
	Contract.COLUMN_EXPIRATION + INTEGER_TYPE + COMMA_SEP +
	// Trigger owner app identifier
	Contract.COLUMN_APP_IDENTIFIER + TEXT_TYPE + ")";

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Contract.TABLE_NAME;

	/**
	 * 
	 * @param context
	 */
	public SPFTriggerTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * Save the trigger in the database. If the operation succeeds, return a
	 * trigger with an updated id is returned, otherwise return null. The id is
	 * updated only when a new trigger is created. ( e.g. its id is set to -1)
	 * 
	 * @param trigger
	 * @param appIdentifier
	 * @return a trigger, otherwise null
	 */
	SPFTrigger saveTrigger(SPFTrigger trigger, String appIdentifier) {
		if (appIdentifier == null || trigger == null) {
			throw new NullPointerException();
		}

		ContentValues cv = new ContentValues();
		cv.put(Contract.COLUMN_NAME, trigger.getName());
		cv.put(Contract.COLUMN_QUERY, trigger.getQuery().toQueryString());
		cv.put(Contract.COLUMN_ACTION, trigger.getAction().toJSON());
		cv.put(Contract.COLUMN_ONESHOT, trigger.isOneShot() ? 1 : 0);
		cv.put(Contract.COLUMN_EXPIRATION, trigger.getSleepPeriod());
		cv.put(Contract.COLUMN_APP_IDENTIFIER, appIdentifier);

		SQLiteDatabase db = getWritableDatabase();

		if (trigger.getId() >= 0) {
			String where = Contract._ID + " = ?";
			String[] whereArgs = { String.valueOf(trigger.getId()) };
			if (db.update(Contract.TABLE_NAME, cv, where, whereArgs) > 0) {
				return trigger;
			}
			return null;
		}

		long id = db.insert(Contract.TABLE_NAME, null, cv);
		if (id >= 0) {
			trigger.setId(id);
			return trigger;
		}

		return null;
	}

	/**
	 * Return all the triggers of the specified application
	 * 
	 * @param appIdentifier
	 *            - the identifier of the application
	 * @return a list of {@link SPFTrigger}
	 */
	List<SPFTrigger> getAllTriggers(String appIdentifier) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String[] whereArgs = { appIdentifier };
		Cursor c = getReadableDatabase().query(Contract.TABLE_NAME, null, where, whereArgs, null, null, null);
		List<SPFTrigger> triggers = new ArrayList<SPFTrigger>();

		while (c.moveToNext()) {
			triggers.add(triggerFromCursor(c));
		}

		c.close();
		return triggers;
	}

	/**
	 * Return all the saved trigger.
	 * 
	 * @return a list of {@link SPFTrigger}
	 */
	List<SPFTrigger> getAllTriggers() {
		String where = null;
		String[] whereArgs = null;
		Cursor c = getReadableDatabase().query(Contract.TABLE_NAME, null, where, whereArgs, null, null, null);
		List<SPFTrigger> triggers = new ArrayList<SPFTrigger>();
		while (c.moveToNext()) {
			triggers.add(triggerFromCursor(c));
		}

		c.close();
		return triggers;
	}

	/**
	 * Delete all the triggers registered with the given application identifier.
	 * 
	 * @param appPackageName
	 * @return true if there is at least one row deleted
	 */
	boolean deleteAllTriggerOf(String appPackageName) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String[] whereArgs = { appPackageName };
		int c = getReadableDatabase().delete(Contract.TABLE_NAME, where, whereArgs);
		return c > 0;
	}

	/**
	 * Delete the trigger with the given id.
	 * 
	 * @param id
	 * @param appPackageName
	 * @return true if there is a deletion
	 */
	boolean deleteTrigger(long id, String appPackageName) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ? AND " + Contract._ID + " = ?";
		String[] whereArgs = { appPackageName, Long.toString(id) };
		int count = getReadableDatabase().delete(Contract.TABLE_NAME, where, whereArgs);
		return count > 0;

	}

	/**
	 * Return the trigger with the specified id.
	 * 
	 * @param triggerId
	 * @param appPackageName
	 * @return
	 */
	SPFTrigger getTrigger(long triggerId, String appPackageName) {
		String where = Contract._ID + " = ? AND " + Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String[] whereArgs = { Long.toString(triggerId), appPackageName };
		Cursor c = getReadableDatabase().query(Contract.TABLE_NAME, null, where, whereArgs, null, null, null);
		if (!c.moveToFirst()) {
			return null;
		}
		SPFTrigger t = triggerFromCursor(c);
		c.close();
		return t;
	}

	private SPFTrigger triggerFromCursor(Cursor c) {
		int idColumnId = c.getColumnIndexOrThrow(Contract._ID);
		int nameColumnId = c.getColumnIndexOrThrow(Contract.COLUMN_NAME);
		int queryColumnId = c.getColumnIndexOrThrow(Contract.COLUMN_QUERY);
		int actionColumnId = c.getColumnIndexOrThrow(Contract.COLUMN_ACTION);
		int oneShotColumnId = c.getColumnIndexOrThrow(Contract.COLUMN_ONESHOT);
		int expirationColumnId = c.getColumnIndexOrThrow(Contract.COLUMN_EXPIRATION);

		long id = c.getLong(idColumnId);
		String name = c.getString(nameColumnId);
		SPFQuery query = SPFQuery.fromQueryString(c.getString(queryColumnId));
		SPFAction action = SPFAction.fromJSON(c.getString(actionColumnId));
		boolean oneShot = c.getInt(oneShotColumnId) == 1;
		long expiration = c.getLong(expirationColumnId);

		try {
			return new SPFTrigger(id, name, query, action, oneShot, expiration);
		} catch (IllegalTriggerException e) {
			// This will never happen
			throw new IllegalStateException("Invalid trigger retrieved from db", e);
		}
	}

}
