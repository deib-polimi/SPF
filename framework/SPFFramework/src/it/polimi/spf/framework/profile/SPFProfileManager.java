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
package it.polimi.spf.framework.profile;

import it.polimi.spf.framework.security.PersonAuth;
import it.polimi.spf.framework.security.PersonRegistry;
import it.polimi.spf.shared.model.BaseInfo;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;

import java.util.List;

import android.app.DownloadManager.Query;
import android.content.Context;
import android.os.Bundle;

/**
 * 
 * Handles the storage of fields containing information about the user.
 * Different views on the profile are provided by selecting the appropriate
 * {@link SPFPersona}. For each profile field and for each {@link SPFPersona}
 * privacy levels can be configured by adding and removing circles. The list of
 * the available profile field can be found in {@link ProfileField} class. The
 * management of circles and their association with users is responsibility of
 * {@link PersonRegistry}.
 * 
 * @see SPFPersona
 * @see ProfileField
 * @author Jacopo Aliprandi
 */
public class SPFProfileManager {

	private ProfileTable mProfileTable;

	public SPFProfileManager(Context context) {
		mProfileTable = new ProfileTable(context);
	}

	/**
	 * Returns a {@link ProfileFieldContainer} filled with the values specified
	 * in the request.
	 * 
	 * @param persona
	 *            - not null
	 * @param fields
	 *            - an array of {@link ProfileField} to retrieve.
	 * @return a {@link ProfileFieldContainer}
	 */
	public ProfileFieldContainer getProfileFieldBulk(SPFPersona persona, ProfileField<?>... fields) {
		return mProfileTable.getProfileFieldBulk(persona, fields);
	}

	/**
	 * Method for proximity interface to provide access to local profile to
	 * remote spf instances
	 * 
	 * @param auth
	 *            - the {@link PersonAuth} of the sender of the request
	 * @param persona
	 * @param fields
	 *            - the identifiers of the list of fields to read
	 * @return a {@link ProfileFieldContainer} with the value of read fields, if
	 *         accessible.
	 */
	public ProfileFieldContainer getProfileFieldBulk(PersonAuth auth, SPFPersona persona, String[] fields) {
		return mProfileTable.getProfileFieldBulk(persona, fields, auth);
	}

	/**
	 * Returns a {@link ProfileFieldContainer} filled with the values specified
	 * in the request.
	 * 
	 * @param fields
	 *            - an array of {@link ProfileField} to retrieve.
	 * @param persona
	 *            - the {@link SPFPersona} to read, not null
	 * @return a {@link ProfileFieldContainer}
	 */
	public ProfileFieldContainer getProfileFieldBulk(String[] fields, SPFPersona persona) {
		return mProfileTable.getProfileFieldBulk(persona, fields);
	}

	/**
	 * Save the profile field container in the specified persona.
	 * 
	 * @param container
	 *            - the {@link ProfileFieldContainer} to save
	 * @param persona
	 *            - the associated {@link SPFPersona}
	 */
	public void setProfileFieldBulk(ProfileFieldContainer container, SPFPersona persona) {
		mProfileTable.setProfileFieldBulk(persona, container);
	}

	/**
	 * Returns the display_name of the user. If there is no display_name, the
	 * method returns its identifier.
	 * 
	 * @param persona
	 *            - the {@link SPFPersona} to read.
	 * @return the {@link BaseInfo} of the given persona
	 */
	public BaseInfo getBaseInfo(SPFPersona persona) {
		ProfileFieldContainer pfc = getProfileFieldBulk(persona, ProfileField.IDENTIFIER, ProfileField.DISPLAY_NAME);
		return new BaseInfo(pfc.getFieldValue(ProfileField.IDENTIFIER), pfc.getFieldValue(ProfileField.DISPLAY_NAME));
	}

	/**
	 * Returns the list of the available SPFPersonas.
	 * 
	 * @return a {@link List} of {@link SPFPersona}
	 */
	public List<SPFPersona> getAvailablePersonas() {
		return mProfileTable.getAvailablePersonas();
	}

