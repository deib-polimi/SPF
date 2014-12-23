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

import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * TODO this class should be moved somewhere else as it's only used by middlewares
 * @author darioarchetti
 *
 */
public class InvocationMarshaller {

	public static JsonElement toJsonElement(InvocationRequest invocationRequest) {
		return new Gson().toJsonTree(invocationRequest);
	}

	public static InvocationRequest requestfromJsonElement(JsonElement invocationRequest) {
		return new Gson().fromJson(invocationRequest, InvocationRequest.class);
	}

	public static String toJson(InvocationRequest request) {
		return new Gson().toJson(request);
	}

	public static InvocationRequest requestFromJson(String request) {
		return new Gson().fromJson(request, InvocationRequest.class);
	}

	public static JsonElement toJsonElement(InvocationResponse invocationResponse) {
		return new Gson().toJsonTree(invocationResponse);
	}

	public static InvocationResponse responsefromJsonElement(JsonElement invocationResponse) {
		return new Gson().fromJson(invocationResponse, InvocationResponse.class);
	}

	public static String toJson(InvocationResponse response) {
		return new Gson().toJson(response);
	}

	public static InvocationResponse responsefromJson(String response) {
		return new Gson().fromJson(response, InvocationResponse.class);
	}

	public static JsonElement toJsonElement(SPFActivity activity) {
		return new Gson().toJsonTree(activity);
	}

	public static SPFActivity activityFromJsonElement(JsonElement activity) {
		return new Gson().fromJson(activity, SPFActivity.class);
	}

	public static String toJson(SPFActivity activity) {
		return new Gson().toJson(activity);
	}

	public static SPFActivity activityFromJson(String json) {
		return new Gson().fromJson(json, SPFActivity.class);
	}
}
