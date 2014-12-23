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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import it.polimi.spf.framework.IdentifierGenerator;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.AppDescriptor;

/**
 * Registry that lists authorized applications.
 * 
 * 
 * 
 * TODO database table - app identifier (string) - token (string) - permissions
 * (list of strings) - persona (string)
 * 
 * @author darioarchetti
 * 
 */
public class ApplicationRegistry {

	private static final String DEREGISTRATION_INTENT = "it.polimi.spf.framework.AppDeregistered";
	private static final String DEREGISTERED_APP = "appIdentifier";

	private final RegistryTable mRegistryTable;
	private final IdentifierGenerator mTokenGenerator;
	private final Context mContext;

	public ApplicationRegistry(Context context) {
		if (context == null) {
			throw new NullPointerException();
		}

		mRegistryTable = new RegistryTable(context);
		mTokenGenerator = new IdentifierGenerator();
		mContext = context;
	}

	public AppAuth getAppAuthorization(String accessToken) throws TokenNotValidException {
		if (accessToken == null) {
			throw new NullPointerException();
		}

		String where = Contract.COLUMN_ACCESS_TOKEN + " = ?";
		String[] whereArgs = { accessToken };

		Cursor c = mRegistryTable.getReadableDatabase().query(Contract.TABLE_NAME, null, where, whereArgs, null, null, null);

		if (!c.moveToFirst()) {
			throw new TokenNotValidException();
		}

		AppAuth a = appAuthFromCursor(c);
		c.close();
		return a;
	}

	/**
	 * Retrieves an appauth given the identifier of an application
	 * 
	 * @param appId
	 *            - the Id of the application
	 * @return the appAuth of the application, or null if the app is not
	 *         installed
	 */
	public AppAuth getAppAuthorizationByAppId(String appId) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String args[] = { appId };
		Cursor c = mRegistryTable.getReadableDatabase().query(Contract.TABLE_NAME, null, where, args, null, null, null);
		AppAuth auth = null;
		if (c.moveToFirst()) {
			auth = appAuthFromCursor(c);
		}
		c.close();
		return auth;
	}

	/**
	 * Low level method to register applications. It will not validate the
	 * descriptor given as parameter.
	 * 
	 * @param descriptor
	 * @param persona
	 * @return - the token to give back.
	 */
	public String registerApplication(AppDescriptor descriptor, SPFPersona persona) {
		String token = mTokenGenerator.generateAccessToken();

		ContentValues cv = new ContentValues();
		cv.put(Contract.COLUMN_APP_NAME, descriptor.getAppName());
		cv.put(Contract.COLUMN_ACCESS_TOKEN, token);
		cv.put(Contract.COLUMN_APP_IDENTIFIER, descriptor.getAppIdentifier());
		cv.put(Contract.COLUMN_PERMISSION_CODE, descriptor.getPermissionCode());
		cv.put(Contract.COLUMN_PERSONA, persona.getIdentifier());

		SQLiteDatabase db = mRegistryTable.getWritableDatabase();
		if (db.insert(Contract.TABLE_NAME, null, cv) == -1) {
			return null; // TODO handle insertion error
		}

		return token;
	}

	/**
	 * To be called from application uninstall monitor
	 * 
	 * @param appIdentifier
	 */
	public boolean unregisterApplication(String appIdentifier) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String[] whereArgs = { appIdentifier };
		if (mRegistryTable.getWritableDatabase().delete(Contract.TABLE_NAME, where, whereArgs) == 0) {
			return false;
		}

		if (SPF.get().getServiceRegistry().unregisterAllServicesOfApp(appIdentifier)) {
			Intent i = new Intent(DEREGISTRATION_INTENT);
			i.putExtra(DEREGISTERED_APP, appIdentifier);
			mContext.sendBroadcast(i);
			return true;
		}

		return false;
	}

	public boolean isAppRegistered(String appIdentifier) {
		String where = Contract.COLUMN_APP_IDENTIFIER + " = ?";
		String[] whereArgs = { appIdentifier };
		String[] columns = {};

		return mRegistryTable.getReadableDatabase().query(Contract.TABLE_NAME, columns, where, whereArgs, null, null, null).moveToFirst();
	}

	public List<AppAuth> getAvailableApplications() {
		Cursor c = mRegistryTable.getReadableDatabase().query(Contract.TABLE_NAME, null, null, null, null, null, null);
		List<AppAuth> result = new ArrayList<AppAuth>();
		while (c.moveToNext()) {
			result.add(appAuthFromCursor(c));
		}

		c.close();
		return result;
	}

	/**
	 * Returns the SPFPersona associated to the specified application. If the
	 * application does not exist returns the default SPFPersona.
	 * 
	 * @param appIdentifier
	 *            - the identifier of the application
	 * @return a SPFPersona
	 */
	public SPFPersona getPersonaOf(String appIdentifier) {
		SQLiteDatabase db = mRegistryTable.getReadableDatabase();
		String table = Contract.TABLE_NAME;
		String[] columns = { Contract.COLUMN_PERSONA };
		String selection = Contract.COLUMN_APP_IDENTIFIER + " = ? ";
		String[] selectionArgs = { appIdentifier };
		String groupBy = null;
		String having = null;
		String orderBy = null;
		Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		if (c.moveToNext()) {
			String persona = c.getString(c.getColumnIndex(Contract.COLUMN_PERSONA));
			return new SPFPersona(persona);
		}

		c.close();
		return SPFPersona.DEFAULT;
	}

	private AppAuth appAuthFromCursor(Cursor c) {
		String appName = c.getString(c.getColumnIndexOrThrow(Contract.COLUMN_APP_NAME));
		String identifier = c.getString(c.getColumnIndexOrThrow(Contract.COLUMN_APP_IDENTIFIER));
		int permissionCode = c.getInt(c.getColumnIndexOrThrow(Contract.COLUMN_PERMISSION_CODE));
		String persona = c.getString(c.getColumnIndexOrThrow(Contract.COLUMN_PERSONA));

		return new AppAuth(appName, identifier, permissionCode, persona);
	}

	private static class Contract implements BaseColumns {
		public static final String TABLE_NAME = "applications";
		public static final String COLUMN_APP_NAME = "app_name";
		public static final String COLUMN_APP_IDENTIFIER = "app_identifier";
		public static final String COLUMN_ACCESS_TOKEN = "access_token";
		public static final String COLUMN_PERMISSION_CODE = "permissions";
		public static final String COLUMN_PERSONA = "persona";
	}

	/**
	 * Helper to manage the database table
	 * 
	 * @author darioarchetti
	 * 
	 */
	private static class RegistryTable extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "applications.db";
		private static final int DATABASE_VERSION = 2;
		private static final String TEXT_TYPE = " TEXT";
		private static final String INTEGER_TYPE = " INTEGER";
		private static final String COMMA_SEP = ",";

		private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + Contract.TABLE_NAME + " (" + Contract._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP + // ID
		Contract.COLUMN_APP_NAME + TEXT_TYPE + COMMA_SEP + // app name
		Contract.COLUMN_APP_IDENTIFIER + TEXT_TYPE + " UNIQUE ON CONFLICT REPLACE" + COMMA_SEP + // identifier
		Contract.COLUMN_ACCESS_TOKEN + TEXT_TYPE + COMMA_SEP + // access token
		Contract.COLUMN_PERMISSION_CODE + INTEGER_TYPE + COMMA_SEP + // permissions
		Contract.COLUMN_PERSONA + TEXT_TYPE + ")"; // Persona

		private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Contract.TABLE_NAME;

		public RegistryTable(Context context) {
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
	}

}