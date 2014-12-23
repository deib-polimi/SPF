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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import android.os.Handler;
import android.os.Looper;

public class GroupClientActor extends GroupActor {

	private static final String TAG = "GroupClientActor";
	private InetAddress groupOwnerAddress;
	private int destPort;
	private Socket socket;
	private boolean closed = false;

	public GroupClientActor(InetAddress groupOwnerAddress, int destPort,
			GroupActorListener listener, String myIdentifier) {
		super(listener,myIdentifier);
		this.groupOwnerAddress = groupOwnerAddress;
		this.destPort = destPort;
	}

	public void connect() {

		t.start();
	}

	public void disconnect() {
		try {
			WfdLog.d(TAG, "Disconnect called");
			t.interrupt();
			socket.close();
		} catch (IOException e) {
			WfdLog.d(TAG, "error on closing socket", e);
		}
	}
	
	Thread t = new Thread() {
		@Override
		public void run() {
			WfdInputStream inStream;
			try {
				WfdLog.d(TAG, "Opening socket connection");
				socket = new Socket();
				SocketAddress remoteAddr = new InetSocketAddress(
						groupOwnerAddress, destPort);
				socket.connect(remoteAddr, 1000);
				inStream = new WfdInputStream(socket.getInputStream());
				establishConnection();
				WfdLog.d(TAG, "Entering read loop");
				while (!isInterrupted()) {
					WfdMessage msg = inStream.readMessage();
					WfdLog.d(TAG, "message received");
					GroupClientActor.super.handle(msg);
				}
			} catch (Throwable e) {
				WfdLog.d(TAG, "error in the run loop", e);
			} finally {
				closeSocket();
			}
			if (!closed) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						GroupClientActor.super.onError();
					}
				});
			}
		}
	};

	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

	private void establishConnection() throws IOException {
		WfdMessage msg = new WfdMessage();
		msg.setType(WfdMessage.TYPE_CONNECT);
		msg.setSenderId(getIdentifier());
		WfdLog.d(TAG, "Sending connection message... ");
		sendMessage(msg);
	}

	@Override
	public void sendMessage(WfdMessage msg) throws IOException {
		WfdLog.d(TAG, "Sending message");
		WfdOutputStream outstream = new WfdOutputStream(
				socket.getOutputStream());
		outstream.writeMessage(msg);
	}



}
