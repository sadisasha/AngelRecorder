package net.wizartinteractive.angelrecorder;

//public class CallRecordingService
//{
//
//}

import net.wizartinteractive.common.Utilities;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallRecordingService extends Service implements Runnable
{
	private String LOG_TAG = "Telephone";

	private static String phoneNumber;
	private MediaRecorder mediaRecorder;
	private final Handler handler = new Handler();

	private static boolean isRecording = false;

	private static final String INCOMING_CALL_ACTION = "android.intent.action.PHONE_STATE";
	public static final int NOTIFICATION_ID_RECEIVED = 0x1221;

	private static ConfigurationManager configurationManager = null;

	private final BroadcastReceiver callStateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			MyPhoneListener phoneListener = new MyPhoneListener(context);
			TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

			Bundle extras = intent.getExtras();

			if (extras != null)
			{
				String state = extras.getString(TelephonyManager.EXTRA_STATE);

				if (state.equals(TelephonyManager.EXTRA_STATE_RINGING))
				{
					phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
					configurationManager.setIncomingPhoneNumber(phoneNumber);
				}
			}
		}

		class MyPhoneListener extends PhoneStateListener
		{
			private Context appContext;
			
			MyPhoneListener(Context context)
			{
				super();
				appContext = context;
			}

			@Override
			public void onCallStateChanged(int state, String incomingNumber)
			{
				switch (state)
				{
				case TelephonyManager.CALL_STATE_IDLE:

					if (isRecording)
					{
						NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
						Notification not;

						stopRecording();
					}

					break;
				}
			}

		}
	};

	@Override
	public void onCreate()
	{
		super.onCreate();

		if (this.configurationManager == null)
		{
			this.configurationManager = ConfigurationManager.getInstance();
			this.configurationManager.Init(this);
		}

		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(INCOMING_CALL_ACTION);
		this.registerReceiver(callStateReceiver, intentToReceiveFilter, null, handler);

		Thread aThread = new Thread(this);
		aThread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		this.phoneNumber = intent.getExtras().getString("phoneNumber");
		configurationManager.setIncomingPhoneNumber(this.phoneNumber);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run()
	{
		Looper.myLooper();
		Looper.prepare();

		if (this.configurationManager.getServiceEnabled())
		{
			Utilities.logDebugMessage(LOG_TAG, String.format("Recording started saving to file: %s", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + phoneNumber + ".3gp"));

			this.phoneNumber = this.configurationManager.getPhoneNumber();

			if (phoneNumber != "")
			{
				mediaRecorder = new MediaRecorder();
				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				mediaRecorder.setMaxDuration(0);

				mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + phoneNumber + ".3gp");

				try
				{
					mediaRecorder.prepare();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					mediaRecorder.start();
					isRecording = true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	void stopRecording()
	{
		Utilities.logDebugMessage(LOG_TAG, "recording stopped");

		mediaRecorder.stop();
		mediaRecorder.reset();
		mediaRecorder.release();
		isRecording = false;
	}
}