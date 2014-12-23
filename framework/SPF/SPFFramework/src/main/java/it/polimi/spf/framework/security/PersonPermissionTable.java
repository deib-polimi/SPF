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
package it.polimi.spf.framework.security;

import it.polimi.spf.framework.IdentifierGenerator;
import it.polimi.spf.framework.security.TokenCipher.WrongPassphraseException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * 
 * @author Jacopo Aliprandi
 * 
 * 
 */
public class PersonPermissionTable extends SQLiteOpenHelper {

	private static final String DB_NAME = "relationship_table.db";
	private static final int DB_VERSION = 1;
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	public PersonPermissionTable(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	/*
	 * TABLE_PERSON_AUTH contains the uuid and the access token
	 * 
	 * TABLE_PERMISSION contains the uuid and the associated 'circle'
	 * 
	 * TABLE_CIRCLES contains the available circles + the default one: private,
	 * public
	 */
	private static abstract class RelationshipEntry implements BaseColumns {
		public static final String TABLE_PERSON_AUTH = "person_auth";
		public static final String TABLE_PERMISSIONS = "permissions";
		public static final String TABLE_CIRCLES = "available_circles";
		public static final String COLUMN_USER_UUID = "unique_identifier";
		public static final String COLUMN_PASSWORD = "password";
		public static final String COLUMN_CIRCLE = "circle";
		public static final String COLUMN_TKN = "token";
		public static final String COLUMN_REQUEST_STATUS = "request_status";
	}

	public final static int REQUEST_NOT_EXIST = -1;
	public final static int REQUEST_ACCEPTED = 1;
	public final static int REQUEST_PENDING = 2;

	//@formatter:off
	private static final String SQL_CREATE_PERSON_AUTH = "CREATE TABLE " + RelationshipEntry.TABLE_PERSON_AUTH + " ("
			+ RelationshipEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP
			+ RelationshipEntry.COLUMN_USER_UUID + TEXT_TYPE + " UNIQUE ON CONFLICT REPLACE" + COMMA_SEP
			+ RelationshipEntry.COLUMN_REQUEST_STATUS + INTEGER_TYPE + COMMA_SEP
			+ RelationshipEntry.COLUMN_PASSWORD + TEXT_TYPE + COMMA_SEP
			+ RelationshipEntry.COLUMN_TKN + TEXT_TYPE + " )";

	private static final String SQL_CREATE_PERMISSIONS = "CREATE TABLE " + RelationshipEntry.TABLE_PERMISSIONS + " ("
			+ RelationshipEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP
			+ RelationshipEntry.COLUMN_USER_UUID + TEXT_TYPE + COMMA_SEP
			+ RelationshipEntry.COLUMN_CIRCLE + TEXT_TYPE + COMMA_SEP 
			+ " UNIQUE ( " + RelationshipEntry.COLUMN_USER_UUID + COMMA_SEP + RelationshipEntry.COLUMN_CIRCLE + " ) ON CONFLICT REPLACE )";

	private static final String SQL_CREATE_CIRCLES = "CREATE TABLE " + RelationshipEntry.TABLE_CIRCLES + " ("
			+ RelationshipEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP
			+ RelationshipEntry.COLUMN_CIRCLE + TEXT_TYPE + " UNIQUE ON CONFLICT REPLACE )";

	//@formatter:on

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_PERSON_AUTH);
		db.execSQL(SQL_CREATE_PERMISSIONS);
		db.execSQL(SQL_CREATE_CIRCLES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// no code here
	}

