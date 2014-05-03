package net.wizartinteractive.angelrecorder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import net.wizartinteractive.common.Utilities;
import net.wizartinteractive.dbmodels.Call;
import net.wizartinteractive.dbmodels.CallType;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * the array adapter for the events list
 * 
 * @author Streetwizard
 * 
 */
public class CallsListAdapter extends ArrayAdapter<Call>
{
	// application context
	private Context appContext = null;

	// a layout inflater to inflate the events list
	private LayoutInflater layoutInflater;

	public CallsListAdapter(Context context, int resource, ArrayList<Call> objects)
	{
		super(context, resource, objects);
		this.appContext = context;
		this.layoutInflater = (LayoutInflater) this.appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder;

		final Call tempItemList = getItem(position);

		// this check improves performance to only create the elements to fill the device screen
		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.call_item, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.contact = (ImageView) convertView.findViewById(R.id.contact_imageView);
			viewHolder.phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber_textView);
			viewHolder.phoneDirection = (ImageView) convertView.findViewById(R.id.phone_imageView);
			viewHolder.details = (TextView) convertView.findViewById(R.id.callDetails_textView);
			viewHolder.date = (TextView) convertView.findViewById(R.id.date_textView);
			viewHolder.play = (ImageButton) convertView.findViewById(R.id.play_imageButton);
			viewHolder.play.setFocusable(false);
			viewHolder.play.setClickable(false);

			convertView.setTag(viewHolder);

			viewHolder.play.setOnClickListener(new OnClickListener()
			{
				private boolean isMediaPlaying = false;

				public void onClick(View v)
				{
					Intent viewIntent = new Intent(Intent.ACTION_VIEW);
					String filePath = tempItemList.getFilePath();
					
					File file = new File(filePath);
					
					if(file.exists())
					{
						viewIntent.setDataAndType(Uri.fromFile(file), "audio/*");
						viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	
						appContext.startActivity(Intent.createChooser(viewIntent, null));
					}
					else
					{
						Toast toast = Toast.makeText(appContext, appContext.getString(R.string.mediaPlayingFileError), Toast.LENGTH_LONG);
						toast.show();
					}
					
					// try
					// {
					// File file = new File(tempItemList.getFilePath());
					// Uri fileUri = Uri.fromFile(file);
					// MediaPlayer mediaPlayer = new MediaPlayer();
					// mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					// mediaPlayer.setDataSource(appContext, fileUri);
					// mediaPlayer.prepare();
					// mediaPlayer.start();
					// }
					// catch (Exception e)
					// {
					// e.printStackTrace();
					// Toast toast = Toast.makeText(appContext, appContext.getString(R.string.mediaPlayingFileError), Toast.LENGTH_LONG);
					// toast.show();
					// }
				}
			});
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		String contactName = this.getContactName(tempItemList.getIncomingNumber());
		InputStream photo = this.getContactPhoto(tempItemList.getIncomingNumber());
		// Uri photoUri = this.getContactPhoto(tempItemList.getIncomingNumber());

		// contact photo
		if (photo != null)
		{
			// viewHolder.contact.setImageURI(photoUri);
			viewHolder.contact.setImageBitmap(BitmapFactory.decodeStream(photo));
		}
		else
		{
			viewHolder.contact.setImageResource(R.drawable.ic_default_contact_photo);
		}

		// contact name
		if (contactName != null)
		{
			viewHolder.phoneNumber.setText(contactName);
		}
		else
		{
			viewHolder.phoneNumber.setText(tempItemList.getIncomingNumber());
		}

		// call type
		if (tempItemList.getType().getType() == CallType.INCOMING.getType())
		{
			viewHolder.phoneDirection.setImageResource(android.R.drawable.sym_call_incoming);
		}
		else
		{
			viewHolder.phoneDirection.setImageResource(android.R.drawable.sym_call_outgoing);
		}

		viewHolder.details.setText(String.format("%s", Utilities.ConvertMilisecondsToHMS(tempItemList.getDuration())));
		viewHolder.date.setText(String.format("%s", Utilities.ConvertDateToShortDateString(tempItemList.getDate())));
		viewHolder.play.setTag(tempItemList);

		return convertView;
	}

	private String getContactName(String phoneNumber)
	{

		String contactName = null;

		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cursor = this.appContext.getContentResolver().query(contactUri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);

		if (cursor == null)
		{
			return null;
		}

		try
		{
			if (cursor.moveToFirst())
			{
				contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			}
		}
		finally
		{
			cursor.close();
		}

		return contactName;
	}

	private InputStream getContactPhoto(String phoneNumber)
	{

		if (this.getContactName(phoneNumber) != null)
		{
			Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
			Cursor cursor = this.appContext.getContentResolver().query(contactUri, new String[] { PhoneLookup._ID }, null, null, null);

			long contactId = 0;

			if (cursor == null)
			{
				return null;
			}

			try
			{
				if (cursor.moveToFirst())
				{
					contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				}
			}
			finally
			{
				cursor.close();
			}

			return this.getContactPhoto(contactId);
		}

		return null;
	}

	private InputStream getContactPhoto(long contactId)
	{
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = this.appContext.getContentResolver().query(photoUri, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);

		// Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photo_id);
		// Cursor c = cr.query(photoUri, new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);

		if (cursor == null)
		{
			return null;
		}
		try
		{
			if (cursor.moveToFirst())
			{
				byte[] data = cursor.getBlob(0);
				if (data != null)
				{
					return new ByteArrayInputStream(data);
				}
			}
		}
		finally
		{
			cursor.close();
		}

		return null;
	}

	private class ViewHolder
	{
		ImageView contact;
		TextView phoneNumber;
		ImageView phoneDirection;
		TextView details;
		TextView date;
		ImageButton play;
	}
}
