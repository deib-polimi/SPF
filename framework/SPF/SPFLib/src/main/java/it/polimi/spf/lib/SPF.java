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

import it.polimi.spf.lib.profile.SPFRemoteProfile;
import it.polimi.spf.lib.search.SPFSearch;
import it.polimi.spf.lib.services.SPFServiceExecutor;
import it.polimi.spf.lib.services.ServiceInvocationException;

import it.polimi.spf.shared.SPFInfo;
import it.polimi.spf.shared.aidl.SPFProximityService;
import it.polimi.spf.shared.aidl.SPFSearchCallback;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFSearchDescriptor;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Class that provides a unified access to components for the interaction of
 * people in proximity. The component is loaded in a static way using the
 * {@link #connect(Context, ConnectionListener)} method.
 * 
 * @author darioarchetti
 * 
 */
public class SPF extends Component<SPF, SPFProximityService> {

	public static final int SEARCH = 0;
	public static final int SERVICE_EXECUTION = 1;
	public static final int REMOTE_PROFILE = 2;
	private static final String TAG = "SPF";

	private static final Descriptor<SPF, SPFProximityService> DESCRIPTOR = new Descriptor<SPF, SPFProximityService>() {

		@Override
		public String getActionName() {
			return SPFInfo.ACTION_PROXIMITY_SERVER;
		}

		@Override
		public SPFProximityService castInterface(IBinder binder) {
			return SPFProximityService.Stub.asInterface(binder);
		}

		@Override
		public SPF createInstance(Context context, SPFProximityService serviceInterface, ServiceConnection connection, ConnectionCallback<SPF> callback) {
			return new SPF(context, serviceInterface, connection, callback);
		}
	};

	/**
	 * Creates a connection to SPF asynchronously.
	 * 
	 * @param context
	 *            - the context used to bind to SPF service.
	 * @param listener
	 *            - the listener that is notified when the connection to SPF is
	 *            ready, when the connection is closed or when an error occurs.
	 */
	public static void connect(final Context context, final ConnectionListener listener) {
		Component.load(context, DESCRIPTOR, asBase(listener));
	}

	// FIXME ConnectionListener should extends ConnectionCallback<SPF>, but method names are different
	private static ConnectionCallback<SPF> asBase(final ConnectionListener listener) {
		return new ConnectionCallback<SPF>() {

			@Override
			public void onServiceReady(SPF serviceInterface) {
				listener.onConnected(serviceInterface);
			}

			@Override
			public void onError(SPFError err) {
				listener.onError(err);
			}

			@Override
			public void onDisconnect() {
				listener.onDisconnected();
			}
		};
	}

	// Components
	private SPFSearch spfSearch;
	private SPFRemoteProfile spfRemoteProfile;
	private SPFServiceExecutor spfServiceExecutor;

	private SPF(Context context, SPFProximityService serverService, ServiceConnection serviceConnection, ConnectionCallback<SPF> callback) {
		super(context, serverService, serviceConnection, callback);
	}

	/**
	 * Provides access to the different components of SPF. The components are
	 * identified by static constants available in this class, and all share a
	 * common superclass, {@link SPFComponent}; the instances returned vy this
	 * method should be casted to the right class. Available services are:
	 * <ul>
	 * <li>{@link #SEARCH}: Searching of people in proximity. Returned
	 * instance should be casted to {@link SPFSearch}</li>
	 * <li>{@link #SERVICE_EXECUTION}: Execution of remote methods.
	 * Returned instance should be casted to {@link SPFServiceExecutor}</li>
	 * <li>{@link #REMOTE_PROFILE}: Retrieval of information from
	 * remote profiles. Returned instances should be casted to
	 * {@link SPFRemoteProfile}</li>
	 * </ul>
	 * 
	 * @param code
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E> E getComponent(int code) {
		switch (code) {
		case REMOTE_PROFILE:
			if (spfRemoteProfile == null) {
				spfRemoteProfile = new SPFRemoteProfile(getContext(), this);
			}
			return (E) spfRemoteProfile;

		case SEARCH:
			if (spfSearch == null) {
				spfSearch = new SPFSearch(getContext(), this);
			}
			return (E) spfSearch;
		case SERVICE_EXECUTION:
			if (spfServiceExecutor == null) {
				spfServiceExecutor = new SPFServiceExecutor(getContext(), this);
			}
			return (E) spfServiceExecutor;
		default:
			throw new IllegalArgumentException("Component code " + code + " not found.");
		}
	}

	/**
	 * Disconnects from SPF. All created components will stop working after this
	 * method is invoked.
	 */
	@Override
	public void disconnect() {
		// TODO release resources
		if (spfSearch != null) {
			spfSearch.recycle();
		}
		;
		// spfRemoteProfile.recycle();
		// spfServiceExecutor.recycle();
		super.disconnect();
	}

	/*
	 * Implementation of SearchInterface (non-Javadoc)
	 * 
	 * @see it.polimi.spf.lib.async.search.SearchInterface#startSearch(it.polimi
	 * .dei.spf.framework.local.SearchDescriptor,
	 * it.polimi.spf.lib.async.search.SearchInterface.SearchStartedCallback)
	 */

	public void startSearch(SPFSearchDescriptor searchDescriptor, SPFSearchCallback callback) {
		try {
			SPFError err = new SPFError();
			getService().startNewSearch(getAccessToken(), searchDescriptor, callback, err);
			if (!err.isOk()) {
				handleError(err);
			}
		} catch (RemoteException e) {
			// TODO Error management
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.lib.async.search.SearchInterface#stopSearch(java.lang
	 * .String)
	 */

	public void stopSearch(String queryId) {
		try {
			SPFError err = new SPFError();
			getService().stopSearch(getAccessToken(), queryId, err);
			if (!err.isOk()) {
				handleError(err);
			}
		} catch (RemoteException e) {
			// TODO: error management
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.lib.async.search.SearchInterface#lookup(java.lang.String )
	 */

	public boolean lookup(String identifier) {
		try {
			SPFError err = new SPFError();
			boolean found = getService().lookup(getAccessToken(), identifier, err);
			if (err.isOk()) {
				return found;
			} else {
				handleError(err);
				return false;
			}
		} catch (RemoteException e) {
			// TODO Error Management
			Log.d(TAG, "Remote exception while executing lookup on " + identifier, e);
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.lib.async.profile.RemoteProfileInterface#getProfileBulk
	 * (java.lang.String, java.lang.String[],
	 * it.polimi.spf.framework.local.SPFError)
	 */

	public ProfileFieldContainer getProfileBulk(String identifier, String[] fields, SPFError err) {
		String accessToken = getAccessToken();
		try {
			ProfileFieldContainer pfc = getService().getProfileBulk(accessToken, identifier, fields, err);
			if (err.isOk()) {
				return pfc;
			}
		} catch (RemoteException e) {
			Log.e(TAG, "Error @ getProfileBulk", e);
		}
		handleError(err);
		return null;
	}

	public void injectActivities(String target, Object[] arguments) throws ServiceInvocationException {
		String token = getAccessToken();
		SPFError err = new SPFError();
		try {
			for (Object arg : arguments) {
				if (arg instanceof SPFActivity) {
					getService().injectInformationIntoActivity(token, target, (SPFActivity) arg, err);
					if (!err.isOk()) {
						handleError(err);
						throw new ServiceInvocationException();
					}
				}
			}
		} catch (RemoteException e) {
			throw new ServiceInvocationException(e);
		}
	}

	public InvocationResponse executeService(String target, InvocationRequest request) {
		String token = getAccessToken();
		SPFError err = new SPFError();
		try {
			InvocationResponse response = getService().executeRemoteService(token, target, request, err);
			if (!err.isOk()) {
				handleError(err);
				response = InvocationResponse.error(err.toString());
			}
			return response;
		} catch (RemoteException e) {
			return InvocationResponse.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.lib.async.services.execution.ServiceExecutionInterface
	 * #sendActivity(it.polimi.spf.framework.local.SPFActivity)
	 */

	public InvocationResponse sendActivity(String target, SPFActivity activity) {
		String token = getAccessToken();
		SPFError err = new SPFError();
		InvocationResponse resp;
		try {
			getService().injectInformationIntoActivity(token, target, activity, err);
			if (!err.isOk()) {
				handleError(err);
				return InvocationResponse.error(err.toString());
			}
			resp = getService().sendActivity(token, target, activity, err);
		} catch (RemoteException e) {
			return InvocationResponse.error(e);
		}

		if (!err.isOk()) {
			handleError(err);
			resp = InvocationResponse.error(err.toString());
		}

		return resp;
	}

	/**
	 * Interface for components that wants to be notified of the status of the
	 * connection with SPF.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public static interface ConnectionListener {

		/**
		 * Called when the connection to SPF is available.
		 * 
		 * @param instance
		 *            - the instance of SPF to use to interact with people in
		 *            the proximity.
		 */
		public void onConnected(SPF instance);

		/**
		 * Called when an error occurs.
		 */
		public void onError(SPFError errorMsg);

		/**
		 * Called when the connection with SPF is closed.
		 */
		public void onDisconnected();

	}
}
