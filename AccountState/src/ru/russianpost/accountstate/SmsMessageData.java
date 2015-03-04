package ru.russianpost.accountstate;

import java.util.Date;

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