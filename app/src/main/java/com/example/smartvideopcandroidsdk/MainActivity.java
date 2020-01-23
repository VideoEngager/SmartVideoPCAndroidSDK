package com.example.smartvideopcandroidsdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.videoengager.clientsdk.VideoClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private Button mSignInButton;
    VideoClient mVideoClient;
    private Button mCallButton;
    private boolean mIsConnected;
    int MY_PERMISSIONS_REQUEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        }

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsConnected) {
                    showLogoutDialog();
                } else {
                    attemptLoginGenesys();
                }
            }
        });

        mCallButton = (Button) findViewById(R.id.call_button);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mGenesysSwitch.isChecked()) {
                mVideoClient.callGenesysAgent(false);
//                } else {
//                    mVideoClient.callAgent(false, false);
//                }
            }
        });
        mCallButton.setEnabled(false);
    }

    private void attemptLoginGenesys() {
        String agentUrl = "https://test.videoengager.com/slavd";
        String pcDisplayName = "Android SDK";
        String pcFirstName = "First Name";
        String pcLastName = "Last Name";
        String pcQueueName = "Support";
        String pcOrganizationId = "639292ca-14a2-400b-8670-1f545d8aa860";
        String pcDeploymentId = "1b4b1124-b51c-4c38-899f-3a90066c76cf";
        String pcEnvironment = "mypurecloud.de";

        try {
            loginGenesys(agentUrl, pcDisplayName, pcFirstName, pcLastName, pcQueueName, pcOrganizationId, pcDeploymentId, pcEnvironment);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loginGenesys(String agentUrl, String pcDisplayName, String pcFirstName, String pcLastName, String pcQueueName,
                              String pcOrganizationId, String pcDeploymentId, String pcEnvironment) throws Exception {
        if (mIsConnected) {
            return;
        }
        mVideoClient = VideoClient.getInstance(this);
        mVideoClient.setEventsListener(new VideoClient.VideoClientEventsListener() {
            @Override
            public void onInitResult(boolean success) {
                Log.i(TAG, "onInitResult:" + success);
                onSuccess(success);
            }

            @Override
            public void onAudioVideoCallStateChanged(VideoClient.CallState newCallState, boolean
                    isIncoming) {
                Log.i(TAG, "onAudioVideoCallStateChanged: " + newCallState.toString());
            }

            @Override
            public void onChatCallStateChanged(VideoClient.CallState newCallState, boolean
                    isIncoming) {
            }

            @Override
            public void onNewChatMessage() {
            }

            @Override
            public void onAgentStatusUpdate(boolean availableForCalls, boolean availableForChat) {
//                Log.i(TAG, "onAgentStatusUpdate");
//                onActionButtonsUpdate(availableForCalls, availableForChat);
            }
        });

        mVideoClient.initGenesysClient(agentUrl, pcDisplayName, pcFirstName, pcLastName, pcQueueName, pcOrganizationId, pcDeploymentId, pcEnvironment);
    }

    private void onSuccess(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateViews(success);
                if (!success) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void updateViews(boolean isConnected) {
        mIsConnected = isConnected;
        mCallButton.setEnabled(isConnected);
        if(isConnected) {
            mSignInButton.setText("LOG OUT");
        } else {
            mSignInButton.setText("START");
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to stop?");
        builder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
        builder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void logout() {
        if (mVideoClient != null) {
            mVideoClient.logout();
            updateViews(false);
        }
    }
}