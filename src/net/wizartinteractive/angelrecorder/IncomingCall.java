package net.wizartinteractive.angelrecorder;

import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.dbmodels.CallType;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class IncomingCall extends BroadcastReceiver
{
	private static final String LOG_TAG = "Telephone";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		PhoneListener phoneListener = new PhoneListener(context);

		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private class PhoneListener extends PhoneStateListener
	{
		private Context context = null;

		private ConfigurationManager configurationManager = null;

		public PhoneListener(Context context)
		{
			this.context = context;

			if (this.configurationManager == null)
			{
				ConfigurationManager.Init(context);
				this.configurationManager = ConfigurationManager.getInstance();
			}
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			super.onCallStateChanged(state, incomingNumber);

			switch (state)
			{
			case TelephonyManager.CALL_STATE_RINGING:

				Utilities.logDebugMessage(LOG_TAG, String.format("Phone is ringing, number %s", incomingNumber));

				if (incomingNumber != null && incomingNumber != "")
				{
					configurationManager.setPhoneNumber(incomingNumber);
					configurationManager.setCallDirection(CallType.INCOMING.getType());
				}

				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:

				Utilities.logDebugMessage(LOG_TAG, String.format("Phone call offhook, number %s", this.configurationManager.getPhoneNumber()));

				Intent recordingServiceIntent = new Intent(this.context, CallRecordingService.class);
				context.startService(recordingServiceIntent);

				return;

			case TelephonyManager.CALL_STATE_IDLE:

				Utilities.logDebugMessage(LOG_TAG, String.format("Phone call hung up, incoming number %s. STOPING recording service", this.configurationManager.getPhoneNumber()));
				
				Intent stopRecordingServiceIntent = new Intent(this.context, CallRecordingService.class);
				context.stopService(stopRecordingServiceIntent);

				break;

			default:
				return;
			}
		}
	}
}