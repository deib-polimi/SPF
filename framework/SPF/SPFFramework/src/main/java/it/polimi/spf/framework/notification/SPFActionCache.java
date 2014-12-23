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

import it.polimi.spf.shared.model.SPFTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/*package*/ class SPFActionCache {

	private static final String SEP = "..";
	SharedPreferences pref;

	public SPFActionCache(Context context) {
		pref = context.getSharedPreferences("trigger_cache", Context.MODE_PRIVATE);

	}

	public boolean triggerIsSleepingOnTarget(String identifier, long triggerId) {
		long wakeUpAt = pref.getLong(identifier + SEP + triggerId, -1);
		final long currentTimeMillis = System.currentTimeMillis();
		if (wakeUpAt <= currentTimeMillis) {
			pref.edit().remove(identifier + SEP + triggerId).commit();
			return false;// the trigger is active
		}
		return true;// the trigger is sleeping
	}

	public void add(String targetId, SPFTrigger trigger) {
		long triggerId = trigger.getId();
		long sleep = trigger.getSleepPeriod();
		SharedPreferences.Editor editor = pref.edit();
		long nextWakeUpTime = sleep + System.currentTimeMillis();
		if (nextWakeUpTime <= 0) {
			nextWakeUpTime = Long.MAX_VALUE;
		}
		editor.putLong(targetId + SEP + triggerId, nextWakeUpTime);
		editor.commit();
	}

	public void refresh(Iterable<SPFTrigger> triggers) {
		Map<String, ?> m = pref.getAll();
		long currenttime = System.currentTimeMillis();
		List<String> keyToRemove = new ArrayList<String>();
		for (String key : m.keySet()) {
			if ((m.get(key) instanceof Long) && (Long) m.get(key) > currenttime) {
				keyToRemove.add(key);
				continue;
			}
			if (!triggerExists(triggers, key)) {
				keyToRemove.add(key);
			}
		}
		removeKeys(keyToRemove);
	}

	/**
	 * @param triggers
	 * @param key
	 * @param triggerExist
	 * @return
	 */
	private boolean triggerExists(Iterable<SPFTrigger> triggers, String key) {
		for (SPFTrigger trg : triggers) {
			if (key.contains(Long.toString(trg.getId()) + SEP)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param keyToRemove
	 */
	private void removeKeys(List<String> keyToRemove) {
		if (!keyToRemove.isEmpty()) {
			SharedPreferences.Editor editor = pref.edit();
			for (String key : keyToRemove) {
				editor.remove(key);
			}
			editor.commit();
		}
	}

}
