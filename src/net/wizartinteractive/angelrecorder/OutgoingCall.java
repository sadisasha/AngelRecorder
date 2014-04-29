package net.wizartinteractive.angelrecorder;

import net.wizartinteractive.common.Utilities;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class OutgoingCall extends BroadcastReceiver
{
	private static final String LOG_TAG = "Telephone";

	private ConfigurationManager configurationManager = null;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Utilities.logDebugMessage(LOG_TAG, "OutgoingCall in progress");

		if (this.configurationManager == null)
		{
			this.configurationManager = ConfigurationManager.getInstance();
			this.configurationManager.Init(context);
		}

		String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		this.configurationManager.setOutgoingPhoneNumber(phoneNumber);
	}

}
