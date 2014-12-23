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
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.Date;

/**
 * A class that allows the conversion of the value of a {@link ProfileField}
 * from/to a String that can be persisted in a database.
 * 
 * @author darioarchetti
 * 
 * @param <E>
 *            - the type of the {@link ProfileField}
 */
public abstract class ProfileFieldConverter<E extends Object> {

	private static final ProfileFieldConverter<String> STRING_CONVERTER = new StringConverter();
	private static final ProfileFieldConverter<Date> DATE_CONVERTER = new DateConverter();
	private static final ProfileFieldConverter<String[]> STRING_ARRAY_CONVERTER = new ArrayConverter<String>(String.class, STRING_CONVERTER);
	private static final ProfileFieldConverter<Bitmap> BITMAP_CONVERTER = new BitmapConverter();
	private static final String ARRAY_SEPARATOR = ";";

	/**
	 * Returns a {@link ProfileFieldConverter} that can handle the value of a
	 * given {@link ProfileField}
	 * 
	 * @param field
	 *            - the field whose values should be handled
	 * @return - the {@link ProfileFieldConverter} that can handle those values
	 */
	@SuppressWarnings("unchecked")
	public static <E> ProfileFieldConverter<E> forField(ProfileField<E> field) {
		Class<?> fieldClass = field.getFieldClass();

		if (fieldClass == String.class) {
			return (ProfileFieldConverter<E>) STRING_CONVERTER;
		} else if (fieldClass == Date.class) {
			return (ProfileFieldConverter<E>) DATE_CONVERTER;
		} else if (fieldClass == String[].class) {
			return (ProfileFieldConverter<E>) STRING_ARRAY_CONVERTER;
		} else if (fieldClass == Bitmap.class) {
			return (ProfileFieldConverter<E>) BITMAP_CONVERTER;
		}

		throw new IllegalArgumentException("Field type " + fieldClass.getSimpleName() + " not supported");
	}

	// public methods
	/**
	 * Converts the value of the {@link ProfileField} to a String
	 * 
	 * @param value
	 *            - the value of the {@link ProfileField}
	 * @return the String representation of the given value
	 */
	public abstract String toStorageString(E value);

	/**
	 * Converts the String representation of the value of a {@link ProfileField}
	 * to the original value.
	 * 
	 * @param storageString
	 *            - the String representation
	 * @return the original value of the {@link ProfileField}
	 */
	public abstract E fromStorageString(String storageString);

	// Implementations
	private static class StringConverter extends ProfileFieldConverter<String> {

		@Override
		public String toStorageString(String value) {
			return value;
		}

		@Override
		public String fromStorageString(String storageString) {
			return storageString;
		}
	}

	private static class DateConverter extends ProfileFieldConverter<Date> {

		@Override
		public String toStorageString(Date value) {
			return String.valueOf(value.getTime());
		}

		@Override
		public Date fromStorageString(String storageString) {
			return new Date(Long.valueOf(storageString));
		}
	}

	private static class ArrayConverter<E> extends ProfileFieldConverter<E[]> {

		private Class<E> mComponentType;
		private ProfileFieldConverter<E> mBaseConverter;

		public ArrayConverter(Class<E> componentType, ProfileFieldConverter<E> baseConverter) {
			this.mComponentType = componentType;
			this.mBaseConverter = baseConverter;
		}

		@Override
		public String toStorageString(E[] value) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < value.length; i++) {
				b.append(mBaseConverter.toStorageString(value[i]));
				if ((i + 1) < value.length) {
					b.append(ARRAY_SEPARATOR);
				}
			}
			return b.toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public E[] fromStorageString(String storageString) {
			String[] values = storageString.split(ARRAY_SEPARATOR);
			Object array = Array.newInstance(mComponentType, values.length);

			for (int i = 0; i < values.length; i++) {
				Array.set(array, i, mBaseConverter.fromStorageString(values[i]));
			}

			return (E[]) array;
		}

	}

	private static class BitmapConverter extends ProfileFieldConverter<Bitmap> {

		@Override
		public String toStorageString(Bitmap value) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			value.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		}

		@Override
		public Bitmap fromStorageString(String storageString) {
			byte[] ba = Base64.decode(storageString.getBytes(), Base64.DEFAULT);
			Bitmap photo = BitmapFactory.decodeByteArray(ba, 0, ba.length);
			return photo;
		}
	}
}
