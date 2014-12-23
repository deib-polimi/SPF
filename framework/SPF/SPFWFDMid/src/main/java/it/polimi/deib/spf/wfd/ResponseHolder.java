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
package it.polimi.deib.spf.wfd;

public class ResponseHolder {

	private final long REQ_TIMEOUT;
	private WfdMessage msg;
	private boolean threadWaiting = false;
	private long requestSequenceId = 0;

	public ResponseHolder(long timeout) {
		this.REQ_TIMEOUT = timeout;
	}
	
	public synchronized void set(WfdMessage msg) {
		long sequenceNum = msg.getTimestamp();

		if (threadWaiting && sequenceNum == requestSequenceId) {
			this.msg = msg;
			notify();
		}
	}

	public synchronized WfdMessage get() throws InterruptedException {
		if (msg == null) {
			threadWaiting = true;
			wait(REQ_TIMEOUT);
			WfdMessage _tmpMsg = msg;
			msg = null;
			threadWaiting = false;
			return _tmpMsg;
		}
		return null;
	}

	public long assignRequestSequenceId() {
		return ++requestSequenceId;
	}
}