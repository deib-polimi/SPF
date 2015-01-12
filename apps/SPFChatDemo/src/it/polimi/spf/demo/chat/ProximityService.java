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
package it.polimi.spf.demo.chat;

import it.polimi.spf.lib.services.ServiceInterface;
import it.polimi.spf.lib.services.ActivityConsumer;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * @author darioarchetti
 */
@ServiceInterface(
        app = "it.polimi.spf.demo.chat",
        name = "SPFChatDemo Proximity",
        description = "Service that allows users to communicate",
        version = "0.1",
        consumedVerbs = {ProximityService.POKE_VERB, ProximityService.MESSAGE_VERB}
)
public interface ProximityService {

    public static final String POKE_VERB = "nex2:poke";
    public static final String MESSAGE_VERB = "nex2:message";
    public static final String MESSAGE_TEXT = "nex2:text";

    @ActivityConsumer(verb = POKE_VERB)
    void onPokeReceived(SPFActivity poke);

    @ActivityConsumer(verb = MESSAGE_VERB)
    void onMessageReceived(SPFActivity message);
}
