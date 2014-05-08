package net.wizartinteractive.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.wizartinteractive.angelrecorder.MainActivity;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class Utilities extends MainActivity
{
	public static void logErrorMessage(String tag, String message, Exception exception)
	{
		if (Constants.LOG_ENABLED)
		{
			if (exception != null)
			{
				exception.printStackTrace();

				StringWriter stringWriter = new StringWriter();
				exception.printStackTrace(new PrintWriter(stringWriter));
				String stacktrace = stringWriter.toString();

				writeEntryToLog(String.format("Class: %s \nLocalized Message: %s \nMessage: %s \nStackTrace: %s", exception.getClass().getName(), exception.getLocalizedMessage(), exception.getMessage(), stacktrace));
			}

			Log.e(tag, message);
		}
	}

	public static void logInfoMessage(String tag, String message)
	{
		if (Constants.LOG_ENABLED)
		{
			Log.i(tag, message);
		}
	}

	public static void logDebugMessage(String tag, String message)
	{
		if (Constants.LOG_ENABLED)
		{
			Log.d(tag, message);
		}
	}

	private static void writeEntryToLog(String message)
	{
		try
		{
			File storagePath = Environment.getExternalStorageDirectory();

			File logFile = new File(storagePath.getAbsolutePath(), "AngelRecorder.log");

			OutputStreamWriter fileOut = new OutputStreamWriter(new FileOutputStream(logFile));

			fileOut.append(message);
			fileOut.close();
		}
		catch (Exception ex)
		{
		}
	}

	public static Date ConvertStringToDate(String dateString)
	{
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);

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
		return String.format("%s", DateFormat.format(Constants.DATE_FORMAT, date));
	}
	
	public static String ConvertDateToShortDateString(Date date)
	{
		return String.format("%s", DateFormat.format(Constants.SHORT_DATE_FORMAT, date));
	}

	public static String ConvertMilisecondsToHMS(long milliseconds)
	{

		int seconds = (int) (milliseconds / 1000) % 60;
		int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
		int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}
