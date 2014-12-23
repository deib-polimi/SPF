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
package it.polimi.spf.framework.profile;

import it.polimi.spf.framework.security.DefaultCircles;
import it.polimi.spf.framework.security.PersonAuth;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.ProfileFieldContainer.FieldStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * SQLite helper class. Provides the implementation of the store procedures
 * needed by {@link SPFProfileManager}.
 * 
 * @author Jacopo Aliprandi
 * 
 */
/*package*/ class ProfileTable extends SQLiteOpenHelper {

	public static final String TAG = "ProfileDB";

	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Profile.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";

	public ProfileTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Contract for the database schema of {@link ProfileTable}.
	 * 
	 * @author Jacopo Aliprandi
	 * 
	 */
	private static abstract class Contract implements BaseColumns {

		/**
		 * Table that contains {@link SPFPersona}s. Schema : < PERSONA >
		 */
		public static final String TABLE_PERSONAS = "personas_t";

		/**
		 * Table that contains the profile fields. Schema : < KEY , VALUE ,
		 * PERSONA>
		 */
		public static final String TABLE_PROFILE = "profile_t";

		/**
		 * Table that contains the circle specified for each profile field and
		 * {@link SPFPersona}. Schema : <KEY , CIRCLE , PERSONA >
		 */
		public static final String TABLE_VISIBILITY = "visibility_t";

		/**
		 * The string identifier of a profile field.
		 */
		public static final String COLUMN_KEY = "key";

		/**
		 * The string value (content) of a profile field.
		 */
		public static final String COLUMN_VALUE = "value";

		/**
		 * The string identifier of the {@link SPFPersona}.
		 */
		public static final String COLUMN_PERSONA = "persona";

		/**
		 * The string identifier for a circle.
		 */
		public static final String COLUMN_CIRCLE = "circle";
	}

	//@formatter:off
	private static final String SQL_CREATE_PROFILE = "CREATE TABLE "
			+ Contract.TABLE_PROFILE + " (" 
			+ Contract._ID	+ " INTEGER PRIMARY KEY" + COMMA_SEP 
			+ Contract.COLUMN_KEY + TEXT_TYPE + COMMA_SEP 
			+ Contract.COLUMN_VALUE + TEXT_TYPE	+ COMMA_SEP 
			+ Contract.COLUMN_PERSONA + TEXT_TYPE + COMMA_SEP
			+ " UNIQUE ( " + Contract.COLUMN_KEY + COMMA_SEP
			+ Contract.COLUMN_PERSONA + " ) ON CONFLICT REPLACE)";

	private static final String SQL_CREATE_PERSONAS = "CREATE TABLE "
			+ Contract.TABLE_PERSONAS + " (" 
			+ Contract._ID	+ " INTEGER PRIMARY KEY" + COMMA_SEP 
			+ Contract.COLUMN_PERSONA + TEXT_TYPE + " UNIQUE ON CONFLICT REPLACE" + ")";

	private static final String SQL_CREATE_VISIBILITY = "CREATE TABLE "
			+ Contract.TABLE_VISIBILITY + " (" 
			+ Contract._ID + " INTEGER PRIMARY KEY" + COMMA_SEP 
			+ Contract.COLUMN_PERSONA + TEXT_TYPE + COMMA_SEP
			+ Contract.COLUMN_CIRCLE + TEXT_TYPE + COMMA_SEP
			+ Contract.COLUMN_KEY + TEXT_TYPE + COMMA_SEP
			+" UNIQUE ( " + Contract.COLUMN_KEY + COMMA_SEP
			+ Contract.COLUMN_PERSONA +COMMA_SEP + Contract.COLUMN_CIRCLE+" ) ON CONFLICT REPLACE)";
	//@formatter:on

	private static final boolean LOG = false;

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_PROFILE);
		db.execSQL(SQL_CREATE_PERSONAS);
		db.execSQL(SQL_CREATE_VISIBILITY);
		String table = Contract.TABLE_PERSONAS;
		String nullColumnHack = null;
		ContentValues values = new ContentValues();
		values.put(Contract.COLUMN_PERSONA, SPFPersona.getDefault().getIdentifier());
		db.insert(table, nullColumnHack, values);
		addCircleToFieldsInternal(DefaultCircles.PUBLIC, ProfileField.DISPLAY_NAME, SPFPersona.getDefault(), db);
		addCircleToFieldsInternal(DefaultCircles.PUBLIC, ProfileField.IDENTIFIER, SPFPersona.getDefault(), db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new IllegalStateException("ProfileTable on upgrade/downgrade called");
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * Insert a single profile field in the {@link Contract#TABLE_PROFILE}
	 * 
	 * @param key
	 * @param value
	 * @param personaIdentifier
	 * @return true if the operation was successful
	 */
	private boolean setValue(String key, String value, String personaIdentifier) {
		SQLiteDatabase db = getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Contract.COLUMN_KEY, key);
		values.put(Contract.COLUMN_VALUE, value);
		values.put(Contract.COLUMN_PERSONA, personaIdentifier);

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		String nullHack = null;
		newRowId = db.insert(Contract.TABLE_PROFILE, nullHack, values);
		if (newRowId <= -1) {
			Log.e(TAG, "Failure on inserting key:" + key + " value:" + value);
			return false;
		}
		return true;
	}

	/**
	 * /** Check if the profile fields of a given SPFPersona contains the
	 * specified tag. Returns true when the tag is contained in at least one
	 * profile field value.
	 * 
	 * @param tag
	 * @param persona
	 * @return true if the profile contains the tag
	 */
	public boolean hasTag(String tag, SPFPersona persona) {
		SQLiteDatabase db = getReadableDatabase();
		String selection = Contract.COLUMN_VALUE + " LIKE ? AND " + Contract.COLUMN_PERSONA + " = ? ";
		String[] selectionArgs = { "%" + tag + "%", persona.getIdentifier() };
		String[] columns = { Contract.COLUMN_KEY };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		String limit = null;
		Cursor c = db.query(true, // distinct
				Contract.TABLE_PROFILE, // table name
				columns, // projection
				selection, // selection
				selectionArgs, // selection arguments
				groupBy, // don't group the rows
				having, // don't filter by row groups
				orderBy, // don't order by anything
				limit);

		boolean match = c.moveToNext();
		c.close();
		return match;
	}

	/**
	 * Returns the list of all the existing {@link SPFPersona}.
	 * 
	 * @return the list of {@link SPFPersona}
	 */
	List<SPFPersona> getAvailablePersonas() {
		SQLiteDatabase db = getReadableDatabase();
		String table = Contract.TABLE_PERSONAS;
		String[] columns = { Contract.COLUMN_PERSONA };
		String selection = null;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		List<SPFPersona> availablePersonas = new ArrayList<SPFPersona>();
		while (c.moveToNext()) {
			String p = c.getString(c.getColumnIndex(Contract.COLUMN_PERSONA));
			SPFPersona pers = new SPFPersona(p);
			availablePersonas.add(pers);
		}
		c.close();
		return availablePersonas;
	}

	/**
	 * Creates a new SPFPersona.
	 * 
	 * @param persona
	 *            - the {@link SPFPersona} to add.
	 */
	boolean addPersona(SPFPersona persona) {
		SQLiteDatabase db = getWritableDatabase();
		String table = Contract.TABLE_PERSONAS;
		String nullColumnHack = null;
		ContentValues values = new ContentValues();
		values.put(Contract.COLUMN_PERSONA, persona.getIdentifier());
		if (db.insert(table, nullColumnHack, values) > 0) {
			// copy the unique identifier
			ProfileFieldContainer pfc = getProfileFieldBulk(SPFPersona.getDefault(), ProfileField.IDENTIFIER);
			String id = pfc.getFieldValue(ProfileField.IDENTIFIER);
			if (setValue(ProfileField.IDENTIFIER.getIdentifier(), id, persona.getIdentifier())) {
				addCircleToFieldsInternal(DefaultCircles.PUBLIC, ProfileField.IDENTIFIER, persona, db);
				addCircleToFieldsInternal(DefaultCircles.PUBLIC, ProfileField.DISPLAY_NAME, persona, db);
				return true;
			} else {
				removePersona(persona);
				return false;
			}
		}
		return false;
	}

	/**
	 * Delete a SPFPersona. All the information related to the specified persona
	 * will be erased.
	 * 
	 * @param persona
	 *            - the {@link SPFPersona} to remove
	 */
	boolean removePersona(SPFPersona persona) {
		SQLiteDatabase db = getWritableDatabase();
		if (persona.getIdentifier().equals("default")) {
			return false;
		}

		String table = Contract.TABLE_PERSONAS;
		String selection = Contract.COLUMN_PERSONA + " = ?";
		String[] selectionArgs = { persona.getIdentifier() };
		if (db.delete(table, selection, selectionArgs) > 0) {
			deleteFieldsOf(persona, db);
			deleteVisibilityOf(persona, db);
		}
		return true;
	}

	private void deleteVisibilityOf(SPFPersona persona, SQLiteDatabase db) {
		String table = Contract.TABLE_VISIBILITY;
		String whereClause = Contract.COLUMN_PERSONA + " = ?";
		String[] whereArgs = { persona.getIdentifier() };
		db.delete(table, whereClause, whereArgs);
	}

	private void deleteFieldsOf(SPFPersona persona, SQLiteDatabase db) {
		String table = Contract.TABLE_PROFILE;
		String whereClause = Contract.COLUMN_PERSONA + " = ?";
		String[] whereArgs = { persona.getIdentifier() };
		db.delete(table, whereClause, whereArgs);
	}

	/**
	 * Adds a circle to a specified {@link ProfileField}.
	 * 
	 * @param field
	 *            - the {@link ProfileField} to modify
	 * @param circle
	 *            - the circle to add
	 * @param persona
	 *            - the {@link SPFPersona} to modify
	 * @return true if the operation was successful
	 */
	boolean addCircleToFields(String circle, ProfileField<?> field, SPFPersona p) {
		if (field.getIdentifier().equals(ProfileField.IDENTIFIER.getIdentifier()) || field.getIdentifier().equals(ProfileField.DISPLAY_NAME.getIdentifier())) {
			return false;
		}
		SQLiteDatabase db = getWritableDatabase();
		return addCircleToFieldsInternal(circle, field, p, db);
	}

	private boolean addCircleToFieldsInternal(String circle, ProfileField<?> field, SPFPersona p, SQLiteDatabase db) {
		String table = Contract.TABLE_VISIBILITY;
		ContentValues values = new ContentValues();
		values.put(Contract.COLUMN_KEY, field.getIdentifier());
		values.put(Contract.COLUMN_CIRCLE, circle);
		values.put(Contract.COLUMN_PERSONA, p.getIdentifier());
		String nullColumnHack = null;
		long rowId = db.insert(table, nullColumnHack, values);
		return rowId <= -1;
	}

	/**
	 * Remove a circle from a specified profile field.
	 * 
	 * @param field
	 *            the {@link ProfileField} to modify
	 * @param circle
	 *            - the circle to add
	 * @param persona
	 *            - the {@link SPFPersona} to modify
	 * @return true if the operation was successful
	 */
	boolean removeCircleFromField(String circle, ProfileField<?> field, SPFPersona p) {
		if (field.getIdentifier().equals(ProfileField.IDENTIFIER.getIdentifier()) || field.getIdentifier().equals(ProfileField.DISPLAY_NAME.getIdentifier())) {
			return false;
		}
		SQLiteDatabase db = getWritableDatabase();
		return removeCircleFromField(circle, field, p, db);
	}

	private boolean removeCircleFromField(String circle, ProfileField<?> field, SPFPersona p, SQLiteDatabase db) {
		String table = Contract.TABLE_VISIBILITY;
		String[] whereArgs = { p.getIdentifier(), field.getIdentifier(), circle };
		String whereClause = Contract.COLUMN_PERSONA + " = ? AND " + Contract.COLUMN_KEY + " = ? AND " + Contract.COLUMN_CIRCLE + " = ?";
		int rowAffected = db.delete(table, whereClause, whereArgs);
		return rowAffected > 0;
	}

	/**
	 * Returns a {@link Bundle} with profile fields identifiers as keys, and
	 * {@link ArrayList<String>} as circles.
	 * 
	 * @param persona
	 * @return
	 */
	Bundle getCirclesOf(SPFPersona persona) {
		SQLiteDatabase db = getReadableDatabase();
		String table = Contract.TABLE_VISIBILITY;
		String[] columns = { Contract.COLUMN_KEY, Contract.COLUMN_CIRCLE };
		String selection = Contract.COLUMN_PERSONA + " = ?";
		String[] selectionArgs = { persona.getIdentifier() };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Bundle b = new Bundle();
		while (c.moveToNext()) {
			String key = c.getString(c.getColumnIndex(Contract.COLUMN_KEY));
			String circle = c.getString(c.getColumnIndex(Contract.COLUMN_CIRCLE));
			ArrayList<String> list = b.getStringArrayList(key);
			if (list == null) {
				list = new ArrayList<String>();
			}
			list.add(circle);
			b.putStringArrayList(key, list);
		}
		c.close();
		return b;
	}

	ProfileFieldContainer getProfileFieldBulk(SPFPersona persona, ProfileField<?>... fields) {
		ProfileFieldContainer container = getProfileFieldBulkInternal(persona, ProfileField.toIdentifierList(fields));
		log("Returned container: " + container);
		return container;
	}

	ProfileFieldContainer getProfileFieldBulk(SPFPersona persona, String[] fields) {
		ProfileFieldContainer container = getProfileFieldBulkInternal(persona, fields);
		log("Returned container: " + container);
		return container;
	}

	ProfileFieldContainer getProfileFieldBulk(SPFPersona persona, String[] fields, PersonAuth auth) {
		List<String> visibleFields = visibleFields(auth, persona, fields);
		ProfileFieldContainerInternal container = getProfileFieldBulkInternal(persona, visibleFields.toArray(new String[0]));

		List<String> unaccessibleFields = new ArrayList<String>(Arrays.asList(fields));
		unaccessibleFields.removeAll(visibleFields);
		for (String s : unaccessibleFields) {
			container.setStatus(s, FieldStatus.UNACCESSIBLE);
			// FIXME unaccessible fields are invisible:
			// set to null in profile container
		}

		log("Returned container: " + container);
		return container;
	}

	void setProfileFieldBulk(SPFPersona persona, ProfileFieldContainer bulk) {
		ProfileFieldContainerInternal pfc = ProfileFieldContainerInternal.form(bulk);
		for (String key : pfc.getModifiedFieldIdentifiers()) {
			setValue(key, pfc.getFieldValue(key), persona.getIdentifier());
		}
		return;
	}

	private ProfileFieldContainerInternal getProfileFieldBulkInternal(SPFPersona persona, String[] fields) {
		log("Request for fields " + Arrays.toString(fields) + " of persona " + persona.getIdentifier());
		SQLiteDatabase db = getReadableDatabase();
		String table = Contract.TABLE_PROFILE;
		String[] columns = { Contract.COLUMN_VALUE, Contract.COLUMN_KEY };
		String inClause = getInClause(fields);
		String selection = Contract.COLUMN_PERSONA + " = ? AND " + Contract.COLUMN_KEY + " IN " + inClause;

		log("getBulkSelection: " + selection);
		String[] selectionArgs = { persona.getIdentifier() };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		ProfileFieldContainerInternal pfc = new ProfileFieldContainerInternal();

		log("container: " + pfc);
		while (c.moveToNext()) {
			String key = c.getString(c.getColumnIndex(Contract.COLUMN_KEY));
			String value = c.getString(c.getColumnIndex(Contract.COLUMN_VALUE));
			pfc.setInitialFieldValue(key, value);
		}
		c.close();
		return pfc;
	}

	/**
	 * Filters the parameter fields and retruns only the field that can be
	 * accessed given the permission provided with the {@link PersonAuth}
	 * parameter.
	 * 
	 * @param pAuth
	 *            - the permissions
	 * @param persona
	 *            - the {@link SPFPersona} to read
	 * @param fields
	 *            - the fields' identifiers to filter
	 * @return a list of accessible fields
	 */
	private List<String> visibleFields(PersonAuth pAuth, SPFPersona persona, String[] fields) {
		SQLiteDatabase db = getReadableDatabase();
		String table = Contract.TABLE_VISIBILITY;
		String[] columns = { Contract.COLUMN_KEY, Contract.COLUMN_CIRCLE };
		String inClause = getInClause(fields);
		String selection = Contract.COLUMN_PERSONA + " = ? AND " + Contract.COLUMN_KEY + " IN " + inClause;
		String[] selectionArgs = { persona.getIdentifier() };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		
		List<String> fieldKeys = new ArrayList<String>();
		List<String> privateFields = new ArrayList<String>();
		boolean allCircles = pAuth.getCircles().contains(DefaultCircles.ALL_CIRCLE);

		while (c.moveToNext()) {
			String key = c.getString(c.getColumnIndex(Contract.COLUMN_KEY));
			String circle = c.getString(c.getColumnIndex(Contract.COLUMN_CIRCLE));
			if (circle.equals(DefaultCircles.PRIVATE)){
				privateFields.add(circle);
			} else if (allCircles || pAuth.getCircles().contains(circle)) {
				fieldKeys.add(key);
			}
		}
		c.close();
		fieldKeys.removeAll(privateFields);
		return fieldKeys;
	}
	
	/**
	 * Return a SQL IN set parameter with the specified array of string e.g.
	 * "( 'arg1' , 'arg2' , 'arg3' )"
	 * 
	 * @param fields
	 * @return the IN clause
	 */
	private String getInClause(String[] fields) {
		StringBuilder builder = new StringBuilder("( ");
		if (fields.length != 0) {
			for (int i = 0; i < fields.length; i++) {
				builder.append("'");
				builder.append(fields[i]);
				builder.append("'");
				if (i != fields.length - 1) {
					builder.append(" , ");
				}
			}
		}
		builder.append(" )");
		return builder.toString();
	}

	private void log(String msg) {
		if (LOG) {
			Log.d(TAG, msg);
		}
	}

	private static class ProfileFieldContainerInternal extends ProfileFieldContainer {

		public static ProfileFieldContainerInternal form(ProfileFieldContainer source) {
			if (source instanceof ProfileFieldContainerInternal) {
				return (ProfileFieldContainerInternal) source;
			}

			return new ProfileFieldContainerInternal(source);
		}

		public ProfileFieldContainerInternal() {

		}

		public ProfileFieldContainerInternal(ProfileFieldContainer source) {
			super(source);
		}

		public void setInitialFieldValue(String fieldIdentifier, String fieldValue) {
			mFields.putString(fieldIdentifier, fieldValue);
			setStatus(fieldIdentifier, FieldStatus.ORIGINAL);
		}

		public String getFieldValue(String fieldIdentifier) {
			return mFields.getString(fieldIdentifier);
		}

		@Override
		public void setStatus(String field, FieldStatus status) {
			super.setStatus(field, status);
		}

		public List<String> getModifiedFieldIdentifiers() {
			List<String> result = new ArrayList<String>();
			for (String key : mStatus.keySet()) {
				FieldStatus status = getStatus(key);
				if (status == FieldStatus.MODIFIED || status == FieldStatus.DELETED) {
					result.add(key);
				}
			}

			return result;
		}
	}

}
