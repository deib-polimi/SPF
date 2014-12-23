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
import java.io.OutputStream;
import java.io.PrintWriter;

public class WfdOutputStream {
	
	private PrintWriter writer;
	
	public WfdOutputStream(OutputStream outputStream) {
		this.writer = new PrintWriter(outputStream);
		
	}

	public void writeMessage(WfdMessage msg) throws IOException {
		String str = msg.toString();
		writer.println(str);
		writer.flush();
		if(writer.checkError()){
			throw new IOException();
		}
		
	}

}
