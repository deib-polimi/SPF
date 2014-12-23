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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Jacopo
 * 
 */
public final class DefaultCircles {

	public static final String PRIVATE = "private";
	public static final String PUBLIC = "public";
	public static final String ALL_CIRCLE = "all_circles";

	public static Comparator<String> COMPARATOR = new Comparator<String>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(String lhs, String rhs) {
			if(lhs.equals(rhs)){
				return 0;
			}
			
			if (lhs.equals(PUBLIC)) {
				return -1;
			}
			
			if (rhs.equals(PUBLIC)) {
				return 1;
			}
			
			if (lhs.equals(ALL_CIRCLE)) {
				return -1;
			}

			if (rhs.equals(ALL_CIRCLE)) {
				return 1;
			}

			return lhs.compareTo(rhs);
		}
	};

	private static final List<String> ALL = Arrays.asList(new String[]{PUBLIC, ALL_CIRCLE, PRIVATE });

	public static String[] getAll() {
		return ALL.toArray(new String[ALL.size()]);
	}
	
	public static List<String> getList() {
		return new ArrayList<String>(ALL);
	}

	public static boolean isDefault(String circle) {
		return ALL.contains(circle);
	}

	/**
	 * 
	 */
	private DefaultCircles() {
	}

}
