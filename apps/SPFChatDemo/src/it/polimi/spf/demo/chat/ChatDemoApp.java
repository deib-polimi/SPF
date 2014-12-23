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

import android.app.Application;

public class ChatDemoApp extends Application{

	private static ChatDemoApp instance;
	
	public static ChatDemoApp get(){
		return instance;
	}
	
	private ChatStorage mStorage;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		mStorage = new ChatStorage.ChatStorageImpl(this);
	}
	
	public ChatStorage getChatStorage(){
		return mStorage;
	}
}
