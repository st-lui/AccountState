package ru.russianpost.accountstate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


class SmsMessageData{
	Date smsDate;
	String smsBody;
	
}

public class AccountState extends Activity {
	
	public class SmsMessageData implements Comparable<SmsMessageData>{
		public Date smsDate;
		public String Body;
		
		public SmsMessageData() {
			Body=new String();
			smsDate=new Date();
		}
		
		public SmsMessageData(Date _smsDate,String _smsBody){
			smsDate=_smsDate;
			Body=_smsBody; 
		}
		
		public SmsMessageData(Long _smsDate,String _smsBody){
			smsDate=new Date(_smsDate);
			Body=_smsBody; 
		}

		@Override
		public int compareTo(SmsMessageData another) {
			return this.smsDate.compareTo(another.smsDate);
		}
		
	}

	public double GetBalance(){
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor c= getContentResolver().query(uri, null, null ,null,null);
		startManagingCursor(c);
		
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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		double startBalance = GetBalance();
		final TextView textView = (TextView) findViewById(R.id.textView2);
		textView.setText(((Double)startBalance).toString());
	}
}
