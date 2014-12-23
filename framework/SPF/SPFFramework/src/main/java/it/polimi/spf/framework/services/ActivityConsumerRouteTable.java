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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFServiceDescriptor;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Persistence component that stores details on SPF Services capable of
 * consuming {@link SPFActivity}, together with the default service to handle
 * each registered verb.
 * 
 * @author darioarchetti
 * 
 */
/* package */class ActivityConsumerRouteTable extends SQLiteOpenHelper {

	private interface Contract extends BaseColumns {
		public static final String TABLE_SERVICES = "activity_service_table";
		public static final String TABLE_DEFAULTS = "activity_defaults_table";
		public static final String APP_ID = "appIdentifier";
		public static final String VERB = "verb";
		public static final String SERVICE_NAME = "service";
		public static final String DEFAULT = "isDef";
	}

	private final static String TAG = "ActivityConsumerTable";
	private final static String DATABASE_NAME = "activityConsumer.db";
	private final static int DATABASE_VERSION = 1;
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	//@formatter:off
	private static final String SQL_CREATE_SERVICES = "CREATE TABLE " + Contract.TABLE_SERVICES + "("
			+ Contract._ID          + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP
			+ Contract.APP_ID       + TEXT_TYPE    + COMMA_SEP
			+ Contract.SERVICE_NAME + TEXT_TYPE    + COMMA_SEP
			+ Contract.VERB         + TEXT_TYPE    + COMMA_SEP
			+ Contract.DEFAULT      + INTEGER_TYPE + COMMA_SEP
			+ "UNIQUE (" + Contract.APP_ID       + COMMA_SEP
						 + Contract.SERVICE_NAME + COMMA_SEP
						 + Contract.VERB         + ") ON CONFLICT REPLACE)";
	
	private static final String SQL_CREATE_DEFAULTS = "CREATE TABLE " + Contract.TABLE_DEFAULTS + "("
			+ Contract._ID          + INTEGER_TYPE + " PRIMARY KEY"                + COMMA_SEP
			+ Contract.VERB         + TEXT_TYPE    + " UNIQUE ON CONFLICT REPLACE" + COMMA_SEP
			+ Contract.APP_ID       + TEXT_TYPE    + COMMA_SEP
			+ Contract.SERVICE_NAME + TEXT_TYPE    + ")";

	private static final String SQL_DELETE_SERVICES = "DROP TABLE IF EXISTS " + Contract.TABLE_SERVICES;
	private static final String SQL_DELETE_DEFAULTS = "DROP TABLE IF EXISTS " + Contract.TABLE_DEFAULTS;
	//@formatter:on

	public ActivityConsumerRouteTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_SERVICES);
		db.execSQL(SQL_CREATE_DEFAULTS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_SERVICES);
		db.execSQL(SQL_DELETE_DEFAULTS);
		onCreate(db);
	}

	/**
	 * Returns the {@link ServiceIdentifier} of the service designated to handle
	 * the given activity, or null if no service can handle such activity.
	 * 
	 * @param activity
	 *            . the {@link SPFActivity} to handle.
	 * @return the serviceIdentifier of the service that should handle the
	 *         activity.
	 */
	public ServiceIdentifier getServiceFor(SPFActivity activity) {
		return getServiceForInternal(activity.getVerb());
	}

	private ServiceIdentifier getServiceForInternal(String verb) {
		String where = Contract.VERB + " = ?";
		String[] args = { verb };
		String[] columns = { Contract.APP_ID, Contract.SERVICE_NAME };
		Cursor c = getReadableDatabase().query(Contract.TABLE_DEFAULTS, columns, where, args, null, null, null);

		//@formatter:off
		ServiceIdentifier identifier = !c.moveToFirst() ? null :
			new ServiceIdentifier(
				c.getString(c.getColumnIndexOrThrow(Contract.APP_ID)),
				c.getString(c.getColumnIndexOrThrow(Contract.SERVICE_NAME))
			);
		//@formatter:on
		c.close();
		return identifier;
	}

	/**
	 * 
	 * @param verb
	 * @param appId
	 * @param serviceName
	 * @return
	 */
	public boolean setDefaultServiceForVerb(String verb, ServiceIdentifier identifier) {
		return setDefaultServiceForVerb(verb, identifier.getAppId(), identifier.getServiceName());
	}

	private boolean setDefaultServiceForVerb(String verb, String appId, String serviceName) {
		ContentValues cv = new ContentValues();
		cv.put(Contract.VERB, verb);
		cv.put(Contract.APP_ID, appId);
		cv.put(Contract.SERVICE_NAME, serviceName);

		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert(Contract.TABLE_DEFAULTS, null, cv);
		if (id == -1) {
			return false;
		}

		return true;
	}

	/**
	 * Registers the capabilities of a service to consume activities. If the
	 * descriptor doesn't declare the service as capable of handling activities,
	 * no action will be performed.
	 * 
	 * @param descriptor
	 *            - the descriptor of the service to register
	 * @return true if the service was registered
	 */
	public boolean registerService(SPFServiceDescriptor descriptor) {
		String appId = descriptor.getAppIdentifier();
		String serviceName = descriptor.getServiceName();

		for (String verb : descriptor.getConsumedVerbs()) {
			if (!registerServiceInternal(verb, serviceName, appId)) {
				return false;
			}
			
			Log.v(TAG, "Registered service as Activity consumer: " + descriptor.getServiceName());
		}

		return true;
	}

	private boolean registerServiceInternal(String verb, String serviceName, String appId) {
		ContentValues cv = new ContentValues();
		SQLiteDatabase db = getWritableDatabase();

		cv.put(Contract.VERB, verb);
		cv.put(Contract.APP_ID, appId);
		cv.put(Contract.SERVICE_NAME, serviceName);
		long id = db.insert(Contract.TABLE_SERVICES, null, cv);

		if (id == -1) {
			return false;
		}

		if (getServiceForInternal(verb) == null) {
			return setDefaultServiceForVerb(verb, appId, serviceName);
		}

		return true;
	}

	/**
	 * TODO #ServiceUnregistration
	 * 
	 * @param descriptor
	 * @return
	 */
	public boolean unregisterService(SPFServiceDescriptor descriptor) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * TODO #ServiceUnregistration
	 * 
	 * @param appIdentifier
	 * @return
	 */
	public boolean unregisterAllServicesOfApp(String appIdentifier) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns a list containing the default service for each verb, as an
	 * instance of {@link VerbSupport}
	 * 
	 * @return - the list of {@link VerbSupport}
	 */
	public Collection<VerbSupport> getVerbSupport() {
		Map<String, VerbSupport> entries = new HashMap<String, VerbSupport>();

		String[] columns = { Contract.APP_ID, Contract.SERVICE_NAME, Contract.VERB };
		Cursor c = getReadableDatabase().query(true, Contract.TABLE_SERVICES, columns, null, null, null, null, null, null);

		int appIndex = c.getColumnIndexOrThrow(Contract.APP_ID);
		int verbIndex = c.getColumnIndexOrThrow(Contract.VERB);
		int svcIndex = c.getColumnIndexOrThrow(Contract.SERVICE_NAME);

		while (c.moveToNext()) {
			String verb = c.getString(verbIndex);
			String appId = c.getString(appIndex);
			String svcName = c.getString(svcIndex);

			VerbSupport d = entries.get(verb);
			if (d == null) {
				d = new VerbSupport(verb);
				entries.put(verb, d);
			}

			d.addSupportingService(new ServiceIdentifier(appId, svcName));
		}

		c.close();

		for (VerbSupport d : entries.values()) {
			d.setDefaultApp(getServiceForInternal(d.getVerb()));
		}

		return entries.values();
	}
}