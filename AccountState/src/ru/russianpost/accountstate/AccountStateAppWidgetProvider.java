package ru.russianpost.accountstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.widget.RemoteViews;

public class AccountStateAppWidgetProvider extends AppWidgetProvider {
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().compareTo(ACTION)==0){
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,AccountStateAppWidgetProvider.class));
			this.onUpdate(context, appWidgetManager , appWidgetIds);
		}
		super.onReceive(context, intent);
	};
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Create an Intent to launch ExampleActivity
			Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName("com.sonyericsson.organizer", "com.sonyericsson.organizer.Organizer")).addCategory(Intent.CATEGORY_LAUNCHER);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					alarmClockIntent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.accountstate_appwidget);
			views.setOnClickPendingIntent(R.id.accountWidgetLayout, pendingIntent);
			double balance = 0;
			try{
				balance = GetBalance(context);
			}
			catch (Exception e){                                                                 
				String s = e.toString();
				System.err.println(s);
			}
			views.setTextViewText(R.id.appwidget_text, String.format(Locale.US,"%1$2G", ((Double)balance)));
			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	private double GetBalance(Context context){
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor c= context.getContentResolver().query(uri, null, null ,null,null);
		
		ArrayList<SmsMessageData> smsMessages = new ArrayList<SmsMessageData>();
		String number = "";
		                
		if(c.moveToFirst()){
		        for(int i=0;i<c.getCount();i++){
		                
		                number=c.getString(c.getColumnIndexOrThrow("address")).toString();
		                if (number.compareTo("Sviaz-Bank")==0){
		                	String b = c.getString(c.getColumnIndexOrThrow("body")).toString();
		                	Long date = c.getLong(c.getColumnIndexOrThrow("date"));
		                	smsMessages.add(new SmsMessageData(date,b));
		                }
		                c.moveToNext();
		        }
		}
		c.close();
		Object[] sortedSmsMessages =  smsMessages.toArray();
		Arrays.sort(sortedSmsMessages);
		int startIndex = sortedSmsMessages.length-1;
		for (int i=sortedSmsMessages.length-1;i>=0;i--){
			SmsMessageData smsMessageData = (SmsMessageData) sortedSmsMessages[i];
			if (smsMessageData.Body.contains("Dostupno")){
				startIndex=i;
				break;
			}
		}
		SmsMessageData smsMessageData = (SmsMessageData) sortedSmsMessages[startIndex];
		String[] bodyData= smsMessageData.Body.split("\\s+");
		double startBalance=0;
		for (int i=0;i<bodyData.length;i++){
			if (bodyData[i].compareTo("Dostupno")==0){
				startBalance = Double.parseDouble(bodyData[i+1].replace(',','.'));
			}
		}
		for (int i=startIndex+1;i<sortedSmsMessages.length;i++){
			SmsMessageData sms = (SmsMessageData) sortedSmsMessages[i];
			if (sms.Body.contains("Summa")){
				String[] smsData = sms.Body.split("\\s+");
				for (int j=0;j<smsData.length;j++){
					if (smsData[j].compareTo("Summa")==0){
						double changeBalance = Double.parseDouble(smsData[j+1].replace(',','.'));
						startBalance -=changeBalance;
					}
				}
			}
		}	
		return startBalance;
	}
}
