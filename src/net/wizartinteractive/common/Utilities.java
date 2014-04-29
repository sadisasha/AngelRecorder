package net.wizartinteractive.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.wizartinteractive.angelrecorder.MainActivity;
import android.text.format.DateFormat;
import android.util.Log;

public class Utilities extends MainActivity
{
	private static final boolean LOG_ENABLED = true;
	
	private static final String dateFormat = "yyyy-MM-dd hh:mm:ss.sss";
	
	public static void logErrorMessage(String tag, String message, Exception exception)
	{
		if(LOG_ENABLED)
		{
			if(exception != null)
			{
				exception.printStackTrace();
			}
			
			Log.e(tag, message);
		}
	}
	
	public static void logInfoMessage(String tag, String message)
	{
		if(LOG_ENABLED)
		{
			Log.i(tag, message);
		}
	}
	
	public static void logDebugMessage(String tag, String message)
	{
		if(LOG_ENABLED)
		{
			Log.d(tag, message);
		}
	}
	
	public static Date ConvertStringToDate(String dateString)
	{
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		
		try
		{
			date = format.parse(dateString);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		
		return date;
	}
	
	public static String ConvertDateToString(Date date)
	{
		return String.format("%s", DateFormat.format(dateFormat, date));
	}
}
