package com.androidweariot.demo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhoneActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;
    private Firebase myFirebaseRef;

    public static final String PARAM_MSG = "param_msg";
    public static final String PARAM_NUM_MSG = "param_num_msg";

    @Bind(R.id.textViewHeartValue) TextView textViewHeartValue;
    @Bind(R.id.textViewAccValue) TextView textViewAcctValue;
    @Bind(R.id.textViewFirebaseState) TextView textViewFirebaseState;
    @Bind(R.id.textViewWearState) TextView textViewWearState;
    @Bind(R.id.buttonConnect) Button buttonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String msg = extras.getString(PARAM_MSG);
            int numMsg = extras.getInt(PARAM_NUM_MSG);
            if (msg != null) {
                textViewHeartValue.setText(msg);
            }

        }


        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://androidweariot.firebaseio.com/");
        textViewFirebaseState.setText(PhoneActivity.this.getString(R.string.label_connected));

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient = new GoogleApiClient.Builder(PhoneActivity.this)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle connectionHint) {
                                textViewWearState.setText(getString(R.string.label_connected));
                                tellWatchConnectedState("sending message");
                            }
                            @Override
                            public void onConnectionSuspended(int cause) {
                            }
                        })
                        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                textViewWearState.setText(getString(R.string.label_error));
                            }
                        })
                        .addApi(Wearable.API)
                        .build();
                mGoogleApiClient.connect();

               // tellWatchConnectedState("sending message");
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }


    private List<Node> getNodes() {
        ArrayList<Node> results = new ArrayList<Node>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node);
        }
        return results;
    }

    private void tellWatchConnectedState(final String state){
        new AsyncTask<Void, Void, List<Node>>(){

            private static final String START_ACTIVITY = "/start_activity";

            @Override
            protected List<Node> doInBackground(Void... params) {
                return getNodes();
            }

            @Override
            protected void onPostExecute(List<Node> nodeList) {
                for(Node node : nodeList) {
                    String msg = "telling " + node.getDisplayName() + " - " + node.getId() + " i am " + state;

                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient,
                            node.getId(),
                            START_ACTIVITY, //"/listener/lights/" + state,
                            msg.getBytes()
                    );

                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            textViewWearState.setText(PhoneActivity.this.getString(R.string.label_connected));
                        }
                    });
                }
            }
        }.execute();

    }
}
