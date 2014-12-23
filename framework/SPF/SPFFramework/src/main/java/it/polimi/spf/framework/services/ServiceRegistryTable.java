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
package it.polimi.spf.framework.services;

import it.polimi.spf.shared.model.SPFServiceDescriptor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * Refactored version of {@link DispatchTable} that only stores appnames and
 * servicenames
 * 
 * @author darioarchetti
 * 
 */
/* package */class ServiceRegistryTable extends SQLiteOpenHelper {

	public static final String TAG = "ServiceTable";

	public static class Contract implements BaseColumns {
		public static final String TABLE_NAME = "spfservice";
		public static final String COLUMN_SERVICE_NAME = "service_name";
		public static final String COLUMN_SERVICE_DESCRIPTION = "service_description";
		public static final String COLUMN_APP_IDENTIFIER = "app_identifier";
		public static final String COLUMN_VERSION = "version";
		public static final String COLUMN_COMPONENT_NAME = "component_name";
		public static final String COLUMN_CONSUMED_VERBS = "consumed_verbs";
	}

	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "ServiceTable.db";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	//@formatter:off
	private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + Contract.TABLE_NAME + " ("
			+ Contract._ID                        + INTEGER_TYPE + " PRIMARY KEY"      + COMMA_SEP
			+ Contract.COLUMN_SERVICE_NAME        + TEXT_TYPE    + COMMA_SEP
			+ Contract.COLUMN_SERVICE_DESCRIPTION + TEXT_TYPE    + COMMA_SEP
			+ Contract.COLUMN_APP_IDENTIFIER      + TEXT_TYPE    + COMMA_SEP
			+ Contract.COLUMN_VERSION             + TEXT_TYPE    + COMMA_SEP
			+ Contract.COLUMN_COMPONENT_NAME      + TEXT_TYPE    + COMMA_SEP
			+ Contract.COLUMN_CONSUMED_VERBS      + TEXT_TYPE    + COMMA_SEP
			+ "UNIQUE ( " + Contract.COLUMN_APP_IDENTIFIER + COMMA_SEP
						  + Contract.COLUMN_SERVICE_NAME + ") ON CONFLICT REPLACE )";
	//@formatter:on

	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Contract.TABLE_NAME;
	private static final String DELIMITER = ";";

	public ServiceRegistryTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public boolean registerService(SPFServiceDescriptor descriptor) {
		SQLiteDatabase db = getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Contract.COLUMN_SERVICE_NAME, descriptor.getServiceName());
		values.put(Contract.COLUMN_SERVICE_DESCRIPTION, descriptor.getDescription());
		values.put(Contract.COLUMN_APP_IDENTIFIER, descriptor.getAppIdentifier());
		values.put(Contract.COLUMN_VERSION, descriptor.getVersion());
		values.put(Contract.COLUMN_COMPONENT_NAME, descriptor.getComponentName());
		values.put(Contract.COLUMN_CONSUMED_VERBS, TextUtils.join(DELIMITER, descriptor.getConsumedVerbs()));

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		newRowId = db.insert(Contract.TABLE_NAME, null, values);

		if (newRowId == -1) {
			Log.e(TAG, "Db insert returns -1");
			return false;
		}

		Log.v(TAG, "Registered service " + descriptor.getServiceName() + ", id = " + newRowId);
		return true;
	}

	public boolean unregisterAllServicesOfApp(String appIdentifier) {
		SQLiteDatabase db = getWritableDatabase();
		// Define 'where' part of query.
		String selection = Contract.COLUMN_APP_IDENTIFIER + " = ? ";

		// Specify arguments in placeholder order.
		String[] selectionArgs = { appIdentifier };

		// Issue SQL statement.
		return db.delete(Contract.TABLE_NAME, selection, selectionArgs) > 0;
	}

	public boolean unregisterService(SPFServiceDescriptor descriptor) {
		SQLiteDatabase db = getWritableDatabase();
		// Define 'where' part of query.
		String selection = Contract.COLUMN_APP_IDENTIFIER + " = ? " + "AND " + Contract.COLUMN_SERVICE_NAME + "= ? ";

		// Specify arguments in placeholder order.
		String[] selectionArgs = { descriptor.getAppIdentifier(), descriptor.getServiceName() };

		// Issue SQL statement.
		return db.delete(Contract.TABLE_NAME, selection, selectionArgs) > 0;
	}

	public String getComponentForService(String appName, String serviceName) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ? AND " + Contract.COLUMN_SERVICE_NAME + " = ?";
		String[] whereArgs = { appName, serviceName };
		String[] columns = { Contract.COLUMN_COMPONENT_NAME };

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(Contract.TABLE_NAME, columns, where, whereArgs, null, null, null);

		if (!c.moveToNext()) {
			return null;
		}

		String intent = c.getString(c.getColumnIndexOrThrow(Contract.COLUMN_COMPONENT_NAME));
		c.close();

		return intent;
	}

	public String getComponentForService(ServiceIdentifier id) {
		if (id == null) {
			return null;
		}

		return getComponentForService(id.getAppId(), id.getServiceName());
	}

	public SPFServiceDescriptor[] getServicesOfApp(String appIdentifier) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String[] whereArgs = { appIdentifier };
		Cursor c = getReadableDatabase().query(Contract.TABLE_NAME, null, where, whereArgs, null, null, null);

		int nameIndex = c.getColumnIndexOrThrow(Contract.COLUMN_SERVICE_NAME);
		int descriptionIndex = c.getColumnIndexOrThrow(Contract.COLUMN_SERVICE_DESCRIPTION);
		int versionIndex = c.getColumnIndexOrThrow(Contract.COLUMN_VERSION);
		int componentName = c.getColumnIndexOrThrow(Contract.COLUMN_COMPONENT_NAME);
		int consumedVerbs = c.getColumnIndexOrThrow(Contract.COLUMN_CONSUMED_VERBS);
		
		SPFServiceDescriptor[] services = new SPFServiceDescriptor[c.getCount()];
		int i = 0;
		while (c.moveToNext()) {
			services[i++] = new SPFServiceDescriptor(
				c.getString(nameIndex),
				c.getString(descriptionIndex),
				appIdentifier,
				c.getString(versionIndex),
				c.getString(componentName),
				TextUtils.split(c.getString(consumedVerbs), DELIMITER));
		}

		c.close();
		return services;
	}

	public void deleteTable() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(SQL_DELETE_ENTRIES);
		onCreate(db);
	}
}