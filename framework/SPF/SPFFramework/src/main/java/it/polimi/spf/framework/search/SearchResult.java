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

import it.polimi.spf.shared.model.BaseInfo;

/**
 * 
 *
 */
public class SearchResult {

	private String mQueryId;
	private String mUniqueIdentifier;
	private BaseInfo mBaseInfo;

	public SearchResult(String searchId, String uniqueIdentifier, BaseInfo baseInfo) {
		this.mQueryId = searchId;
		this.mUniqueIdentifier = uniqueIdentifier;
		this.mBaseInfo = baseInfo;
	}

	public String getQueryId() {
		return mQueryId;
	}

	public String getUniqueIdentifier() {
		return mUniqueIdentifier;
	}

	public BaseInfo getBaseInfo() {
		return mBaseInfo;
	}

}
