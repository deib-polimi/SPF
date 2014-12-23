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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WfdInputStream {
	BufferedReader reader;

	public WfdInputStream(InputStream inputStream) {
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	public WfdMessage readMessage() throws IOException {
		String str = reader.readLine();
		return WfdMessage.fromString(str);
	}

	public WfdMessage readMessage(long l) throws InterruptedException {
		TimedRead tr = new TimedRead();
		tr.start();
		String str = tr.readResult(l);
		if (str != null) {
			return WfdMessage.fromString(str);
		}
		return null;
	}

	class TimedRead extends Thread {
		
		String str = null;

		@Override
		public void run() {
			try {
				str = WfdInputStream.this.reader.readLine();

			} catch (IOException e) {

			} finally {
				synchronized (this) {
					notify();
				}
			}
		}

		String readResult(long millis) throws InterruptedException {
			synchronized (this) {
				if (str == null) {
					wait(millis);
				}
				return str;
			}
		}
	}

}
