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

import android.content.Intent;
import android.util.Log;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.proximity.SPFRemoteInstance;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.SPFAction;
import it.polimi.spf.shared.model.SPFActionIntent;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFTrigger;

/*package*/ class SPFActionPerformerDelegate implements SPFActionPerformer {

	@Override
	public void perform(SPFAdvProfile target,SPFTrigger trigger) {
		SPFAction action=trigger.getAction();
		if(action instanceof SPFActionIntent){
			performIntentAction(target,trigger,(SPFActionIntent) action);
		} else if (action instanceof SPFActionSendNotification){
			performSendNotificationAction(target,(SPFActionSendNotification) action);
		}
	}

	private void performSendNotificationAction(SPFAdvProfile target,SPFActionSendNotification action) {
		Log.d("NOTIFICATION!!", action.getTitle() + ": " + action.getMessage());
		String targetId =target.getField(ProfileField.IDENTIFIER.getIdentifier());
		SPFRemoteInstance targetInstance = SPF.get().getPeopleManager().getPerson(targetId);
		if(targetInstance==null){
			return;
		}else{
			
			targetInstance.sendNotification(SPF.get().getUniqueIdentifier(),action);
		}
	}

	private void performIntentAction(SPFAdvProfile target, SPFTrigger trigger, SPFActionIntent action) {
		String actionString = action.getAction();
		Intent intent = new Intent(actionString);
		intent.putExtra(SPFActionIntent.ARG_STRING_TARGET, target.getField(ProfileField.IDENTIFIER.getIdentifier()));
		intent.putExtra(SPFActionIntent.ARG_STRING_DISPLAY_NAME,target.getField(ProfileField.DISPLAY_NAME.getIdentifier()));
		intent.putExtra(SPFActionIntent.ARG_STRING_TRIGGER_NAME, trigger.getName());
		intent.putExtra(SPFActionIntent.ARG_LONG_TRIGGER_ID, trigger.getId() );
		SPF.get().getContext().sendBroadcast(intent);
	}

}
