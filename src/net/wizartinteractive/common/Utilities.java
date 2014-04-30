package net.wizartinteractive.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.wizartinteractive.angelrecorder.MainActivity;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class Utilities extends MainActivity
{
	private static final boolean LOG_ENABLED = true;

	private static final String dateFormat = "yyyy-MM-dd hh:mm:ss.sss";

	public static void logErrorMessage(String tag, String message, Exception exception)
	{
		if (LOG_ENABLED)
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

	public void uno()
	{

	}

	public static void logInfoMessage(String tag, String message)
	{
		if (LOG_ENABLED)
		{
			Log.i(tag, message);
		}
	}

	public static void logDebugMessage(String tag, String message)
	{
		if (LOG_ENABLED)
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

			fileOut.write(message);
			fileOut.close();
		}
		catch (Exception ex)
		{
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

	public static String ConvertMilisecondsToHMS(long miliseconds)
	{
		return String.format("%s", DateFormat.format("hh:mm:ss", new Date(miliseconds)));
	}
}
