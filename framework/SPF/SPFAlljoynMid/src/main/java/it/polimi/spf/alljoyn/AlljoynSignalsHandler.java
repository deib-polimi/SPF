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
package it.polimi.spf.alljoyn;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

@BusInterface(name= "it.polimi.spf.alljoyn.SPFAlljoynSignalsInterface")
/*package*/ interface AlljoynSignalsHandler {
	
	@BusSignal
	void searchSignal(String sender,String queryId,String query) throws BusException;
	
	@BusSignal
	void searchResult(String queryId,String uniqueIdentifier,String baseInfo) throws BusException;

	@BusSignal
	void advertisingSignal(String profile,String uniqueIdentifier) throws BusException;
}
