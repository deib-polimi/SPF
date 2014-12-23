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
package it.polimi.spf.framework.people;

import it.polimi.spf.framework.proximity.SPFRemoteInstance;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import android.util.Log;

/*package*/ class ReferenceTable {

	private final static String TAG = "ReferenceTable";

	private Map<String, SPFRemoteInstance> references;

	public ReferenceTable() {
		references = new Hashtable<String, SPFRemoteInstance>();
	}

	public void addReference(String name, SPFRemoteInstance reference) {
		references.put(name, reference);
		Log.d(TAG, "Added reference " + name);
	}

	public void removeReference(String name) {
		references.remove(name);
		Log.d(TAG, "Removed reference " + name);
	}

	public SPFRemoteInstance getReference(String target) {
		return references.get(target);
	}

	public List<String> clear() {
		List<String> removedRefs = new ArrayList<String>(references.keySet());	
		references.clear();
		//TODO release resources
		//TODO fix synchronization: add status connected or disconnected
		return removedRefs;
	}
}
