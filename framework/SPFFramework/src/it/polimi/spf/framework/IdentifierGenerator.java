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
package it.polimi.spf.framework;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class IdentifierGenerator {

	private final static int ACCESS_TOKEN_LENGTH = 26;
	private Random mRandom = new SecureRandom();

	public String generateIdentifier(int length) {
		return new BigInteger(length * 5, mRandom).toString(32);
	}

	public String generateAccessToken() {
		return generateIdentifier(ACCESS_TOKEN_LENGTH);
	}
}
