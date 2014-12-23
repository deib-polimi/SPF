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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describe the configuration of a search operation. Its use is intended to ease the communication
 * beetween the application and the spf framework. It defines the query, the duration of the search
 * and the frequency of the search signal.
 */
/*
 * TODO Use it directly in the SPFLib ...different from QueryInfo which is an
 * internal data structure! (To favor decoupling )
 */
public final class SPFSearchDescriptor implements Parcelable {

    private long mIntervalBtwSignals;
    private int mNumberOfSignals;
    private SPFQuery mQuery;

    /**
     * @param intervalBtwSignals
     * @param numberOfSignals
     * @param query
     */
    public SPFSearchDescriptor(long intervalBtwSignals, int numberOfSignals, SPFQuery query) {
        super();
        this.mIntervalBtwSignals = intervalBtwSignals;
        this.mNumberOfSignals = numberOfSignals;
        this.mQuery = query;
    }

    /**
     * @return the query string
     */
    public SPFQuery getQuery() {
        return mQuery;
    }

    /**
     * @return the intervalBtwSignals
     */
    public long getIntervalBtwSignals() {
        return mIntervalBtwSignals;
    }

    /**
     * @return the numberOfSignals
     */
    public int getNumberOfSignals() {
        return mNumberOfSignals;
    }

	/*
     * Set of methods an constructor required to implement the Parcelable
	 * interface and the aidl.
	 */

    private SPFSearchDescriptor(Parcel source) {
        this.mIntervalBtwSignals = source.readLong();
        this.mNumberOfSignals = source.readInt();
        this.mQuery = source.readParcelable(((Object) this).getClass().getClassLoader());
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mIntervalBtwSignals);
        dest.writeInt(mNumberOfSignals);
        dest.writeParcelable(mQuery, 0);

    }

    public final static Creator<SPFSearchDescriptor> CREATOR = new Creator<SPFSearchDescriptor>() {

        @Override
        public SPFSearchDescriptor[] newArray(int size) {

            return new SPFSearchDescriptor[size];
        }

        @Override
        public SPFSearchDescriptor createFromParcel(Parcel source) {
            return new SPFSearchDescriptor(source);
        }
    };

}