	// TODO add encryption, decryption and whatever else is needed... :P
	/**
	 * Call this method every time you receive a remote request.
	 * 
	 * @param receivedTkn
	 *            - may be null
	 * @return
	 */
	public PersonAuth getPersonAuthFrom(String receivedTkn) {
		if (receivedTkn.equals("")) {
			return PersonAuth.getPublicAuth();
		}

		String selection = RelationshipEntry.COLUMN_TKN + " = ? AND " + RelationshipEntry.COLUMN_REQUEST_STATUS + " = ?";
		String[] selectionArgs = { receivedTkn, Integer.toString(REQUEST_ACCEPTED) };
		String[] columns = { RelationshipEntry.COLUMN_USER_UUID };
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(RelationshipEntry.TABLE_PERSON_AUTH, columns, selection, selectionArgs, /* groupBy */null, /* having */null, /* orderBy */
				null);

		PersonAuth auth;
		if (cursor.moveToNext()) {
			String uniqueIdentifier = cursor.getString(cursor.getColumnIndex(RelationshipEntry.COLUMN_USER_UUID));
			auth = generatePermissionFor(uniqueIdentifier, db);
		} else {
			auth = PersonAuth.getPublicAuth();
		}

		cursor.close();
		return auth;
	}

	private PersonAuth generatePermissionFor(String uuid, SQLiteDatabase db) {
		String selection = RelationshipEntry.COLUMN_USER_UUID + " = ?";
		String[] selectionArgs = { uuid };
		String[] columns = { RelationshipEntry.COLUMN_CIRCLE };
		Cursor cursor = db.query(RelationshipEntry.TABLE_PERMISSIONS, columns, selection, selectionArgs, /* groupBy */null, /* having */null, /* orderBy */
				null);
		List<String> circles = new ArrayList<String>();
		while (cursor.moveToNext()) {
			String c = cursor.getString(cursor.getColumnIndex(RelationshipEntry.COLUMN_CIRCLE));
			circles.add(c);
		}

		if (!circles.contains(DefaultCircles.PUBLIC)) {
			circles.add(DefaultCircles.PUBLIC);
		}

		cursor.close();
		return new PersonAuth(uuid, circles);
	}

	private static final String NULLABLE_HACK = "null";

	public String getTokenFor(String targetUID) {
		String selection = RelationshipEntry.COLUMN_USER_UUID + " = ? AND " + RelationshipEntry.COLUMN_REQUEST_STATUS + " = ?";
		String[] selectionArgs = { targetUID, Integer.toString(REQUEST_ACCEPTED) };
		String[] columns = { RelationshipEntry.COLUMN_TKN };
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(RelationshipEntry.TABLE_PERSON_AUTH, columns, selection, selectionArgs, /* groupBy */null, /* having */null, /* orderBy */
				null);
		
		if (cursor.moveToNext()) {
			String tknToSend = cursor.getString(cursor.getColumnIndex(RelationshipEntry.COLUMN_TKN));
			if (!tknToSend.equals(NULLABLE_HACK)) {
				cursor.close();
				return tknToSend;
			}
		}
		cursor.close();
		return "";
	}

	/**
	 * Creates a pending request associated to the target id.
	 * 
	 * @param targetUid
	 * @return
	 */
	public String createEntryForSentRequest(String targetUid, String password) throws GeneralSecurityException {
		// TODO Add it back later
		// if (entryExistsFor(targetUid) != REQUEST_NOT_EXIST) {
		// return ;
		// }

		String user_uuid = targetUid;
		String token = new IdentifierGenerator().generateAccessToken();

		int request_status = REQUEST_ACCEPTED;
		insertNewEntry(user_uuid, token, request_status);
		return TokenCipher.encryptToken(token, password);
	}

	/**
	 * Creates a pending request with the information contained in the message.
	 * 
	 * @param fr
	 * @return
	 */
	public boolean createEntryForReceivedRequest(ContactRequest fr) {
		String user_uuid = fr.getUserIdentifier();
		String receive_token = fr.getAccessToken();
		int request_status = REQUEST_PENDING;
		if (insertNewEntry(user_uuid, receive_token, request_status)) {
			return true;
		}
		return false;
	}

