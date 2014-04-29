package net.wizartinteractive.angelrecorder;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

public class ConfigurationManager
{
	private static ConfigurationManager instance;

	private Context context;

	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;
	
	private SharedPreferences angelRecorderPreferences;


	public static ConfigurationManager getInstance()
	{
		if (instance == null)
		{
			instance = new ConfigurationManager();
		}

		return instance;
	}

	public void Init(Context context)
	{
		this.context = context;
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
		this.angelRecorderPreferences = this.context.getSharedPreferences("AngelRecorderPreferences", Context.MODE_PRIVATE);
		this.editor = this.angelRecorderPreferences.edit();

		File storageFolder = new File(String.format("%s/AngelRecorder", Environment.getExternalStorageDirectory()));

		if (!storageFolder.exists())
		{
			storageFolder.mkdir();
		}
	}

	public boolean getServiceEnabled()
	{
		return this.preferences.getBoolean(PreferencesActivity.SERVICE_ENABLED_KEY, false);
	}

	public int getAudioSource()
	{
		return Integer.parseInt(this.preferences.getString(PreferencesActivity.AUDIO_SOURCE_KEY, String.format("%s", MediaRecorder.AudioSource.VOICE_CALL)));
	}

	public int getAudioFormat()
	{
		return Integer.parseInt(this.preferences.getString(PreferencesActivity.AUDIO_FORMAT_KEY, String.format("%s", MediaRecorder.OutputFormat.MPEG_4)));
		// return this.preferences.getInt(PreferencesActivity.AUDIO_FORMAT_KEY, MediaRecorder.OutputFormat.MPEG_4);
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
		else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			available = new File(this.getAppFolderStorage()).getFreeSpace();
		}

		return available;
	}

	public void setAudioSource(int audioSource)
	{
		this.editor.putInt(PreferencesActivity.AUDIO_SOURCE_KEY, audioSource);

		this.editor.commit();
	}

	public void setIncomingPhoneNumber(String phoneNumber)
	{
		this.editor.putString(PreferencesActivity.PHONE_NUMBER_KEY, phoneNumber);
		this.editor.putString(PreferencesActivity.CALL_STATE_KEY, "Incoming");

		this.editor.commit();
	}

	public void setOutgoingPhoneNumber(String phoneNumber)
	{
		this.editor.putString(PreferencesActivity.PHONE_NUMBER_KEY, phoneNumber);
		this.editor.putString(PreferencesActivity.CALL_STATE_KEY, "Outgoing");

		this.editor.commit();
	}
}