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

/**
 * Interface that provides the definition of the methods needed by triggers'
 * actions.
 * 
 * @see SPFAction
 * @see SPFActionNotification
 * @see SPFActionServiceCall
 * @see SPFActionIntent
 * @see SPFTrigger
 * 
 * @author Jacopo Aliprandi
 */
/*package*/ interface SPFActionPerformer {

	public void perform(SPFAdvProfile target, SPFTrigger trigger);

}