	public void deleteEntryOf(String userUID) {
		SQLiteDatabase db = getWritableDatabase();
		String table = RelationshipEntry.TABLE_PERSON_AUTH;
		String whereClause = RelationshipEntry.COLUMN_USER_UUID + " = ?";
		String[] whereArgs = { userUID };
		if (db.delete(table, whereClause, whereArgs) > 0) {
			table = RelationshipEntry.TABLE_PERMISSIONS;
			db.delete(table, whereClause, whereArgs);
		}
	}

	private boolean insertNewEntry(String user_uuid, String token, int status) {
		ContentValues cv = new ContentValues();
		cv.put(RelationshipEntry.COLUMN_TKN, token);
		cv.put(RelationshipEntry.COLUMN_USER_UUID, user_uuid);
		cv.put(RelationshipEntry.COLUMN_REQUEST_STATUS, status);
		SQLiteDatabase db = getWritableDatabase();
		return db.insert(RelationshipEntry.TABLE_PERSON_AUTH, NULLABLE_HACK, cv) > -1;
	}

	/**
	 * check if an entry for the specified user id exists. Returns the entry
	 * state.
	 * 
	 * @param userUID
	 * @return
	 */
	public int entryExistsFor(String userUID) {
		String selection = RelationshipEntry.COLUMN_USER_UUID + " = ?";
		String[] selectionArgs = { userUID };
		String[] columns = { RelationshipEntry.COLUMN_REQUEST_STATUS };
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(RelationshipEntry.TABLE_PERSON_AUTH, columns, selection, selectionArgs, /* groupBy */null, /* having */null, /* orderBy */
				null);
		if (cursor.moveToNext()) {
			return cursor.getInt(cursor.getColumnIndex(RelationshipEntry.COLUMN_REQUEST_STATUS));
		}
		cursor.close();
		return REQUEST_NOT_EXIST;
	}

	/**
	 * 
	 * @return
	 */
	public Collection<String> getCircles() {
		String table = RelationshipEntry.TABLE_CIRCLES;
		String selection = null;
		String[] selectionArgs = null;
		String[] columns = { RelationshipEntry.COLUMN_CIRCLE };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Set<String> circles = new TreeSet<String>(DefaultCircles.COMPARATOR);

		int circleIndex = c.getColumnIndex(RelationshipEntry.COLUMN_CIRCLE);
		while (c.moveToNext()) {
			String circle = c.getString(circleIndex);
			circles.add(circle);
		}

		// Add default circle, they are NOT stored in the database.
		for (String def_circle : DefaultCircles.getAll()) {
			circles.add(def_circle);
		}

		c.close();
		return circles;
	}

	/**
	 * 
	 * @param circle
	 * @return
	 */
	public boolean addCircle(String circle) {
		if (DefaultCircles.isDefault(circle)) {
			return false; // new circle has the same name of a new one.
		}

		ContentValues cv = new ContentValues();
		cv.put(RelationshipEntry.COLUMN_CIRCLE, circle);
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert(RelationshipEntry.TABLE_CIRCLES, NULLABLE_HACK, cv);
		return id != -1;
	}

	/**
	 * 
	 * @param circle
	 * @return
	 */
	public boolean removeCircle(String circle) {
		if(DefaultCircles.isDefault(circle)){
			return false;
		}
		
		SQLiteDatabase db = getWritableDatabase();
		String table = RelationshipEntry.TABLE_CIRCLES;
		String whereClause = RelationshipEntry.COLUMN_CIRCLE + " = ?";
		String[] whereArgs = { circle };
		int count = db.delete(table, whereClause, whereArgs);
		return count > 0;
	}

	/**
	 * 
	 * @return
	 */
	public Collection<PersonAuth> getPersonAuthList(int status) {
		SQLiteDatabase db = getReadableDatabase();
		String table = RelationshipEntry.TABLE_PERSON_AUTH;
		String[] columns = { RelationshipEntry.COLUMN_USER_UUID };
		String selection = RelationshipEntry.COLUMN_REQUEST_STATUS + " = ?";
		String[] selectionArgs = { Integer.toString(status) };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		List<PersonAuth> pAuths = new ArrayList<PersonAuth>();
		while (c.moveToNext()) {
			String uuid = c.getString(c.getColumnIndex(RelationshipEntry.COLUMN_USER_UUID));
			PersonAuth pAuth = generatePermissionFor(uuid, db);
			pAuths.add(pAuth);
		}
		return pAuths;
	}

