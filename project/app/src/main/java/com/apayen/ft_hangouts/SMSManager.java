package com.apayen.ft_hangouts;

import android.app.Activity;
import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.List;

public class SMSManager extends BroadcastReceiver
{
	//////////////////////////////
	// Variables
	//////////////////////////////
	static int errno = 0;
	static DatabaseManager db = null;
	Context ctx = null;
	//////////////////////////////
	// Getters
	//////////////////////////////
	public static int getErrno()
	{ return (errno); }
	public static String getError(Context context, int errno)
	{
		String error = "";

		if (errno == 1)
			error = context.getString(R.string.error_sms_generic);
		else if (errno == 2)
			error = context.getString(R.string.error_sms_service);
		else if (errno == 3)
			error = context.getString(R.string.error_sms_pdu);
		else if (errno == 4)
			error = context.getString(R.string.error_sms_radio);
		return (error);
	}
	//////////////////////////////
	// Functions
	//////////////////////////////
	public static void send(Context context, String phone, String message)
	{
		SmsManager smsManager = SmsManager.getDefault();
		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE);
		PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), PendingIntent.FLAG_IMMUTABLE);

		smsManager.sendTextMessage(phone, null, message, sentIntent, deliveredIntent);
	}
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String code = intent.getAction();
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.apayen.NEW_SMS"));

		if (code != null)
		{
			if (code.equals("android.provider.Telephony.SMS_RECEIVED"))
			{
				Bundle bundle = intent.getExtras();
				if (bundle == null)
					return;
				Object[] pdus = (Object[]) bundle.get("pdus");
				if (pdus == null)
					return;
				String format = bundle.getString("format");

				if (db == null)
					db = new DatabaseManager(context);
				for (Object pdu : pdus)
				{
					SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
					String sender = sms.getOriginatingAddress();
					if (sender != null && sender.startsWith("+33"))
						sender = "0" + sender.replaceFirst("\\+\\d{1,2}", "");
					String message = sms.getMessageBody();

					List<ContactModel> contacts = db.getContacts(sender);
					if (contacts.isEmpty())
					{
						if (!db.addContact(new ContactModel(-1, "", sender, sender, "", "", "", "", "")))
							Toast.makeText(context, context.getString(R.string.error_adding_contact), Toast.LENGTH_LONG).show();
						else
						{
							List<ContactModel> list = db.getContacts(sender);

							if (!list.isEmpty())
							{
								Intent localIntent = new Intent("com.apayen.NEW_SMS");
								localIntent.putExtra("sender", sender);
								localIntent.putExtra("message", message);
								LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

								if (!db.addSMS(new SMSModel(-1, list.get(0).getID(), "Receive", message, System.currentTimeMillis())))
									Toast.makeText(context, context.getString(R.string.error_adding_sms), Toast.LENGTH_LONG).show();
							}
						}
					}
					for (ContactModel contact : contacts)
					{
						if (!db.addSMS(new SMSModel(-1, contact.getID(), "Receive", message, System.currentTimeMillis())))
							Toast.makeText(context, context.getString(R.string.error_adding_sms), Toast.LENGTH_LONG).show();
						else
						{
							Intent localIntent = new Intent("com.apayen.NEW_SMS");
							localIntent.putExtra("sender", sender);
							localIntent.putExtra("message", message);
							LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
						}
					}
				}
			}
			else if (code.equals("SMS_SENT"))
			{
				int resCode = getResultCode();

				if (resCode == Activity.RESULT_OK)
					errno = 0;
				else if (resCode == SmsManager.RESULT_ERROR_GENERIC_FAILURE)
					errno = 1;
				else if (resCode == SmsManager.RESULT_ERROR_NO_SERVICE)
					errno = 2;
				else if (resCode == SmsManager.RESULT_ERROR_NULL_PDU)
					errno = 3;
				else if (resCode == SmsManager.RESULT_ERROR_RADIO_OFF)
					errno = 4;
			}
			else if (code.equals("SMS_DELIVERED"))
				errno = 0;
		}
	}
}
