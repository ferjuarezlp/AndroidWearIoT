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
public class MobileMessageListenerService extends WearableListenerService {
    public static final String MSG_FROM_WEAR = "/msg_from_wear";

    private static final String TAG = "MobileMessageListenerService";

    /** # of msg received */
    private static int msgCount = 0;


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(MSG_FROM_WEAR)) {
            try {
                String data = new String(messageEvent.getData(), "UTF-8"); // convert byte[] to string.
                Log.d(TAG, "message received from wear to mobile.  data=" + data);
                Intent intent = new Intent(this, PhoneActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PhoneActivity.PARAM_MSG, "msg received.  data=" + data);
                intent.putExtra(PhoneActivity.PARAM_NUM_MSG, ++msgCount);
                startActivity(intent);
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "data in the message from wear is not utf-8 encoding.  message path = " + messageEvent.getPath(), ex);
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

}
