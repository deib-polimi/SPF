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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/*package*/ class NotificationMessageTable {

	private final Table mTable;

	public NotificationMessageTable(Context context) {
		mTable = new Table(context);
	}

	public List<NotificationMessage> getAvailableNotifications() {
		Cursor c = mTable.getReadableDatabase().query(Contract.TABLE_NAME, null, null, null, null, null, null);
		List<NotificationMessage> result = new ArrayList<NotificationMessage>();

		int idIndex = c.getColumnIndex(Contract._ID);
		int titleIndex = c.getColumnIndexOrThrow(Contract.COLUMN_TITLE);
		int messageIndex = c.getColumnIndexOrThrow(Contract.COLUMN_MESSAGE);
		int senderIndex = c.getColumnIndexOrThrow(Contract.COLUMN_SENDER_ID);

		while (c.moveToNext()) {
			result.add(new NotificationMessage(c.getLong(idIndex), c.getString(senderIndex), c.getString(titleIndex), c.getString(messageIndex)));
		}

		c.close();
		return result;
	}

	public boolean saveNotification(NotificationMessage message) {
		ContentValues cv = new ContentValues();
		cv.put(Contract.COLUMN_SENDER_ID, message.getSenderId());
		cv.put(Contract.COLUMN_TITLE, message.getTitle());
		cv.put(Contract.COLUMN_MESSAGE, message.getMessage());
		return mTable.getWritableDatabase().insert(Contract.TABLE_NAME, null, cv) > 0;
	}

	public boolean deleteNotification(long id) {
		String where = Contract._ID + " = ?";
		String[] args = { String.valueOf(id) };
		return mTable.getWritableDatabase().delete(Contract.TABLE_NAME, where, args) > 0;
	}

	public boolean deleteAllNotifications() {
		return mTable.getWritableDatabase().delete(Contract.TABLE_NAME, "1", null) > 0;
	}

	public int getAvailableNotificationCount() {
		Cursor c = mTable.getReadableDatabase().query(Contract.TABLE_NAME, new String[] {}, null, null, null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}

	private final static class Contract implements BaseColumns {
		public final static String TABLE_NAME = "notificationMessages";
		public final static String COLUMN_TITLE = "title";
		public final static String COLUMN_MESSAGE = "message";
		public final static String COLUMN_SENDER_ID = "senderId";
	}

	private static class Table extends SQLiteOpenHelper {

		public static final int DATABASE_VERSION = 1;
		public static final String DATABASE_NAME = "NotificationMessages.db";
		private static final String COMMA_SEP = ",";

		//@formatter:off
		private final static String CREATE_SQL = "CREATE TABLE " + Contract.TABLE_NAME + " ("
				+ Contract._ID + " INTEGER PRIMARY KEY " + COMMA_SEP 
				+ Contract.COLUMN_TITLE + " TEXT " + COMMA_SEP
				+ Contract.COLUMN_MESSAGE + " TEXT " + COMMA_SEP
				+ Contract.COLUMN_SENDER_ID + " TEXT)";
		
		private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
				+ Contract.TABLE_NAME;
		//@formatter:on

		public Table(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(SQL_DELETE_ENTRIES);
			db.execSQL(CREATE_SQL);
		}

	}
}
