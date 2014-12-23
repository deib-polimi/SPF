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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container for fields read from the profile that can be parcelled and sent over the network as a
 * string. Allows to retrieve all desired fields with a single load / network call.
 *
 * @author darioarchetti
 */
public class ProfileFieldContainer implements Parcelable {

    public static enum FieldStatus {
        ORIGINAL, UNACCESSIBLE, MODIFIED, DELETED
    }

    protected final Bundle mFields, mStatus;

    public ProfileFieldContainer() {
        mFields = new Bundle();
        mStatus = new Bundle();
    }

    protected ProfileFieldContainer(ProfileFieldContainer source) {
        mFields = source.mFields;
        mStatus = source.mStatus;
    }

    private ProfileFieldContainer(Parcel source) {
        mFields = source.readBundle();
        mStatus = source.readBundle();
    }

    /**
     * Retrieves the {@link FieldStatus} for the given field.
     *
     * @param field - the field
     *
     * @return the status for the given field.
     */
    public FieldStatus getStatus(ProfileField<?> field) {
        return getStatus(field.getIdentifier());
    }

    /**
     * Gets the value of a field from the container
     *
     * @param field - the field whose value to retrieve
     *
     * @return the value of the field, or null if the container doesn't have a value for such field
     */
    public <E> E getFieldValue(ProfileField<E> field) {
        if (field == null) {
            throw new NullPointerException();
        }

        String val = mFields.getString(field.getIdentifier());
        return val == null ? null : ProfileFieldConverter.forField(field).fromStorageString(val);
    }

    /**
     * Sets the value of a field in the container. After the first set of a field, calling {@link
     * ProfileFieldContainer#isModified(ProfileField))} with it as parameter will return true.
     *
     * @param field - the fiel whose value to set
     * @param value - the new value of the field.
     */
    public <E> void setFieldValue(ProfileField<E> field, E value) {
        if (field == null || value == null) {
            throw new NullPointerException();
        }

        if (getStatus(field) == FieldStatus.UNACCESSIBLE) {
            throw new IllegalStateException("Cannot write an unaccessible field");
        }

        String newVal = ProfileFieldConverter.forField(field).toStorageString(value);
        String currentVal = mFields.getString(field.getIdentifier());

        if (newVal.equals(currentVal)) {
            return;
        }

        if (getStatus(field) == FieldStatus.ORIGINAL) {
            setStatus(field.getIdentifier(), FieldStatus.MODIFIED);
        }

        mFields.putString(field.getIdentifier(), newVal);
    }

    /**
     * Returns whether at least one field in the container has been modified
     *
     * @return true if at least the value of one field in the container has been modified, false
     * otherwise
     */
    public boolean isModified() {
        for (String key : mStatus.keySet()) {
            FieldStatus status = getStatus(key);
            if (status == FieldStatus.DELETED || status == FieldStatus.MODIFIED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns wether the given field has been modified at least once.
     *
     * @param field
     *
     * @return true if the field has been modified at least once, false otherwise
     */
    public <E> boolean isModified(ProfileField<?> field) {
        return getStatus(field) == FieldStatus.MODIFIED;
    }

    /**
     * Clears the modified state of fields. After this call and before any call to {@link
     * ProfileFieldContainer#setFieldValue(ProfileField, Object)}, {@link
     * ProfileFieldContainer#isModified()} and {@link ProfileFieldContainer#isModified(ProfileField)}
     * return false.
     */
    public void clearModified() {
        for (String field : mStatus.keySet()) {
            switch (getStatus(field)) {
                case DELETED:
                    mStatus.remove(field);
                    break;
                case MODIFIED:
                    setStatus(field, FieldStatus.ORIGINAL);
                    break;
                default:
                    continue;
            }
        }
    }

    // Parcelable methods
    /*
     * (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(mFields);
        dest.writeBundle(mStatus);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{Fields: " + mFields.toString() + "; Status: " + mStatus.toString() + "}";
    }

    public static final Creator<ProfileFieldContainer> CREATOR = new Creator<ProfileFieldContainer>() {

        @Override
        public ProfileFieldContainer[] newArray(int size) {
            return new ProfileFieldContainer[size];
        }

        @Override
        public ProfileFieldContainer createFromParcel(Parcel source) {
            return new ProfileFieldContainer(source);
        }
    };

    protected FieldStatus getStatus(String fieldIdentifier) {
        String status = mStatus.getString(fieldIdentifier);
        return status == null ? FieldStatus.ORIGINAL : FieldStatus.valueOf(status);
    }

    protected void setStatus(String field, FieldStatus status) {
        mStatus.putString(field, status.name());
    }

    /**
     * Returns the string representation of the specified {@link ProfileField}. Use {@link
     * ProfileFieldConverter} to convert the String to the proper data type.
     *
     * @param fieldIdentifier
     *
     * @return - the value of the profile field, represented as a string
     */
    public String getFieldValue(String fieldIdentifier) {
        return mFields.getString(fieldIdentifier);

    }
}
