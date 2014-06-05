package net.wizartinteractive.angelrecorder;

import java.util.Date;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{

	public static final String AUDIO_SOURCE_KEY = "PreferenceAudioSource";
	public static final String AUDIO_FORMAT_KEY = "PreferenceAudioFormat";
	public static final String SERVICE_ENABLED_KEY = "PreferenceIsServiceEnabled";
	public static final String NOTIFICATIONS_ENABLED_KEY = "PreferenceAreNotificationsEnabled";
	public static final String PHONE_NUMBER_KEY = "PhoneNumber";
	public static final String CALL_STATE_KEY = "CallState";
	public static final String AUTO_DELETE_ENABLED_KEY = "PreferenceIsAutoDeleteEnabled";
	public static final String AUTO_DELETE_PERIOD_KEY = "PreferenceAutoDeletePeriod";
	public static final String AUTO_DELETE_LAST_EXECUTION_KEY = "LastExecution";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		ConfigurationManager.Init(getApplicationContext());

		if (key.equals(this.SERVICE_ENABLED_KEY) && !ConfigurationManager.getInstance().getServiceEnabled())
		{
			Intent stopRecordingServiceIntent = new Intent(getApplicationContext(), CallRecordingService.class);
			getApplicationContext().stopService(stopRecordingServiceIntent);
		}
		else if (key.equals(this.AUTO_DELETE_ENABLED_KEY) && ConfigurationManager.getInstance().getAutoDeleteEnabled())
		{
			ConfigurationManager.getInstance().setAutoCleanLastExecution(new Date());
		}
	}
}
