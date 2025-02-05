package com.apayen.ft_hangouts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	public static final String CONTACT_TABLE = "CONTACT_TABLE";
	public static final String COL_CONTACT_ID = "CONTACT_ID";
	public static final String COL_CONTACT_PHOTO = "CONTACT_PHOTO";
	public static final String COL_CONTACT_NAME = "CONTACT_NAME";
	public static final String COL_CONTACT_PHONE = "CONTACT_PHONE";
	public static final String COL_CONTACT_EMAIL = "CONTACT_EMAIL";
	public static final String COL_CONTACT_STREET = "CONTACT_STREET";
	public static final String COL_CONTACT_CITY = "CONTACT_CITY";
	public static final String COL_CONTACT_ZIP = "CONTACT_ZIP";
	public static final String COL_CONTACT_NOTES = "CONTACT_NOTES";
	public static final String SMS_TABLE = "SMS_TABLE";
	public static final String COL_SMS_ID = "SMS_ID";
	public static final String COL_SMS_CONTACT_ID = "SMS_CONTACT_ID";
	public static final String COL_SMS_TYPE = "SMS_TYPE";
	public static final String COL_SMS_MESSAGE = "SMS_MESSAGE";
	public static final String COL_SMS_DATE = "SMS_DATE";
	//////////////////////////////
	// Constructor
	//////////////////////////////
	public DatabaseManager(@Nullable Context context)
	{ super(context, "contact.db", null, 1); }
	//////////////////////////////
	// Creation
	//////////////////////////////
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String statementContact = "CREATE TABLE IF NOT EXISTS " + CONTACT_TABLE
			+ " ("
			+ COL_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_CONTACT_PHOTO + " TEXT, "
			+ COL_CONTACT_NAME + " TEXT, "
			+ COL_CONTACT_PHONE + " TEXT, "
			+ COL_CONTACT_EMAIL + " TEXT, "
			+ COL_CONTACT_STREET + " TEXT, "
			+ COL_CONTACT_CITY + " TEXT, "
			+ COL_CONTACT_ZIP + " TEXT, "
			+ COL_CONTACT_NOTES + " TEXT"
			+ ")";
		String statementSMS = "CREATE TABLE IF NOT EXISTS " + SMS_TABLE
			+ " ("
			+ COL_SMS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COL_SMS_CONTACT_ID + " INTEGER, "
			+ COL_SMS_TYPE + " TEXT, "
			+ COL_SMS_MESSAGE + " TEXT, "
			+ COL_SMS_DATE + " INTEGER "
			+ ")";
		db.execSQL(statementContact);
		db.execSQL(statementSMS);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + SMS_TABLE);
		onCreate(db);
	}
	//////////////////////////////
	// Functions - Contacts
	//////////////////////////////
	public boolean addContact(ContactModel cm)
	{
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();

			cv.put(COL_CONTACT_PHOTO, cm.getPhoto());
			cv.put(COL_CONTACT_NAME, cm.getName());
			cv.put(COL_CONTACT_PHONE, cm.getPhone());
			cv.put(COL_CONTACT_EMAIL, cm.getEmail());
			cv.put(COL_CONTACT_STREET, cm.getStreet());
			cv.put(COL_CONTACT_CITY, cm.getCity());
			cv.put(COL_CONTACT_ZIP, cm.getZip());
			cv.put(COL_CONTACT_NOTES, cm.getNotes());
			long res = db.insert(CONTACT_TABLE, null, cv);
			db.close();
			return (res >= 0);
		}
		catch (Exception e) { return (false); }
	}
	public boolean updateContact(ContactModel cm)
	{
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();
			String selection = COL_CONTACT_ID + " = ?";
			String[] selectionArgs = { String.valueOf(cm.getID()) };

			cv.put(COL_CONTACT_PHOTO, cm.getPhoto());
			cv.put(COL_CONTACT_NAME, cm.getName());
			cv.put(COL_CONTACT_PHONE, cm.getPhone());
			cv.put(COL_CONTACT_EMAIL, cm.getEmail());
			cv.put(COL_CONTACT_STREET, cm.getStreet());
			cv.put(COL_CONTACT_CITY, cm.getCity());
			cv.put(COL_CONTACT_ZIP, cm.getZip());
			cv.put(COL_CONTACT_NOTES, cm.getNotes());
			int count = db.update(CONTACT_TABLE, cv, selection, selectionArgs);
			db.close();
			return (count > 0);
		}
		catch (Exception e) { return (false); }
	}
	public void deleteContact(ContactModel cm)
	{
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();

			db.delete(CONTACT_TABLE, COL_CONTACT_ID + " = ?", new String[]{String.valueOf(cm.getID())});
			db.close();
		}
		catch (Exception ignored) { }
	}
	public List<ContactModel> getContacts()
	{
		List<ContactModel> list = new ArrayList<>();
		try
		{
			SQLiteDatabase db = this.getReadableDatabase();
			String query = "SELECT * FROM " + CONTACT_TABLE;
			Cursor cursor = db.rawQuery(query, null);

			if (cursor.moveToFirst())
			{
				do
				{
					int id = cursor.getInt(0);
					String photo = cursor.getString(1);
					String name = cursor.getString(2);
					String phone = cursor.getString(3);
					String email = cursor.getString(4);
					String street = cursor.getString(5);
					String city = cursor.getString(6);
					String zip = cursor.getString(7);
					String notes = cursor.getString(8);
					ContactModel contact = new ContactModel(id, photo, name, phone, email, street, city, zip, notes);
					list.add(contact);
				} while (cursor.moveToNext());
			}
			cursor.close();
			db.close();
			return (list);
		}
		catch (Exception e) { return (list); }
	}
	public List<ContactModel> getContacts(String phoneToSearch)
	{
		List<ContactModel> list = new ArrayList<>();
		try
		{
			SQLiteDatabase db = this.getReadableDatabase();
			String query = "SELECT * FROM " + CONTACT_TABLE + " WHERE " + COL_CONTACT_PHONE + " = ? ORDER BY " + COL_CONTACT_NAME + " ASC";
			Cursor cursor = db.rawQuery(query, new String[]{phoneToSearch});

			if (cursor.moveToFirst())
			{
				do
				{
					int id = cursor.getInt(0);
					String photo = cursor.getString(1);
					String name = cursor.getString(2);
					String phone = cursor.getString(3);
					String email = cursor.getString(4);
					String street = cursor.getString(5);
					String city = cursor.getString(6);
					String zip = cursor.getString(7);
					String notes = cursor.getString(8);
					ContactModel contact = new ContactModel(id, photo, name, phone, email, street, city, zip, notes);
					list.add(contact);
				} while (cursor.moveToNext());
			}
			cursor.close();
			db.close();
			return (list);
		}
		catch (Exception e) { return (list); }
	}
	//////////////////////////////
	// Functions - SMS
	//////////////////////////////
	public boolean addSMS(SMSModel sm)
	{
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues cv = new ContentValues();

			cv.put(COL_SMS_CONTACT_ID, sm.getContactID());
			cv.put(COL_SMS_TYPE, sm.getType());
			cv.put(COL_SMS_MESSAGE, sm.getMessage());
			cv.put(COL_SMS_DATE, sm.getDate());

			long res = db.insert(SMS_TABLE, null, cv);
			db.close();
			return (res >= 0);
		}
		catch (Exception e) { return (false); }
	}
	public boolean deleteSMS(int id)
	{
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
			String query = "DELETE FROM " + SMS_TABLE + " WHERE " + COL_SMS_CONTACT_ID + " = " + id;
			Cursor cursor = db.rawQuery(query, null);
			boolean res = cursor.moveToFirst();

			cursor.close();
			db.close();
			return (res);
		}
		catch (Exception e) { return (false); }
	}
	public List<SMSModel> getSMSs(int IDToSearch)
	{
		List<SMSModel> list = new ArrayList<>();
		try
		{
			SQLiteDatabase db = this.getReadableDatabase();
			String query = "SELECT * FROM " + SMS_TABLE + " WHERE " + COL_SMS_CONTACT_ID + " = ? ORDER BY " + COL_SMS_DATE + " ASC";
			Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(IDToSearch)});

			if (cursor.moveToFirst())
			{
				do
				{
					int id = cursor.getInt(0);
					int contactId = cursor.getInt(1);
					String type = cursor.getString(2);
					String message = cursor.getString(3);
					int date = cursor.getInt(1);
					SMSModel sms = new SMSModel(id, contactId, type, message, date);
					list.add(sms);
				} while (cursor.moveToNext());
			}
			cursor.close();
			db.close();
			return (list);
		}
		catch (Exception e) { return (list); }
	}
}
