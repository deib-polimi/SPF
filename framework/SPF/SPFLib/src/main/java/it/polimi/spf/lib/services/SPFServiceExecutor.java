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

import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

import android.content.Context;

/**
 * A {@link SPFComponent} that allows the invocation of the services of a remote
 * person.
 * 
 * @author darioarchetti
 * 
 */
public class SPFServiceExecutor {

	private SPF mExecutionInterface;

	// TODO @hide
	public SPFServiceExecutor(Context context, SPF executionInterface) {

		this.mExecutionInterface = executionInterface;
	}

	/**
	 * Creates an invocation stub to send service invocation requests to a
	 * target {@link SPFPerson} performing method calls. The stub is created
	 * from a provided service interface, which must be annotated with
	 * {@link ServiceInterface} describing the service. The method returns an
	 * object implementing the aforementioned interface that can be used to
	 * perform method invocation.
	 * 
	 * @param target
	 *            - the person who the service invocation requests will be
	 *            dispatched to.
	 * @param serviceInterface
	 *            - the interface of the service.
	 * @param classLoader
	 *            - the ClassLoader to load classes.
	 * @return an invocation stub to perform method calls.
	 */
	public <E> E createStub(final SPFPerson target, Class<E> serviceInterface, ClassLoader classLoader) {
		return InvocationStub.from(serviceInterface, classLoader, new InvocationStub.Target() {

			@Override
			public void prepareArguments(Object[] arguments) throws ServiceInvocationException {
				mExecutionInterface.injectActivities(target.getIdentifier(), arguments);
			}

			@Override
			public InvocationResponse executeService(InvocationRequest request) throws ServiceInvocationException {
				return mExecutionInterface.executeService(target.getIdentifier(), request);
			}

		});
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
	 * @return a {@link InvocationStub} to perform invocations of remote
	 *         services.
	 */
	public InvocationStub createStub(final SPFPerson target, SPFServiceDescriptor descriptor) {
		return InvocationStub.from(descriptor, new InvocationStub.Target() {

			@Override
			public void prepareArguments(Object[] arguments) throws ServiceInvocationException {
				mExecutionInterface.injectActivities(target.getIdentifier(), arguments);
			}

			@Override
			public InvocationResponse executeService(InvocationRequest request) {
				return mExecutionInterface.executeService(target.getIdentifier(), request);
			}
		});
	}

	/**
	 * Dispatches an activity to the SPF instances matching the given
	 * identifier. The framework will perform information injection into the
	 * activity: such information will be available to the caller once the
	 * method ends.
	 * 
	 * @param target
	 *            - the target of the activity.
	 * @param activity
	 *            - the activity to dispatch.
	 * @return - true if the activity has been correctly consumed by the
	 *         recipient.
	 */
	public boolean sendActivity(String target, SPFActivity activity) {
		InvocationResponse resp = mExecutionInterface.sendActivity(target, activity);
		if(resp.isResult()){
			return GsonHelper.gson.fromJson(resp.getPayload(), Boolean.class);
		} else {
			return false;
		}
	}

	protected void recycle() {
		// TODO Auto-generated method stub
	}
}
