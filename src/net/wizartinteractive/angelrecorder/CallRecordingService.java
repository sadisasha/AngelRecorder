package net.wizartinteractive.angelrecorder;

//public class CallRecordingService
//{
//
//}

import java.text.SimpleDateFormat;
import java.util.Date;

import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import net.wizartinteractive.dbmodels.CallType;
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
import android.text.format.DateFormat;

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

	private DBManager dbManager = DBManager.getInstance();

	private static Date recordingStart = null;
	private static Date recordingEnd = null;

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

				mediaRecorder.setOutputFile(this.getFilename(phoneNumber));

				try
				{
					mediaRecorder.prepare();
				}
				catch (Exception e)
				{
					Utilities.logErrorMessage(LOG_TAG, "Error preparing recorder.", e);
					e.printStackTrace();
					mediaRecorder = null;
				}

				try
				{
					mediaRecorder.start();
					isRecording = true;

					this.recordingStart = new Date();
				}
				catch (Exception e)
				{
					Utilities.logErrorMessage(LOG_TAG, "Error starting recorder", e);
					e.printStackTrace();
					mediaRecorder = null;
					isRecording = false;
				}
			}
		}
	}

	void stopRecording()
	{
		Utilities.logDebugMessage(LOG_TAG, String.format("Recording stopped call duration: %s seconds", (new Date().getTime() - this.recordingStart.getTime())));

		if (isRecording)
		{
			this.recordingEnd = new Date();
			
			// this.dbManager.openWritableDB();

			Call call = new Call();
			call.setDate(this.recordingStart);
			call.setDuration(this.recordingEnd.getTime() - this.recordingStart.getTime());
			call.setFilePath(this.getFilename(phoneNumber));
			call.setIncomingNumber(this.configurationManager.getPhoneNumber());
			call.setType(CallType.INCOMING);

			this.dbManager.addCall(call);

			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			isRecording = false;
			
			// this.dbManager.closeDatabase();
		}

		mediaRecorder = null;
	}

	private String getFilename(String number)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd-hh.mm.ss");
		return String.format("%s%s %s", this.configurationManager.getAppFolderStorage(), DateFormat.format("yyyy.MM.dd-hh.mm.ss", new Date()), number);
	}
}