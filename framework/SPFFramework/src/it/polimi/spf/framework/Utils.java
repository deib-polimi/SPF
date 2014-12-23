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
package it.polimi.spf.framework;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class Utils {

	/**
	 * Helper to log a call to a method
	 * 
	 * @param tag
	 *            - the tag
	 * @param methodName
	 *            - the name of the method
	 * @param args
	 *            - the method args
	 */
	public static void logCall(String tag, String methodName, Object... args) {
		if (SPFConfig.DEBUG) {
			Log.d(tag, "method call: " + methodName + "(" + (args != null ? TextUtils.join(",", args) : "") + ")");
		}
	}

	public static void printDatabase(String tag, SQLiteDatabase database, String tableName) {
		if (!SPFConfig.DEBUG) {
			return;
		}

		Cursor c = database.query(tableName, null, null, null, null, null, null);
		if (c.getCount() == 0) {
			Log.d(tag, "Database table " + tableName + " is empty");
			return;
		}

		Log.d(tag, "Table " + tableName);
		String[] columns = c.getColumnNames();
		int[] indexes = new int[columns.length];
		for (int i = 0; i < columns.length; i++) {
			indexes[i] = c.getColumnIndexOrThrow(columns[i]);
		}

		while (c.moveToNext()) {
			StringBuilder builder = new StringBuilder();
			builder.append(c.getPosition());
			builder.append(". ");
			for (int index : indexes) {
				builder.append(c.getString(index));
				builder.append(" ");
			}
			builder.append("\n");
			Log.d(tag, builder.toString());
		}

		c.close();
	}
}
