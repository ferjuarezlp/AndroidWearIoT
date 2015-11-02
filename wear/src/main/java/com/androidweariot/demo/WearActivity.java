package com.androidweariot.demo;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WearActivity extends Activity implements SensorEventListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /** msg path */
    public static final String MSG_FROM_WEAR = "/msg_from_wear";

    public static final String PARAM_MSG = "param_msg";
    /** # of msg received */
    public static final String PARAM_NUM_MSG = "param_num_msg";

    private static final String TAG = WearActivity.class.getName();

    private static final String VALUE_KEY = "com.androidweariot.key.hearthvalue";

    private GoogleApiClient mGoogleApiClient;

    private TextView rate;
    private TextView accuracy;
    private TextView sensorInformation;
    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String msg = extras.getString(PARAM_MSG);
                    int numMsg = extras.getInt(PARAM_NUM_MSG);
                    if (msg != null) {
                        //mTextView.setText("msg received=" + msg);
                    }
                    //textView2.setText("# of msgs received: " + numMsg);
                }





                rate = (TextView) stub.findViewById(R.id.rate);
                rate.setText("Reading...");

                accuracy = (TextView) stub.findViewById(R.id.accuracy);
                sensorInformation = (TextView) stub.findViewById(R.id.sensor);
                mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

            }
        });

        connect();
    }


    private void connect() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        //  "onConnected: null" is normal.
                        //  There's nothing in our bundle.
                        //sendMessage(MSG_FROM_WEAR, "a msg from wear to mobile: " + new Date().toString());
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();

        // need to add <meta-data> com.google.android.gms.version tag into androidmanifest.xml
        mGoogleApiClient.connect();
    }

    /**
     * send message to wearable data layer
     * @param path - msg path, used to identify what the msg is about
     * @param data - data inside msg
     */
    private void sendMessage(final String path, final String data){

        // it is necessary to put the following code in asynctask
        // getNodes() uses await(), which cannot be used on ui thread.
        // we will see an error about await cannot be called on ui thread if we do not use asynctask.
        new AsyncTask<Void, Void, List<Node>>(){

            @Override
            protected List<Node> doInBackground(Void... params) {
                return getNodes();
            }

            @Override
            protected void onPostExecute(List<Node> nodeList) {
                for(Node node : nodeList) {
                    Log.v(TAG, "sending msg.  data=" + data);

                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient,
                            node.getId(),
                            path,
                            data.getBytes()
                    );

                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.v(TAG, "Phone: " + sendMessageResult.getStatus().getStatusMessage());
                        }
                    });
                }
            }
        }.execute();

    }


    /**
     * get a list of connected nodes that you can potentially send messages to
     */
    private List<Node> getNodes() {
        ArrayList<Node> results = new ArrayList<Node>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node);
        }
        return results;
    }

    /*private void updateData(String value) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/hearthrate");
        putDataMapReq.getDataMap().putString(VALUE_KEY, value);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.e("WEAR -*-*-*-onResult ", dataItemResult.getStatus().getStatusMessage());
            }
        });
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //Register the listener
        if (mSensorManager != null){
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unregister the listener
        if (mSensorManager!=null)
            mSensorManager.unregisterListener(this);

        if(mGoogleApiClient != null) mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        if (mSensorManager!=null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Update your data.
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            if(event.values[0] <= 0) { // HR sensor is being initialized
                sendMessage(MSG_FROM_WEAR, String.valueOf(Float.valueOf(event.values[0]).intValue()) + new Date().toString());
                sensorInformation.setText(String.valueOf(Float.valueOf(event.values[0]).intValue()));
            } else {
                sensorInformation.setText("Iniciando...");
            }

            Log.d(TAG, "sensor event: " + event.accuracy + " = " + event.values[0]);
                
            rate.setText(String.valueOf(event.values[0]));
            accuracy.setText("Accuracy: " + event.accuracy);
            //updateData(event.sensor.toString() + System.currentTimeMillis());
            //sensorInformation.setText(event.sensor.toString());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

        Log.d(TAG, "accuracy changed: " + i);
        accuracy.setText("Accuracy: " + Integer.toString(i));
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }



}
