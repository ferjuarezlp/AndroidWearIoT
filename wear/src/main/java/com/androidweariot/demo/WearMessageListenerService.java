package com.androidweariot.demo;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;

/**
 * listen to message received.
 * remember to add <service></service> to androidmanifest.xml.
 * extends wearable listener service.
 *
 * there are 2 ways to listen to message.
 * 1. this is the first way, using WearableListenerService.
 * 2. you can also use MessageApi.MessageListener
 * see http://www.binpress.com/tutorial/a-guide-to-the-android-wear-message-api/152
 *
 * @author sol wu
 */
public class WearMessageListenerService extends WearableListenerService {

    private static final String START_ACTIVITY = "/start_activity";

    private static final String TAG = "WearMessageListenerService";


    /** # of msg received */
    private static int msgCount = 0;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY ) ) {
            try {
                String data = new String(messageEvent.getData(), "UTF-8"); // convert byte[] to string.
                Log.d(TAG, "message received.  data = " + data);
                Intent intent = new Intent(this, WearActivity.class);
                intent.putExtra(WearActivity.PARAM_MSG, data);
                intent.putExtra(WearActivity.PARAM_NUM_MSG, ++msgCount);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                //            final TextView text = (TextView) findViewById(R.id.text);
                //            text.setText("# of msg received: " + ++msgCount);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        } else {
            super.onMessageReceived( messageEvent );
        }
    }
}
