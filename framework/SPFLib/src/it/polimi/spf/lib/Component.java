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

import it.polimi.spf.shared.SPFInfo;
import it.polimi.spf.shared.model.SPFError;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;

/**
 * Base class for SPF components. A component is wrappers for an AIDL interface
 * implementing SPF features like Services, Profile and Notification, and
 * provides high level functionalities to applications leveraging on SPF.
 * Components are loaded using an asynchronous pattern providing a callback that
 * is notified when the binding to the remote service is completed.
 * 
 * Loading an SPF component is as simple as calling
 * {@link #load(Context, Descriptor, ConnectionCallback)}. Each concrete
 * component should provide its own <code>load(Context, Callback)</code> method
 * delegating to the one of the superclass, where Callback is a type-fixed
 * interface extending {@link ConnectionCallback}.
 * 
 * </br></br> Example of subclass:
 * 
 * <pre>
 * public ExampleComponent extends Component&lt;ExampleComponent, IExampleService&gt; {
 *    ...
 * }
 * </pre>
 * 
 * For such class, an example of {@link Descriptor} to be used in
 * {@link #load(Context, Descriptor, ConnectionCallback)} is <br/>
 * <br/>
 * 
 * <pre>
 * private static final Descriptor&lt;ExampleComponent, IExampleService&gt; DESCRIPTOR = new Descriptor&lt;ExampleComponent, IExampleService&gt;() {
 * 
 *     public String getActionName(){ ... }
 * 
 *     public IExampleService castInterface(IBinder binder){
 *         return IExampleService.Stub.asInterface(binder);
 *     }
 * 
 *     public ExampleComponentx createInstance(Context context, I serviceInterface, ServiceConnection connection, ConnectionCallback&lt;ExampleComponent&gt; callback){
 *         return new ExampleComponent(...);
 *     }
 * }
 * </pre>
 * 
 * @author darioarchetti
 * 
 * @param <C>
 *            the type of the Component concrete implementation
 * @param <I>
 *            the type of the AIDL interface that is wrapped by the component
 */
public abstract class Component<C extends Component<C, I>, I extends IInterface> {

	/**
	 * Loads a local component asynchronously.
	 * 
	 * @param context
	 *            - the context to use to bind to the service
	 * @param descriptor
	 *            - a {@link Descriptor} to handle the creation of the
	 *            component;
	 * @param callback
	 *            - the callback to be notified when the service is available
	 * @param <C>
	 *            the type of the Component concrete implementation
	 * @param <I>
	 *            the type of the AIDL interface that is wrapped by the
	 *            component
	 */
	protected static <C extends Component<C, I>, I extends IInterface> void load(
			final Context context, final Descriptor<C, I> descriptor, final ConnectionCallback<C> callback) {
		
		Utils.notNull(context, "context must not be null");
		Utils.notNull(descriptor, "context must not be null");

		if (AccessTokenManager.get(context).hasToken()) {
			bindToService(context, descriptor, callback);
		} else {
			AccessTokenManager.get(context).requireAccessToken(context, new AccessTokenManager.RegistrationCallback() {

				@Override
				public void onRegistrationSuccessful() {
					bindToService(context, descriptor, callback);
				}

				@Override
				public void onRegistrationError(SPFError errorMsg) {
					callback.onError(errorMsg);
				}
			});
		}

	}

