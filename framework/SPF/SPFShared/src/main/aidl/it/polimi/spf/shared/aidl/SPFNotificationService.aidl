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
 
package it.polimi.spf.shared.aidl;

import it.polimi.spf.shared.model.SPFTrigger;
import it.polimi.spf.shared.model.SPFError;
import java.util.List;

/**
 * Interface exposed by SPF to allows local applications to interact with the
 * Notification API.
 * 
 * TODO #Documentation
 * 
 * @author darioarchetti
 * 
 */
interface SPFNotificationService{

  long saveTrigger(in SPFTrigger trigger,in String accessToken, out SPFError err);
  boolean deleteTrigger(in long triggerId, in String token, out SPFError err);
  boolean deleteAllTrigger( in String token, out SPFError err );
  List<SPFTrigger> listTrigger(in String token, out SPFError err);
  SPFTrigger getTrigger(in long triggerId,String token, out SPFError err);

}