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
package it.polimi.spf.framework.security;

import it.polimi.spf.shared.model.Permission;

public class PermissionHelper {

	public static Permission[] getPermissions(int code) {
		int numberOfPermissions = bitCount(code), index = 0;
		Permission[] permissions = new Permission[numberOfPermissions];
		for (Permission p : Permission.values()) {
			if ((p.getCode() & code) > 0) {
				permissions[index++] = p;
			}
		}
		return permissions;
	}

	/**
	 * Constant time, constant memort algorithm to count the number of 1's in a
	 * binary representation
	 * 
	 * @param - the number
	 * @see <a
	 *      href="http://blogs.msdn.com/b/jeuge/archive/2005/06/08/hakmem-bit-count.aspx">this
	 *      link on MSDN</a>
	 * @return the number of 1's in the binary representation
	 */
	private static int bitCount(int u) {
		int uCount;

		uCount = u - ((u >> 1) & 033333333333) - ((u >> 2) & 011111111111);
		return ((uCount + (uCount >> 3)) & 030707070707) % 63;
	}

}
