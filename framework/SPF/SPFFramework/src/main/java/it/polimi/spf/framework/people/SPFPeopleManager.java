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

import java.util.List;

import it.polimi.spf.framework.proximity.SPFRemoteInstance;


/**
 * TODO implement resource management 
 *
 */
public class SPFPeopleManager {
	
	private ReferenceTable rt=new ReferenceTable();
	
	public SPFPeopleManager() {
		
	}

	public SPFRemoteInstance getPerson(String target) {
		
		return rt.getReference(target);
	}

	public void removePerson(String uniqueIdentifier) {
		rt.removeReference(uniqueIdentifier);		
	}

	public void newPerson(SPFRemoteInstance instance) {
		rt.addReference(instance.getUniqueIdentifier(), instance);	
		//SPF.get(SPFApp.get()).dispatchSearchResult(instance.getUniqueIdentifier());
   }

	public boolean hasPerson(String identifier) {
		return getPerson(identifier) != null;
	}

	public List<String> clear() {
		return rt.clear();
	}

}
