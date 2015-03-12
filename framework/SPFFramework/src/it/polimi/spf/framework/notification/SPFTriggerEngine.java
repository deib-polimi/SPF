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
package it.polimi.spf.framework.notification;

import it.polimi.spf.shared.model.SPFQuery;
import it.polimi.spf.shared.model.SPFTrigger;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.util.LongSparseArray;

/**
 * @author Jacopo Aliprandi
 * 
 *         Contains all the triggers' instances. Handle the 'event processing'.
 *         Calls action performer when a trigger has been fired.
 * 
 *         IMPORTANT it is not thread safe: to be called in the same
 *         handler/thread
 */
/*package*/ class SPFTriggerEngine {

	private LongSparseArray<SPFTrigger> triggers;
	private SPFActionPerformer actionPerformer;

	public SPFTriggerEngine(SPFActionPerformer performer) {
		this.triggers = new LongSparseArray<SPFTrigger>();
		this.actionPerformer = performer;

	}

	void remove(Long obj) {
		triggers.remove(obj);
	}

	void lookForMatchingTrigger(SPFAdvProfile profile) {
		for (int i = 0; i < triggers.size(); i++) {
			SPFTrigger trg = triggers.valueAt(i);
			boolean res = analize(trg.getQuery(), profile);
			if (res) {
				actionPerformer.perform(profile, trg);
			}
		}
	}

	private boolean analize(SPFQuery query, SPFAdvProfile profile) {
		// XXX #SearchRefactor
		// - Merge with search responder to create a unique query performer
		// - Fix collections: now i use contains(...) that works but is inefficient
		Map<String, String> fields = query.getProfileFields();
		for (String identifier : fields.keySet()) {
			String profileValue = profile.getField(identifier).toLowerCase(Locale.US);
			String queryValue = fields.get(identifier).toLowerCase(Locale.US);
			if (!profileValue.contains(queryValue)) {
				return false;
			}
		}

		for (String tag : query.getTags()) {
			if (!checkTag(profile, tag)) {
				return false;
			}
		}

		for (String app : query.getApps()) {
			if (!profile.getApplications().contains(app)) {
				return false;
			}
		}
		
		return true;
	}

	private boolean checkTag(SPFAdvProfile profile, String tag) {
		String _tag = tag.trim().toLowerCase(Locale.US);
		for (String value : profile.getFieldsValues()) {
			if (value.toLowerCase(Locale.US).contains(_tag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a new trigger. If exists a trigger with the same id of the one
	 * specified as parameter, it will be replaced.
	 * 
	 * @param trigger
	 *            the trigger to add
	 */
	public void put(SPFTrigger trigger) {
		triggers.put(trigger.getId(), trigger);
	}

	/**
	 * Update the current set of triggers with the given list.
	 * 
	 * @param triggers2
	 */
	public void refreshTriggers(List<SPFTrigger> triggers2) {
		triggers.clear();
		for (SPFTrigger trg : triggers2) {
			triggers.put(trg.getId(), trg);
		}
	}

}
