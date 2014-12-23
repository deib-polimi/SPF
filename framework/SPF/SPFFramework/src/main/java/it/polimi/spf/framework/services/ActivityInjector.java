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
package it.polimi.spf.framework.services;

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFActivity;

public class ActivityInjector {

	public static void injectDataInActivity(SPFActivity activity, String targetId) {
		ProfileFieldContainer pfc = it.polimi.spf.framework.SPF.get().getProfileManager().getProfileFieldBulk(SPFPersona.DEFAULT, ProfileField.IDENTIFIER, ProfileField.DISPLAY_NAME);

		activity.put(SPFActivity.SENDER_DISPLAY_NAME, pfc.getFieldValue(ProfileField.DISPLAY_NAME));
		activity.put(SPFActivity.SENDER_IDENTIFIER, pfc.getFieldValue(ProfileField.IDENTIFIER));

		// TODO ActivityStreams inject information of the recipient
	}

	public static void injectDataInActivity(SPFActivity activity) {
		ProfileFieldContainer pfc = SPF.get().getProfileManager().getProfileFieldBulk(SPFPersona.DEFAULT, ProfileField.IDENTIFIER, ProfileField.DISPLAY_NAME);

		activity.put(SPFActivity.SENDER_DISPLAY_NAME, pfc.getFieldValue(ProfileField.DISPLAY_NAME));
		activity.put(SPFActivity.SENDER_IDENTIFIER, pfc.getFieldValue(ProfileField.IDENTIFIER));
		activity.put(SPFActivity.RECEIVER_DISPLAY_NAME, pfc.getFieldValue(ProfileField.DISPLAY_NAME));
		activity.put(SPFActivity.RECEIVER_IDENTIFIER, pfc.getFieldValue(ProfileField.IDENTIFIER));
	}

}
