package com.apayen.ft_hangouts;

import androidx.annotation.NonNull;

public class SMSModel
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	private final int id;
	private final int contactID;
	private final String type;
	private final String message;
	private final long date;
	//////////////////////////////
	// Constructors
	//////////////////////////////
	public SMSModel(int id, int contactID, String type, String message, long date)
	{
		this.id = id;
		this.contactID = contactID;
		this.type = type;
		this.message = message;
		this.date = date;
	}
	//////////////////////////////
	// Getters
	//////////////////////////////
	public int getID() { return (id); }
	public int getContactID() { return (contactID); }
	public long getDate() { return (date); }
	public String getMessage() { return (message); }
	public String getType() { return (type); }
	//////////////////////////////
	// To String
	//////////////////////////////
	@NonNull
	@Override
	public String toString() {
		return "SMSModel{" +
				"id=" + id +
				", contactID=" + contactID +
				", type='" + type + '\'' +
				", message='" + message + '\'' +
				", date='" + date + '\'' +
				'}';
	}
}
