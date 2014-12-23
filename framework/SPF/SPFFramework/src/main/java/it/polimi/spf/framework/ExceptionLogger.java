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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * Logs unhandled exceptions in a file so that they can be inspected without
 * LogCat. Files are located in the external files dir of the app (see
 * {@link Context#getExternalFilesDir(String)}) in a subfolder called
 * {@value #FOLDER}. Once the exception has been logged, it falls back to the
 * default handler.
 * 
 * @author darioarchetti
 * 
 */
public class ExceptionLogger implements UncaughtExceptionHandler {

	private final static String FOLDER = "crashes/";

	private final Context mContext;
	private final UncaughtExceptionHandler mDefaultHandler;

	private ExceptionLogger(Context c, UncaughtExceptionHandler defaultHandler) {
		mContext = c;
		mDefaultHandler = defaultHandler;
	}

	/**
	 * Installs an instance of {@link ExceptionLogger} as the default in the
	 * Thread class
	 * 
	 * @see Thread#setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler)
	 * @param c
	 */
	public static void installAsDefault(Context c) {
		UncaughtExceptionHandler def = Thread.getDefaultUncaughtExceptionHandler();
		UncaughtExceptionHandler newHandler = new ExceptionLogger(c, def);
		Thread.setDefaultUncaughtExceptionHandler(newHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
	 * .Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Date now = new Date();
		String name = FOLDER + now.getTime() + ".txt";

		try {
			File folder = new File(mContext.getExternalFilesDir(null), FOLDER);
			if (!folder.exists()) {
				folder.mkdir();
			}

			File file = new File(mContext.getExternalFilesDir(null), name);
			if (!file.exists()) {
				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file, false));
			writer.println("Exception in " + thread.getName() + " @ " + DateFormat.getDateFormat(mContext).format(now));
			ex.printStackTrace(writer);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "Exception logging uncaught exception: ", e);
		}

		// Dispatch to default handler to make app crash
		mDefaultHandler.uncaughtException(thread, ex);
	}

}