	/**
	 * Delete a SPFPersona. All the information related to the specified persona
	 * will be erased.
	 * 
	 * @param persona
	 *            - the {@link SPFPersona} to remove
	 */
	public void removePersona(SPFPersona persona) {
		mProfileTable.removePersona(persona);
	}

	/**
	 * Creates a new SPFPersona.
	 * 
	 * @param persona
	 *            - the {@link SPFPersona} to add.
	 */
	public void addPersona(SPFPersona persona) {
		mProfileTable.addPersona(persona);
	}

	/**
	 * Returns a {@link Bundle} with profile fields identifiers as keys, and
	 * {@link ArrayList<String>} as circles.
	 * 
	 * @param persona
	 * @return a {@link Bundle}
	 */
	public Bundle getGroupsOf(SPFPersona persona) {
		return mProfileTable.getCirclesOf(persona);
	}
	
	/**
	 * Returns a {@link Bundle} with profile fields identifiers as keys, and
	 * {@link ArrayList<String>} as circles.
	 * 
	 * @param persona
	 * @return a {@link Bundle}
	 */
	@Deprecated
	public Bundle getCirclesOf(SPFPersona persona) {
		return mProfileTable.getCirclesOf(persona);
	}

	/**
	 * Adds a circle to a specified {@link ProfileField}.
	 * 
	 * @param field
	 *            - the {@link ProfileField} to modify
	 * @param circle
	 *            - the circle to add
	 * @param persona
	 *            - the {@link SPFPersona} to modify
	 * @return true if the operation was successful
	 */
	public boolean addGroupToField(ProfileField<?> field, String group, SPFPersona persona) {
		return mProfileTable.addCircleToFields(group, field, persona);
	}

	/**
	 * Remove a circle from a specified profile field.
	 * 
	 * @param field
	 *            the {@link ProfileField} to modify
	 * @param circle
	 *            - the circle to add
	 * @param persona
	 *            - the {@link SPFPersona} to modify
	 * @return true if the operation was successful
	 */
	public boolean removeGroupFromField(ProfileField<?> field, String group, SPFPersona persona) {
		return mProfileTable.removeCircleFromField(group, field, persona);
	}

	/**
	 * Adds a circle to a specified {@link ProfileField}.
	 * 
	 * @param field
	 *            - the {@link ProfileField} to modify
	 * @param circle
	 *            - the circle to add
	 * @param persona
	 *            - the {@link SPFPersona} to modify
	 * @return true if the operation was successful
	 */
	@Deprecated
	public boolean addCircleToField(ProfileField<?> field, String circle, SPFPersona persona) {
		return mProfileTable.addCircleToFields(circle, field, persona);
	}

	/**
	 * Remove a circle from a specified profile field.
	 * 
	 * @param field
	 *            the {@link ProfileField} to modify
	 * @param circle
	 *            - the circle to add
	 * @param persona
	 *            - the {@link SPFPersona} to modify
	 * @return true if the operation was successful
	 */
	@Deprecated
	public boolean removeCircleFromField(ProfileField<?> field, String circle, SPFPersona persona) {
		return mProfileTable.removeCircleFromField(circle, field, persona);
	}

	/**
	 * Check if the profile fields of a given SPFPersona contains the specified
	 * tag. Returns true when the tag is contained in at least one profile field
	 * value.
	 * 
	 * @param tag
	 *            - the tag to look for
	 * @param persona
	 *            - the {@link SPFPersona} to read
	 * @return true if the profile contains the tag
	 */
	public boolean hasTag(String tag, SPFPersona persona) {
		return mProfileTable.hasTag(tag, persona);
	}

	/**
	 * Matches a query against the information contained in the profile of the
	 * user
	 * 
	 * @param query
	 * @return
	 */
	public boolean match(Query query) {
		return false;
	}

}
