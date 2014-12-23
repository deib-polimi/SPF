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
package it.polimi.spf.lib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Utility class to manage the execution of code in different threds.
 * 
 * @author darioarchetti
 * 
 */
public class LooperUtils {

	/**
	 * Wraps a callback into a proxy that executes its methods on the main
	 * thread. Return types are not supported, a method with return type other
	 * than void always returns null when called on the proxy.
	 * 
	 * @param callbackInterface
	 *            - the interface of the callback.
	 * @param callback
	 *            - the callback implementation
	 * @return the proxy to execute methods on the main thread.
	 */
	public static <E> E onMainThread(Class<E> callbackInterface, final E callback) {
		Utils.notNull(callbackInterface, "callbackInterface must not be null");
		Utils.notNull(callback, "callback must not be null");

		final Handler handler = new Handler(Looper.getMainLooper());
		final String tag = callback.getClass().getSimpleName();

		Object proxy = Proxy.newProxyInstance(callbackInterface.getClassLoader(), new Class<?>[] { callbackInterface }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
				handler.post(new Runnable() {

					@Override
					public void run() {
						try {
							method.invoke(callback, args);
						} catch (IllegalAccessException e) {
							Log.e(tag, "Error executing method " + method.getName() + " on main thread.", e);
						} catch (IllegalArgumentException e) {
							Log.e(tag, "Error executing method " + method.getName() + " on main thread.", e);
						} catch (InvocationTargetException e) {
							Log.e(tag, "Error executing method " + method.getName() + " on main thread.", e);
						}
					}
				});

				return null;
			}
		});

		return callbackInterface.cast(proxy);
	}
}