	/**
	 * Performs the binding to the remote service
	 * 
	 * @param context
	 *            - the context used to bind to the service
	 * @param descriptor
	 *            - the {@link Descriptor} to handle the service
	 * @param callback
	 *            - the callback to notify of the service availability
	 */
	private static <C extends Component<C, I>, I extends IInterface> void bindToService(
		final Context context, final Descriptor<C, I> descriptor, final ConnectionCallback<C> callback) {
		
		Intent intent = new Intent();
		intent.setComponent(SPFInfo.getSPFServiceComponentName());
		intent.setAction(descriptor.getActionName());

		ServiceConnection connection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				I service = descriptor.castInterface(binder);
				C instance = descriptor.createInstance(context, service, this, callback);
				callback.onServiceReady(instance);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				callback.onDisconnect();
			}
		};

		if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE) == false) {
			callback.onError(new SPFError(SPFError.SPF_NOT_INSTALLED_ERROR_CODE));
		}
	}

	private I mServiceInterface;
	private Context mContext;
	private ServiceConnection mConnection;
	private ConnectionCallback<C> mCallback;

	protected Component(Context context, I serviceInterface, ServiceConnection connection, final ConnectionCallback<C> callback) {
		this.mServiceInterface = serviceInterface;
		this.mContext = context;
		this.mConnection = connection;
		this.mCallback = callback;
	}

	/**
	 * Disconnects the component from the remote service.
	 * 
	 * @see Context#unbindService(ServiceConnection)
	 */
	public void disconnect() {
		mContext.unbindService(mConnection);
	}

	/**
	 * Returns the callback provided to
	 * {@link #load(Context, Descriptor, ConnectionCallback)}
	 * 
	 * @return the callback
	 */
	protected ConnectionCallback<C> getCallback() {
		return mCallback;
	}

	/**
	 * Returns the service interface to which this component is bound.
	 * 
	 * @return the service interface
	 */
	protected I getService() {
		return mServiceInterface;
	}

	/**
	 * The context used to bind to the service.
	 * 
	 * @return the context
	 */
	protected Context getContext() {
		return mContext;
	}

	/**
	 * Returns the access token provided to this app by SPF upon registration.
	 * 
	 * @return the access token
	 */
	protected String getAccessToken() {
		return AccessTokenManager.get(getContext()).getAccessToken();
	}

	/**
	 * Performs common error handling operations. Subclasses may override it to
	 * provide specific behavior.
	 * 
	 * @param err
	 */
	protected void handleError(SPFError err) {
		if (err.codeEquals(SPFError.TOKEN_NOT_VALID_ERROR_CODE)) {
			AccessTokenManager.get(mContext).invalidateToken();
		}
		mCallback.onError(err);
	}

	/**
	 * Interface for callback objects that can be used to receive notification
	 * of the availability of the remote service.
	 * 
	 * @author darioarchetti
	 * 
	 * @param <C>
	 *            - the type of the {@link Component} implementation that this
	 *            class will be listen for availability.
	 */
	protected interface ConnectionCallback<C extends Component<C, ?>> {

		/**
		 * Called when the binding to the service has been completed and thus
		 * its services can be used. An instance of the component is provided to
		 * interact with the service.
		 * 
		 * @param componentInstance
		 *            - the instance of the component
		 */
		public void onServiceReady(C componentInstance);

		/**
		 * Called when an error occurred in the connection. After this call, the
		 * component instance and all remote objects obtained from it should not
		 * be used anymore.
		 * 
		 * @param err
		 *            - the error that occurred.
		 */
		public void onError(SPFError err);

		/**
		 * Called when the connection to the remote service has been terminated.
		 * After this call, the component instance and all remote objects
		 * obtained from it should not be used anymore.
		 */
		public void onDisconnect();
	}

	/**
	 * Contains utility method to handle the binding and the creation of a local
	 * component.
	 * 
	 * @author darioarchetti
	 * 
	 * 
	 * @param <C>
	 *            the type of the Component concrete implementation
	 * @param <I>
	 *            the type of the AIDL interface that is wrapped by the
	 *            component
	 */
	protected interface Descriptor<C extends Component<C, I>, I extends IInterface> {

		/**
		 * @return the action to be included in the intent when binding to
		 *         SPFService.
		 */
		public String getActionName();

		/**
		 * Casts a binder to the interface type handled by the component.
		 * 
		 * @param binder
		 *            - the remote binder
		 * @return - the actual stub to communicate with the remote service
		 */
		public I castInterface(IBinder binder);

		/**
		 * Creates a new instance of the local component when the binding is
		 * completed and the stub is available.
		 * 
		 * @param context
		 *            - the context used to bind to the service
		 * @param serviceInterface
		 *            - the stub of the remote service
		 * @param connection
		 *            - the service connection used to bind to the service,
		 *            needed for
		 *            {@link Context#unbindService(ServiceConnection)}
		 * @param callback
		 *            - the callback to be notified when an error occurs
		 * @return an instance of the component implementation.
		 */
		public C createInstance(Context context, I serviceInterface, ServiceConnection connection, ConnectionCallback<C> callback);
	}
}
