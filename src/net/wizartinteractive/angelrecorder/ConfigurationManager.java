package net.wizartinteractive.angelrecorder;

import java.io.File;

import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.CallType;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

public class ConfigurationManager
{
	private static ConfigurationManager configurationManager;

	private static SharedPreferences.Editor editor;
	private static SharedPreferences preferences;

	private static SharedPreferences angelRecorderPreferences;

	public static ConfigurationManager getInstance()
	{
		if (configurationManager == null)
		{
			throw new IllegalStateException(String.format("%s is not initialized, call initializeDB(..) method first.", ConfigurationManager.class.getSimpleName()));
		}

		return configurationManager;
	}

	public static void Init(Context context)
	{
		configurationManager = new ConfigurationManager();
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		angelRecorderPreferences = context.getSharedPreferences("AngelRecorderPreferences", Context.MODE_PRIVATE);
		editor = angelRecorderPreferences.edit();

		File storageFolder = new File(String.format("%s/AngelRecorder", Environment.getExternalStorageDirectory()));

		if (!storageFolder.exists())
		{
			storageFolder.mkdir();
		}
	}

	public boolean getServiceEnabled()
	{
		return this.preferences.getBoolean(PreferencesActivity.SERVICE_ENABLED_KEY, true);
	}

	public boolean getNotificationsEnabled()
	{
		return this.preferences.getBoolean(PreferencesActivity.NOTIFICATIONS_ENABLED_KEY, true);
	}

	public int getAudioSource()
	{
		return Integer.parseInt(this.preferences.getString(PreferencesActivity.AUDIO_SOURCE_KEY, String.format("%s", MediaRecorder.AudioSource.VOICE_CALL)));
	}

	public int getAudioFormat()
	{
		return Integer.parseInt(this.preferences.getString(PreferencesActivity.AUDIO_FORMAT_KEY, String.format("%s", MediaRecorder.OutputFormat.MPEG_4)));
	}

	public String getPhoneNumber()
	{
		return this.angelRecorderPreferences.getString(PreferencesActivity.PHONE_NUMBER_KEY, "");
	}

	public String getAppFolderStorage()
	{
		return String.format("%s/AngelRecorder/", Environment.getExternalStorageDirectory());
	}

	public long getAvailableLocalStorageSpace()
	{
		StatFs status = new StatFs(String.format("%s", Environment.getExternalStorageDirectory().getPath()));

		long available = 0;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			available = status.getAvailableBytes();
		}
		else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			available = new File(this.getAppFolderStorage()).getFreeSpace();
		}

		return available;
	}
	
	public int getCallDirection()
	{
		return this.angelRecorderPreferences.getInt(PreferencesActivity.CALL_STATE_KEY, 0);
	}

	public void setAudioSource(int audioSource)
	{
		this.editor.putInt(PreferencesActivity.AUDIO_SOURCE_KEY, audioSource);

		this.editor.commit();
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.editor.putString(PreferencesActivity.PHONE_NUMBER_KEY, phoneNumber);
		
		this.editor.commit();
	}

	public void setCallDirection(int direction)
	{
		this.editor.putInt(PreferencesActivity.CALL_STATE_KEY, direction);

		this.editor.commit();
	}
}
