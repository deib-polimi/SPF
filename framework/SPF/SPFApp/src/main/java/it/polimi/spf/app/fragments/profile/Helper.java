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
package it.polimi.spf.app.fragments.profile;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import it.polimi.spf.app.R;
import it.polimi.spf.framework.security.DefaultCircles;
import it.polimi.spf.shared.model.ProfileField;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;

/**
 * Helper class to retrieve friendly name of fields and circles from resources
 * and to convert the value of {@link ProfileField} to strings that can be
 * displayed in views
 * 
 * @author darioarchetti
 * 
 */
public class Helper {

	public class InvalidValueException extends Exception {

		private static final long serialVersionUID = 4448039112806219926L;

	}

	private static final String TAG = "Helper";
	private static final String PROFILE_FIELD_PREFIX = "profile_field_";
	private static final String CIRCLE_PREFIX = "circle_";
	private static final SparseArray<String> sCache = new SparseArray<String>();

	private Converter<String> mStringConverter = new StringConverter();
	private Converter<Date> mDateConverter = new DateConverter();
	private Converter<String[]> mStringArrayConverter = new StringArrayConverter();

	private Context mContext;

	public Helper(Context context) {
		this.mContext = context;
	}

	/**
	 * Provides the localized friendly name of a field.
	 * @param field - the field whose name to retrieve
	 * @return the friendly name of the field
	 */
	public String getFriendlyNameOfField(ProfileField<?> field) {
		String name = getStringFromResource(PROFILE_FIELD_PREFIX + field.getIdentifier());
		if(name == null){
			return field.getIdentifier();
		}
		
		return name;
	}
	
	private String getStringFromResource(String resourceName){
		int id;
		
		try {
			id = (Integer) R.string.class.getField(resourceName).get(null);
		} catch (IllegalAccessException e) {
			Log.w(TAG, "Cannot retrieve string id for " + resourceName, e);
			return null;
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "Cannot retrieve string id for " + resourceName, e);
			return null;
		} catch (NoSuchFieldException e) {
			Log.w(TAG, "Cannot retrieve string id for " + resourceName, e);
			return null;
		}
		
		String val = sCache.get(id);

		if (val == null) {
			val = mContext.getResources().getString(id);
			sCache.put(id, val);
		}

		return val;
	}

	public String convertToFriendlyString(ProfileField<?> field, Object value) {
		Class<?> fieldClass = field.getFieldClass();
		if (fieldClass == String.class) {
			return mStringConverter.toString((String) value);
		} else if (fieldClass == Date.class) {
			return mDateConverter.toString((Date) value);
		} else if (fieldClass == String[].class) {
			return mStringArrayConverter.toString((String[]) value);
		} else {
			throw new IllegalArgumentException("Type " + fieldClass.getSimpleName() + " not supported.");
		}
	}

	@SuppressWarnings("unchecked")
	public <E> E convertFromFriendlyString(ProfileField<E> field, String value) throws InvalidValueException {
		Class<?> fieldClass = field.getFieldClass();
		if (fieldClass == String.class) {
			return (E) mStringConverter.fromString(value);
		} else if (fieldClass == Date.class) {
			return (E) mDateConverter.fromString(value);
		} else if (fieldClass == String[].class) {
			return (E) mStringArrayConverter.fromString(value);
		} else {
			throw new IllegalArgumentException("Type " + fieldClass.getSimpleName() + " not supported.");
		}
	}

	public String getFriendlyNameOfCircle(String circle) {
		if (!DefaultCircles.isDefault(circle)) {
			return circle;
		}

		String name = getStringFromResource(CIRCLE_PREFIX + circle);
		if(name == null){
			return circle;
		}
		
		return name;
	}

	public String sumUpCircles(List<String> circles) {
		if (circles == null || circles.isEmpty()) {
			return getFriendlyNameOfCircle(DefaultCircles.PRIVATE);
		}

		if (circles.contains(DefaultCircles.PUBLIC)) {
			return getFriendlyNameOfCircle(DefaultCircles.PUBLIC);
		}

		if (circles.contains(DefaultCircles.ALL_CIRCLE)) {
			return getFriendlyNameOfCircle(DefaultCircles.ALL_CIRCLE);
		}

		return circles.size() + " circles";
	}

	private interface Converter<E> {
		E fromString(String value) throws InvalidValueException;

		String toString(E value);
	}

	private class StringConverter implements Converter<String> {

		@Override
		public String fromString(String value) {
			return value;
		}

		@Override
		public String toString(String value) {
			return value;
		}

	}

	private class DateConverter implements Converter<Date> {

		@Override
		public Date fromString(String value) throws InvalidValueException {
			try {
				return value == null ? null : DateFormat.getDateFormat(mContext).parse(value);
			} catch (ParseException e) {
				throw new InvalidValueException();
			}
		}

		@Override
		public String toString(Date value) {
			return value == null ? null : DateFormat.getDateFormat(mContext).format(value);
		}

	}

	public class StringArrayConverter implements Converter<String[]> {

		@Override
		public String[] fromString(String value) throws InvalidValueException {
			return value.split("\n");
		}

		@Override
		public String toString(String[] value) {
			if (value == null) {
				return null;
			}

			int i = 0;
			StringBuilder b = new StringBuilder();

			while (i < value.length) {
				b.append(value[i]);
				i++;

				if (i < value.length) {
					b.append("\n");
				}
			}

			return b.toString();
		}

	}

}
