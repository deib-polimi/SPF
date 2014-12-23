/**
 * Implementation of the SPF Services API that allows:
 * <ul>
 * <li>Registration of services of local applications</li>
 * <li>Dispatching of incoming {@link it.polimi.spf.shared.model.InvocationRequest} and {@link it.polimi.spf.shared.model.SPFActivity} to local applications</li>
 * </ul>
 * 
 * Visible classes of this package are:
 * <ul>
 * <li>{@link it.polimi.spf.framework.services.SPFServiceRegistry}: Allows the registration of services and the dispatching of activities and invocation requests</li>
 * <li>{@link it.polimi.spf.framework.services.ActivityInjector }: Utility class to automatically inject information into activities</li>
 * <li>{@link it.polimi.spf.framework.services.VerbSupport }: Lists the available services for a given verb, together with the default one</li>
 * </ul>
 * 
 * TODO:
 *  - Move {@link it.polimi.spf.framework.services.ServiceIdentifier} to SPFShared and use it globally 
 */
package it.polimi.spf.framework.services;