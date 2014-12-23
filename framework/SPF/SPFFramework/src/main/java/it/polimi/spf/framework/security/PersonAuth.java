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

import java.util.ArrayList;
import java.util.List;

public class PersonAuth {

	private final String uuid;
	private final List<String> circles;

	/* package */PersonAuth(String uuid, List<String> circles) {
		this.circles = circles;
		this.uuid = uuid;
	}

	/**
	 * Create an empty PersonAuth with only public circle permission.
	 */
	/* package */PersonAuth(String uuid) {
		this.uuid = uuid;
		this.circles = new ArrayList<String>();
	}

	public String getUserIdentifier() {
		return uuid;
	}

	public boolean isAllowed(String circle) {
		return circles.contains(circle);
	}

	public List<String> getCircles() {
		return new ArrayList<String>(circles);
	}

	public static PersonAuth getPublicAuth() {
		List<String> circles = new ArrayList<String>();
		circles.add("public");
		return new PersonAuth(null, circles);
	}
}
