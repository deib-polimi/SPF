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
package it.polimi.spf.lib.services;

import it.polimi.spf.lib.Component;
import it.polimi.spf.lib.SPFPerson;

import it.polimi.spf.shared.aidl.LocalServiceManager;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * SPF component to create invocation stubs to invoke remote methods.
 * 
 * @author darioarchetti
 * 
 */
public final class SPFServiceLocalExecutor extends Component<SPFServiceLocalExecutor, LocalServiceManager> {

	private static final String SERVICE_INTENT = "it.polimi.spf.services.LocalServiceExecutor";
	private static final Component.Descriptor<SPFServiceLocalExecutor, LocalServiceManager> DESCRIPTOR = new Component.Descriptor<SPFServiceLocalExecutor, LocalServiceManager>() {

		@Override
		public String getActionName() {
			return SERVICE_INTENT;
		}

		@Override
		public LocalServiceManager castInterface(IBinder binder) {
			return LocalServiceManager.Stub.asInterface(binder);
		}

		@Override
		public SPFServiceLocalExecutor createInstance(Context context, LocalServiceManager serviceInterface, ServiceConnection connection, ConnectionCallback<SPFServiceLocalExecutor> callback) {
			return new SPFServiceLocalExecutor(context, serviceInterface, connection, callback);
		}
	};

	public static void load(Context context, final Callback callback) {
		Component.load(context, DESCRIPTOR, callback);
	}

	protected SPFServiceLocalExecutor(Context context, LocalServiceManager serviceInterface, ServiceConnection connection, ConnectionCallback<SPFServiceLocalExecutor> callback) {
		super(context, serviceInterface, connection, callback);
	}

	private InvocationStub.Target mLocalInvocationTarget = new InvocationStub.Target() {
		@Override
		public void prepareArguments(Object[] arguments) throws ServiceInvocationException {
			String token = getAccessToken();
			SPFError error = new SPFError();
			try {
				for (Object param : arguments) {
					if (param instanceof SPFActivity) {
						getService().injectInformationIntoActivity(token, (SPFActivity) param, error);
						if (!error.isOk()) {
							handleError(error);
							throw new ServiceInvocationException(error.toString());
						}
					}
				}
			} catch (RemoteException e) {
				catchRemoteException(e);
				throw new ServiceInvocationException(e.getClass().getSimpleName());
			}
		}

		@Override
		public InvocationResponse executeService(InvocationRequest request) throws ServiceInvocationException {
			String token = getAccessToken();
			SPFError error = new SPFError();

			try {
				InvocationResponse response = getService().executeLocalService(token, request, error);

				if (!error.isOk()) {
					handleError(error);
					response = InvocationResponse.error(error.toString());
				}

				return response;
			} catch (RemoteException e) {
				catchRemoteException(e);
				throw new ServiceInvocationException(e.getClass().getSimpleName());
			}

		}
	};

	/**
	 * Creates an invocation stub to send service invocation requests to the
	 * local person performing method calls. The stub is created from a provided
	 * service interface, which must be annotated with {@link ServiceInterface}
	 * describing the service. The method returns an object implementing the
	 * aforementioned interface that can be used to perform method invocation.
	 * 
	 * @param serviceInterface
	 *            - the interface of the service.
	 * @param classLoader
	 *            - the ClassLoader to load classes.
	 * @return an invocation stub to perform method calls.
	 */
	public <E> E createStub(Class<E> serviceInterface, ClassLoader classLoader) {
		return InvocationStub.from(serviceInterface, classLoader, mLocalInvocationTarget);
	}

	/**
	 * Creates an invocation stub to send service invocation requests to a
	 * target {@link SPFPerson} providing the name and the parameter list. The
	 * object is created from a {@link ServiceDescriptor} containing the
	 * required details.
	 * 
	 * @param target
	 *            - the person who the service invocation requests will be
	 *            dispatched to.
	 * @param descriptor
	 *            - the {@link ServiceDescriptor} of the service whose methods
	 *            to invoke.
	 * @return a {@link ServiceInvocationStub} to perform invocations of remote
	 *         services.
	 */
	public InvocationStub createStub(SPFServiceDescriptor descriptor) {
		return InvocationStub.from(descriptor, mLocalInvocationTarget);
	}

	/**
	 * Dispatches an activity to the local SPF instance. The framework will
	 * perform information injection into the activity: such information will be
	 * available to the caller once the method ends.
	 * 
	 * @param activity
	 *            - the activity to dispatch.
	 * @return - true if the activity has been correctly consumed.
	 */
	public boolean sendActivityLocally(SPFActivity activity) {
		String token = getAccessToken();
		SPFError err = new SPFError();
		InvocationResponse resp;

		try {
			getService().injectInformationIntoActivity(token, activity, err);
			if (!err.isOk()) {
				handleError(err);
				return false;
			}

			resp = getService().sendActivityLocally(token, activity, err);
		} catch (RemoteException e) {
			// TODO Error Management
			return false;
		}

		if (!err.isOk()) {
			handleError(err);
			return false;
		}

		if (!resp.isResult()) {
			return false;
		}

		return GsonHelper.gson.fromJson(resp.getPayload(), Boolean.class);
	}

	// We do not let Callback implement BaseCallback, otherwise applications
	// would need to cast Component<> to the actual implementation.
	public interface Callback extends ConnectionCallback<SPFServiceLocalExecutor> {

	}

	private void catchRemoteException(RemoteException e) {
		disconnect();
		getCallback().onDisconnect();
	}
}
