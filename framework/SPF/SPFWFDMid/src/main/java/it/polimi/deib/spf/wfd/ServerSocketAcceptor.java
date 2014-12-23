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

import it.polimi.deib.spf.wfd.GOInternalClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketAcceptor extends Thread {
	public static final String TAG  = "ServerSocketAcceptor";
	private GroupOwnerActor groupOwner;
	private ServerSocket serverSocket;
	boolean closed;

	public ServerSocketAcceptor(GroupOwnerActor groupOwner,
			ServerSocket serverSocket) {
		this.groupOwner = groupOwner;
		this.serverSocket = serverSocket;
	}

	public void recycle() {
		this.closed = true;
		interrupt();
	}

	@Override
	public void run() {
		Socket s;
		try {
			while (!Thread.currentThread().isInterrupted()) {
				WfdLog.d(TAG, "accept(): waiting for a new client");
				s = serverSocket.accept();
				WfdLog.d(TAG, "incoming connection");
				new GOInternalClient(s,groupOwner).start();
			}
		} catch (IOException e) {

		}
		WfdLog.d(TAG, "exiting while loop");
		if (!closed) {
			WfdLog.d(TAG, "signalling error to groupOwnerActor");
			groupOwner.onServerSocketError();
		}
	}

}
