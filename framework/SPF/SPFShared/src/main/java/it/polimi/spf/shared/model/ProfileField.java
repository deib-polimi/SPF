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
package it.polimi.spf.shared.model;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Instances of this class represents the type of fields supported by the local
 * profile, and are used to specify which field to write/read in components to
 * access profile. Instances are available as static methods of this class. The
 * list of available fields, with their type, are
 * <ul>
 * <li>IDENTIFIER:string</li>
 * <li>DISPLAY_NAME:string</li>
 * <li>PHOTO:bitmap</li>
 * <li>BIRTHDAY:Date</li>
 * <li>ABOUT_ME:string</li>
 * <li>EMAILS:string[]</li>
 * <li>LOCATION:string</li>
 * <li>STATUS:string</li>
 * <li>GENDER:string</li>
 * <li>INTERESTS:string[]</li>
 * </ul>
 */
public class ProfileField<E> {

	private static List<ProfileField<?>> sFields = new ArrayList<ProfileField<?>>();

	// Available fields
	public static ProfileField<String> IDENTIFIER = new ProfileField<String>("identifier", String.class);
	public static ProfileField<String> DISPLAY_NAME = new ProfileField<String>("display_name", String.class);
	public static ProfileField<Bitmap> PHOTO = new ProfileField<Bitmap>("photo", Bitmap.class);
	public static ProfileField<Date> BIRTHDAY = new DateProfileField("birthday");
	public static ProfileField<String> ABOUT_ME = new ProfileField<String>("about_me", String.class);
	public static ProfileField<String[]> EMAILS = new ProfileField<String[]>("emails", String[].class);
	public static ProfileField<String> LOCATION = new ProfileField<String>("location", String.class);
	public static ProfileField<String> STATUS = new ProfileField<String>("status", String.class);
	public static ProfileField<String> GENDER = new MultipleChoicheProfileField<String>("gender", String.class, new String[] { "male", "female", "other" });
	public static ProfileField<String[]> INTERESTS = new TagProfileField("interests");

	public static List<ProfileField<?>> getDefaultFields() {
		return new ArrayList<ProfileField<?>>(sFields);
	}

	/**
	 * Converts an array of {@link ProfileField} to an array of String,
	 * containing the identifier of the original fields, in the same order.
	 * 
	 * @param fields
	 *            - the array to convert
	 * @return the array of identifiers
	 */
	public static String[] toIdentifierList(ProfileField<?>[] fields) {
		if (fields == null) {
			return null;
		}

		String[] identifiers = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			identifiers[i] = fields[i].getIdentifier();
		}

		return identifiers;
	}

	/**
	 * Converts an array of profile field identifier to an array of
	 * {@link ProfileField}
	 * 
	 * @param identifiers
	 *            the array of identifier
	 * @return the converted array of {@link ProfileField}
	 */
	public static ProfileField<?>[] fromIdentifierList(String[] identifiers) {
		if (identifiers == null) {
			return null;
		}

		ProfileField<?>[] fields = new ProfileField<?>[identifiers.length];
		for (int i = 0; i < identifiers.length; i++) {
			fields[i] = lookup(identifiers[i]);

			if (fields[i] == null) {
				throw new IllegalArgumentException("Unknown field " + identifiers[i]);
			}
		}

		return fields;
	}

	/**
	 * Lookups the {@link ProfileField} identified by the given identifier
	 * 
	 * @param identifier
	 *            - the identifier of the field to lookup
	 * @return the {@link ProfileField} identified by the given identifier, or
	 *         null if none matches
	 */
	public static ProfileField<?> lookup(String identifier) {
		for (ProfileField<?> field : sFields) {
			if (field.getIdentifier().equals(identifier)) {
				return field;
			}
		}

		return null;
	}

	private String mIdentifier;
	private Class<E> mClass;

	private ProfileField(String identifier, Class<E> fieldClass) {
		this.mIdentifier = identifier;
		this.mClass = fieldClass;
		sFields.add(this);
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return mIdentifier;
	}

	/**
	 * @return the {@link Class} of the field
	 */
	public Class<E> getFieldClass() {
		return mClass;
	}

	@Override
	public String toString() {
		return "[ProfileField: " + mIdentifier + "]";
	}

	/**
	 * A profile field whose value can be chosen among a set of possible values
	 * 
	 * @author darioarchetti
	 * 
	 * @param <E>
	 *            - the type of the field
	 */
	public static class MultipleChoicheProfileField<E> extends ProfileField<E> {

		private final E[] mChoiches;

		public MultipleChoicheProfileField(String identifier, Class<E> fieldClass, E[] choiches) {
			super(identifier, fieldClass);
			this.mChoiches = choiches;
		}

		/**
		 * @return the possible values of this field
		 */
		public E[] getChoiches() {
			return mChoiches;
		}
	}

	/**
	 * A field whoase value is an unordered list of String
	 * 
	 * @author darioarchetti
	 * 
	 */
	public static class TagProfileField extends ProfileField<String[]> {
		public TagProfileField(String identifier) {
			super(identifier, String[].class);
		}
	}

	/**
	 * A profile field of {@link Date} type.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public static class DateProfileField extends ProfileField<Date> {
		public DateProfileField(String identifier) {
			super(identifier, Date.class);
		}
	}
}