	/*
	 * given the schema, it is a too heavy operation:
	 * 
	 * public boolean savePersonAuth(PersonAuth pAuth){ / update the circles for
	 * a given person return false; }
	 */

	/**
	 * 
	 * @param uuid
	 * @param circle
	 * @return
	 */
	public boolean addPersonToCircle(String uuid, String circle) {
		SQLiteDatabase db = getWritableDatabase();
		String table = RelationshipEntry.TABLE_PERMISSIONS;
		String nullColumnHack = NULLABLE_HACK;
		ContentValues values = new ContentValues();
		values.put(RelationshipEntry.COLUMN_USER_UUID, uuid);
		values.put(RelationshipEntry.COLUMN_CIRCLE, circle);
		long id = db.insert(table, nullColumnHack, values);
		return id != -1;
	}

	/**
	 * 
	 * @param uuid
	 * @param circle
	 * @return
	 */
	public boolean removePersonFromCircle(String uuid, String circle) {
		SQLiteDatabase db = getWritableDatabase();
		String table = RelationshipEntry.TABLE_PERMISSIONS;
		String whereClause = RelationshipEntry.COLUMN_USER_UUID + " = ? AND " + RelationshipEntry.COLUMN_CIRCLE + " = ?";
		String[] whereArgs = { uuid, circle };
		int count = db.delete(table, whereClause, whereArgs);
		return count > 0;
	}

	/**
	 * Confirm a friendship request. the status of the entry associated with the
	 * specified person, will be REQUEST_ACCEPTED
	 * 
	 * @param targetUID
	 *            - the unique identifier of the person
	 * @throws WrongPassphraseException
	 * @throws GeneralSecurityException
	 */
	public boolean confirmRequest(String targetUID, String password) throws GeneralSecurityException, WrongPassphraseException {
		SQLiteDatabase db = getWritableDatabase();
		String table = RelationshipEntry.TABLE_PERSON_AUTH;
		String[] columns = { RelationshipEntry.COLUMN_TKN, RelationshipEntry.COLUMN_REQUEST_STATUS, RelationshipEntry.COLUMN_PASSWORD };
		String selection = RelationshipEntry.COLUMN_USER_UUID + " = ? ";
		String[] selectionArgs = { targetUID };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String limit = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		if (c.moveToNext()) {
			String token = c.getString(c.getColumnIndex(RelationshipEntry.COLUMN_TKN));
			String decryptedTkn = TokenCipher.decryptToken(token, password);
			if (decryptedTkn != null) {
				return commitConfirmation(targetUID, password, decryptedTkn);
			} else {
				return false;
			}
		}
		return false;
	}

	private boolean commitConfirmation(String targetUID, String password, String token) {

		String selection = RelationshipEntry.COLUMN_USER_UUID + " = ?";
		String[] selectionArgs = { targetUID };
		ContentValues cv = new ContentValues();
		cv.put(RelationshipEntry.COLUMN_REQUEST_STATUS, REQUEST_ACCEPTED);
		cv.put(RelationshipEntry.COLUMN_PASSWORD, password);
		cv.put(RelationshipEntry.COLUMN_TKN, token);
		SQLiteDatabase db = getWritableDatabase();
		return db.update(RelationshipEntry.TABLE_PERSON_AUTH, cv, selection, selectionArgs) > 0;
	}

	public int getPendingRequestCount() {
		String table = RelationshipEntry.TABLE_PERSON_AUTH;
		String[] columns = {};
		String selection = RelationshipEntry.COLUMN_REQUEST_STATUS + " = ?";
		String[] args = { String.valueOf(REQUEST_PENDING) };

		return getReadableDatabase().query(table, columns, selection, args, null, null, null).getCount();
	}

}
