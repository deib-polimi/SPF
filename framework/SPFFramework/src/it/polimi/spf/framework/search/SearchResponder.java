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
package it.polimi.spf.framework.search;

import java.util.Map;

import org.json.JSONException;

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFProfileManager;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.ProfileFieldConverter;
import it.polimi.spf.shared.model.SPFQuery;
import android.content.Context;

/**
 * Class to respond to received queries
 * 
 * @author darioarchetti
 * 
 */
// XXX #SearchRefactor
// - Create unique query performer for search and trigger
// - Hide this class behind the search manager facade
public class SearchResponder {

	public SearchResponder(Context context) {

	}

	/**
	 * Verifies if the local profile matches the given query.
	 * 
	 * @see Query
	 * @param q
	 * @return
	 */
	public boolean matches(String queryJSON) {
		QueryContainer queryContainer;
		try {
			queryContainer = QueryContainer.fromJSON(queryJSON);
		} catch (JSONException e) {
			return false;
		}

		SPFQuery query = queryContainer.getQuery();
		String callerApp = queryContainer.getCallerAppId();
		String userUID = queryContainer.getUserUID();
		return analyzeWith(query, callerApp, userUID);
	}

	private boolean analyzeWith(SPFQuery query, String callerApp, String userUID) {
		SPFProfileManager mProfile = SPF.get().getProfileManager();
		SPFSecurityMonitor mSecMonitor = SPF.get().getSecurityMonitor();
		SPFPersona persona = mSecMonitor.getPersonaOf(callerApp);

		Map<String, String> fields = query.getProfileFields();
		ProfileField[] queryFields = ProfileField.fromIdentifierList(fields.keySet().toArray(new String[fields.keySet().size()]));
		ProfileFieldContainer pfc = mProfile.getProfileFieldBulk(persona, queryFields);
		for (ProfileField f : queryFields) {
			// FIXME refactor profile fields conversion vs. storage string
			String myValue = ProfileFieldConverter.forField(f).toStorageString(pfc.getFieldValue(f));
			String queryValue = fields.get(f.getIdentifier());
			if (!myValue.toLowerCase().contains(queryValue.toLowerCase())) {
				// FIXME contains() is a hack for collection
				return false;
			}
		}

		for (String tag : query.getTags()) {
			if (!mProfile.hasTag(tag, persona)) {
				return false;
			}
		}

		for (String app : query.getApps()) {
			if (!mSecMonitor.isAppRegistered(app)) {
				return false;
			}
		}
		return true;

	}

}
