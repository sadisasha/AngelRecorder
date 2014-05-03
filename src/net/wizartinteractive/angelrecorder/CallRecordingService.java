package net.wizartinteractive.angelrecorder;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.wizartinteractive.common.Constants;
import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.database.DBManager;
import net.wizartinteractive.dbmodels.Call;
import net.wizartinteractive.dbmodels.CallType;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
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

	private DBManager dbManager = null;

	private static Date recordingStart = null;
	private static Date recordingEnd = null;

	private final BroadcastReceiver incomingCallReceiver = new BroadcastReceiver()
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
					configurationManager.setPhoneNumber(phoneNumber);
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
			ConfigurationManager.Init(this);
			this.configurationManager = ConfigurationManager.getInstance();
		}

		if (this.dbManager == null)
		{
			DBManager.initializeDB(getApplicationContext());
			this.dbManager = DBManager.getInstance();
		}

		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(INCOMING_CALL_ACTION);
		this.registerReceiver(incomingCallReceiver, intentToReceiveFilter, null, handler);

		Thread aThread = new Thread(this);
		aThread.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		this.phoneNumber = this.configurationManager.getPhoneNumber();

		return START_STICKY_COMPATIBILITY;
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
			this.phoneNumber = this.configurationManager.getPhoneNumber();

			Utilities.logDebugMessage(LOG_TAG, String.format("Recording started saving to file: %s", this.getFilename(this.phoneNumber)));

			if (phoneNumber != "")
			{
				mediaRecorder = new MediaRecorder();
				// mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
				// mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				mediaRecorder.setAudioSource(this.configurationManager.getAudioSource());
				mediaRecorder.setOutputFormat(this.configurationManager.getAudioFormat());

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
				{
					mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				}
				else
				{
					mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				}

				mediaRecorder.setMaxDuration(0);

				mediaRecorder.setOutputFile(this.configurationManager.getAppFolderStorage() + this.getFilename(phoneNumber));

				try
				{
					mediaRecorder.prepare();
				}
				catch (Exception e)
				{
					Utilities.logErrorMessage(LOG_TAG, "Error preparing recorder.", e);
					e.printStackTrace();
					
					this.showNotification(this.getString(R.string.phoneCompatibilityError), false);
					
					mediaRecorder = null;
				}

				if(mediaRecorder != null)
				{
					try
					{
						mediaRecorder.start();
						isRecording = true;
	
						this.recordingStart = new Date();
	
						this.showNotification(this.getString(R.string.messageRecordingStarted), true);
					}
					catch (Exception e)
					{
						Utilities.logErrorMessage(LOG_TAG, "Error starting recorder", e);
						e.printStackTrace();
						mediaRecorder = null;
						isRecording = false;
						
						this.configurationManager.setPhoneNumber("");
	
						this.showNotification(this.getString(R.string.phoneCompatibilityError), false);
					}
				}
			}
		}
	}

	void stopRecording()
	{
		Utilities.logDebugMessage(LOG_TAG, String.format("Recording stopped call duration: %s seconds", (new Date().getTime() - this.recordingStart.getTime()) / 1000));

		if (isRecording)
		{
			this.recordingEnd = new Date();

			Call call = new Call();
			call.setDate(this.recordingStart);
			call.setDuration(this.recordingEnd.getTime() - this.recordingStart.getTime());
			call.setFilePath(this.getFilename(phoneNumber));
			call.setIncomingNumber(this.configurationManager.getPhoneNumber());
			call.setType(CallType.values()[this.configurationManager.getCallDirection()]);

			this.dbManager.addCall(call);

			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			isRecording = false;
			
			this.configurationManager.setPhoneNumber("");

			this.showNotification(this.getString(R.string.messageRecordingStopped), false);
		}

		mediaRecorder = null;
	}

	private void showNotification(String message, boolean started)
	{
		if (this.configurationManager.getNotificationsEnabled())
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			{
				Notification.Builder notificationBuilder = new Notification.Builder(this);
				notificationBuilder.setSmallIcon(android.R.drawable.sym_def_app_icon);
				notificationBuilder.setContentTitle(Constants.NOTIFICATION_TITLE);
				notificationBuilder.setContentText(message);
				notificationBuilder.setAutoCancel(true);

				PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
				notificationBuilder.setContentIntent(pendingIntent);

				Notification notification = notificationBuilder.build();

				if (started)
				{
					notification.flags = Notification.FLAG_ONGOING_EVENT;
				}
				else
				{
					notification.flags = Notification.FLAG_AUTO_CANCEL;
				}

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(this.LOG_TAG, 0, notification);
			}
			else
			{
				NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
				notificationBuilder.setSmallIcon(android.R.drawable.sym_def_app_icon);
				notificationBuilder.setContentTitle(Constants.NOTIFICATION_TITLE);
				notificationBuilder.setContentText(message);
				notificationBuilder.setAutoCancel(true);

				PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
				notificationBuilder.setContentIntent(pendingIntent);

				Notification notification = notificationBuilder.build();

				if (started)
				{
					notification.flags = Notification.FLAG_ONGOING_EVENT;
				}
				else
				{
					notification.flags = Notification.FLAG_AUTO_CANCEL;
				}

				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(this.LOG_TAG, 0, notification);
			}
		}
	}

	private String getFilename(String number)
	{
		String fileExtension = "";

		if (this.configurationManager.getAudioFormat() == MediaRecorder.OutputFormat.DEFAULT || this.configurationManager.getAudioFormat() == MediaRecorder.OutputFormat.THREE_GPP)
		{
			fileExtension = ".3gp";
		}
		else if (this.configurationManager.getAudioFormat() == MediaRecorder.OutputFormat.MPEG_4)
		{
			fileExtension = ".mp4";
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && this.configurationManager.getAudioFormat() == MediaRecorder.OutputFormat.AAC_ADTS)
		{
			fileExtension = ".m4a";
		}
		
		return String.format("%s %s%s", DateFormat.format("yyyy-MM-dd hhmmss", new Date()), number, fileExtension);
	}
}