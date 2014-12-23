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
package it.polimi.spf.framework.proximity;

// Dummy implementation of middleware loaded when SPF is run on a x86 platform
public class DummyMiddleware implements ProximityMiddleware {

	private boolean isAdvertising = false;
	private boolean isConnected = false;

	@Override
	public void connect() {
		this.isConnected = true;
	}

	@Override
	public void disconnect() {
		this.isConnected = false;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void sendSearchResult(String id, String uniqueIdentifier, String baseInfo) {

	}

	@Override
	public void sendSearchSignal(String sender, String searchId, String query) {

	}

	@Override
	public void registerAdvertisement(String e, long sendPeriod) {
		this.isAdvertising = true;
	}

	@Override
	public void unregisterAdvertisement() {
		this.isAdvertising = false;
	}

	@Override
	public boolean isAdvertising() {
		return isAdvertising;
	}

}
