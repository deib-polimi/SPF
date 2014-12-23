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
 * TODO #Documentation
 * 
 * @author Jacopo Aliprandi
 */
public class SPFTrigger implements Parcelable {

	/*
	 * the identifier generated in SPF database
	 */
	private long id = -1;

	/*
	 * the name assigned to this SPFTrigger
	 */
	private String name = "";

	/*
	 * the query associated with the trigger
	 */
	private SPFQuery query;

	/*
	 * the action associated with this trigger
	 */
	private SPFAction action;

	/*
	 * specifies if the trigger is to be deleted after the first positive query
	 * match.
	 */
	private boolean isOneShot = true;

	/*
	 * specifies for how much the trigger is to be turned off after have been
	 * fired on a given target. this property does not affect the notification
	 * regarding different instances: e.g. entities with different identifiers.
	 * If the trigger isOneShot this property is ignored.
	 */
	private long sleepPeriod = 0;

	// not to parcel properties:

	/*
	 * Use this variable to set the status of the trigger: a trigger can be edit
	 * within beginedit and endEdit. endEdit validates the trigger.
	 * 
	 * NDR this solution avoids the overhead of a builder
	 */
	private boolean editing = false;

	/**
	 * Creates a new trigger.
	 * 
	 * @param name
	 *            - the name of the trigger
	 * @param query
	 *            - the query of the trigger
	 * @param action
	 *            - the action to be executed
	 * @param oneShot
	 *            - if the trigger is oneShot
	 * @param sleepPeriod
	 *            - the sleep period of <code>this</code> trigger
	 * 
	 * @throws IllegalTriggerException
	 *             - if the trigger is not valid
	 */
	public SPFTrigger(String name, SPFQuery query, SPFAction action, boolean oneShot, long sleepPeriod) throws IllegalTriggerException {
		this(-1, name, query, action, oneShot, sleepPeriod);
	}

	/**
	 * Creates a new trigger
	 * 
	 * @param id
	 *            - the id of the trigger
	 * @param name
	 *            - the name of the trigger
	 * @param query
	 *            - the query of the trigger
	 * @param action
	 *            - the action to be executed
	 * @param oneShot
	 *            - if the trigger is oneShot
	 * @param sleepPeriod
	 *            - the sleep period of <code>this</code> trigger
	 * 
	 * @throws IllegalTriggerException
	 *             - if the trigger is not valid
	 */
	public SPFTrigger(long id, String name, SPFQuery query, SPFAction action, boolean oneShot, long activePeriod) throws IllegalTriggerException {
		this.id = id;
		this.name = name;
		this.query = query;
		this.action = action;
		if (oneShot) {
			this.isOneShot = oneShot;
			sleepPeriod = Long.MAX_VALUE;
		} else {
			this.isOneShot = false;
			this.sleepPeriod = activePeriod;
		}
	}

	/**
	 * The identifier of this trigger, it is set to -1 if it is volatile (not
	 * saved).
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the id of the trigger. It is the primary key to use when accessing
	 * the triggers' table.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Enters the editable state.
	 */
	public void beginEdit() {
		this.editing = true;
	}

	/**
	 * Exits from the editable state and validates the content.
	 * 
	 * @throws IllegalTriggerException
	 */
	public void endEdit() throws IllegalTriggerException {
		this.editing = false;
		validate();
	}

	private void validate() throws IllegalTriggerException {
		// TODO implement validation on query, action and other parameters
	}

	/*
	 * check if the instance is in the 'editable' state. If it is not, throws a
	 * runtime exception: TriggerNotEditableException
	 */
	private void checkIfEditable() {
		if (!this.editing) {
			throw new TriggerNotEditableException("Attempt to modify a trigger not in editable state");
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		checkIfEditable();
		this.name = name;
	}

	/**
	 * @return the isOneShot
	 */
	public boolean isOneShot() {
		return isOneShot;
	}

	/**
	 * @param isOneShot
	 *            the isOneShot to set
	 */
	public void setOneShot(boolean isOneShot) {
		checkIfEditable();
		this.isOneShot = isOneShot;
		if (this.isOneShot) {
			this.sleepPeriod = Long.MAX_VALUE;
		}
	}

	/**
	 * Returns the sleepPeriod of this trigger.
	 * 
	 * @return the sleepPeriod
	 */
	public long getSleepPeriod() {
		return sleepPeriod;
	}

	/**
	 * @param sleepPeriod
	 *            the sleepPeriod to set
	 */
	public void setSleepPeriod(long sleepPeriod) {
		checkIfEditable();
		this.sleepPeriod = sleepPeriod;
		this.isOneShot = false;
	}

	/**
	 * @return the action
	 */
	public SPFAction getAction() {
		return action;
	}

	/**
	 * @param sPFAction
	 *            the action to set
	 */
	public void setAction(SPFAction sPFAction) {
		checkIfEditable();
		this.action = sPFAction;
	}

	/**
	 * @return the query
	 */
	public SPFQuery getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(SPFQuery query) {
		checkIfEditable();
		this.query = query;
	}

	/**
	 * Exception thrown when the trigger state is inconsistent.
	 * 
	 * @author Jacopo Aliprandi
	 */
	public class IllegalTriggerException extends Exception {

		private static final long serialVersionUID = 1L;

	}

	/**
	 * Exception thrown when there is an attempt to modify the trigger while it
	 * is not in the editable state.
	 * 
	 * @author Jacopo Aliprandi
	 */
	public class TriggerNotEditableException extends RuntimeException {

		public TriggerNotEditableException(String message) {
			super(message);
		}

		private static final long serialVersionUID = 1L;

	}

	/*
	 * Parcelable interface methods
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeParcelable(query, flags);
		dest.writeParcelable(action, flags);
		dest.writeLong(id);
		dest.writeInt(isOneShot ? 1 : 0);
		dest.writeLong(sleepPeriod);
		dest.writeString(name);
	}

	private SPFTrigger(Parcel source) {
		this.query = source.readParcelable(((Object) this).getClass().getClassLoader());
		this.action = source.readParcelable(((Object) this).getClass().getClassLoader());
		this.id = source.readLong();
		this.isOneShot = (source.readInt() == 1) ? true : false;
		this.sleepPeriod = source.readLong();
		this.name = source.readString();
	}

	public final static Creator<SPFTrigger> CREATOR = new Creator<SPFTrigger>() {

		@Override
		public SPFTrigger[] newArray(int size) {
			return new SPFTrigger[size];
		}

		@Override
		public SPFTrigger createFromParcel(Parcel source) {
			return new SPFTrigger(source);
		}
	};

}
