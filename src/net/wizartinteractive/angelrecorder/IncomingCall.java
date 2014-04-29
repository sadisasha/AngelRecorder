package net.wizartinteractive.angelrecorder;

import net.wizartinteractive.common.Utilities;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class IncomingCall extends BroadcastReceiver
{
	private static final String LOG_TAG = "Telephone";

	private Integer callState = Integer.valueOf(TelephonyManager.CALL_STATE_IDLE);
	private String phoneNumber = null;

	// private ConfigurationManager configurationManager = null;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// if (this.configurationManager == null)
		// {
		// this.configurationManager = ConfigurationManager.getInstance();
		// this.configurationManager.Init(context);
		// }

		PhoneListener phoneListener = new PhoneListener(context);

		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		// this.phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

		// if (this.phoneNumber != null)
		// {
		// this.configurationManager.setIncomingPhoneNumber(this.phoneNumber);
		// }
		//
		// Intent localIntent = new Intent(context, CallRecordingService.class);
		//
		// localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//
		// this.callState = Integer.valueOf(localTelephonyManager.getCallState());

		// switch (this.callState.intValue())
		// {
		// case TelephonyManager.CALL_STATE_RINGING:
		//
		// Utilities.logDebugMessage(this.LOG_TAG, String.format("Phone is ringing, incoming number %s", this.phoneNumber), null);
		//
		// break;
		//
		// case TelephonyManager.CALL_STATE_OFFHOOK:
		//
		// Utilities.logDebugMessage(this.LOG_TAG, String.format("Phone call offhook, incoming number %s", this.phoneNumber), null);
		// context.startService(localIntent);
		//
		// return;
		//
		// case TelephonyManager.CALL_STATE_IDLE:
		//
		// context.stopService(localIntent);
		// Utilities.logDebugMessage(this.LOG_TAG, String.format("Phone call hung up, incoming number %s. STOPING recording service", this.phoneNumber), null);
		//
		// break;
		//
		// default:
		// return;
		// }
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
				this.configurationManager = ConfigurationManager.getInstance();
				this.configurationManager.Init(context);
			}
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			super.onCallStateChanged(state, incomingNumber);

			switch (state)
			{
			case TelephonyManager.CALL_STATE_RINGING:

				Utilities.logDebugMessage(LOG_TAG, String.format("Phone is ringing, incoming number %s", incomingNumber));

				if (incomingNumber != null && incomingNumber != "")
				{
					configurationManager.setIncomingPhoneNumber(incomingNumber);
				}

				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:

				Utilities.logDebugMessage(LOG_TAG, String.format("Phone call offhook, incoming number %s", this.configurationManager.getPhoneNumber()));

				Intent recordingServiceIntent = new Intent(this.context, CallRecordingService.class);
                recordingServiceIntent.putExtra("phoneNumber", this.configurationManager.getPhoneNumber());
				context.startService(recordingServiceIntent);

				return;

			case TelephonyManager.CALL_STATE_IDLE:

				Utilities.logDebugMessage(LOG_TAG, String.format("Phone call hung up, incoming number %s. STOPING recording service", this.configurationManager.getPhoneNumber()));

				break;

			default:
				return;
			}
		}
	}
}