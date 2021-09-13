package com.barte.findmyphone;

import android.content.Context;
import android.util.Log;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgentV2;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;

public class SamsungAccessoryService extends SAAgentV2 {

    private static final String PLAY = "play";
    private static final String STOP = "stop";

    private static final String TAG = "FIND_MY_PHONE_SAMSUNG_ACCESSORY";
    private static final Class<ServiceConnection> SASOCKET_CLASS = ServiceConnection.class;

    private ServiceConnection mConnectionHandler = null;
    private AlarmMediaPlayer mediaPlayer;

    public class ServiceConnection extends SASocket {

        public ServiceConnection() {
            super(ServiceConnection.class.getName());
        }

        @Override
        public void onError(int i, String s, int i1) {
            Log.e(TAG, "ERROR: " + s);
        }

        @Override
        public void onReceive(int i, byte[] bytes) {
            if (mConnectionHandler == null) return;
            String data = new String(bytes);

            switch (data) {
                case PLAY:
                    mediaPlayer.start();
                    sendData("Playing.");
                    break;

                case STOP:
                    mediaPlayer.stop();
                    sendData("Stopping.");
                    break;

                default:
                    sendData("ERROR: Unrecognized command.");
                    break;
            }
        }

        @Override
        protected void onServiceConnectionLost(int i) {
            mConnectionHandler = null;
        }
    }

    public SamsungAccessoryService(Context context) {
        super(TAG, context, SASOCKET_CLASS);

        mediaPlayer = new AlarmMediaPlayer(context);

        SA mAccessory = new SA();
        try {
            mAccessory.initialize(context);
        } catch (SsdkUnsupportedException e) {
            if (processUnsupportedException(e)) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] saPeerAgents, int i) {
        switch (i) {
            case PEER_AGENT_FOUND:
                Log.e(TAG, "Peer Agent is found");
                break;

            case FINDPEER_DEVICE_NOT_CONNECTED:
                Log.e(TAG, "Peer Agents are not found, no accessory device connected");
                break;

            case FINDPEER_SERVICE_NOT_FOUND:
                Log.e(TAG, "No matching service on connected accessory");
                break;
        }
        super.onFindPeerAgentsResponse(saPeerAgents, i);
    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent saPeerAgent) {
        if (saPeerAgent != null) {
            acceptServiceConnectionRequest(saPeerAgent);
        }
        super.onServiceConnectionRequested(saPeerAgent);
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent saPeerAgent, SASocket saSocket, int i) {
        switch (i) {
            case SAAgentV2.CONNECTION_SUCCESS:
                if (saSocket != null) {
                    mConnectionHandler = (ServiceConnection) saSocket;
                }
                break;

            case SAAgentV2.CONNECTION_ALREADY_EXIST:
                Log.e(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
                break;
        }
        super.onServiceConnectionResponse(saPeerAgent, saSocket, i);
    }

    protected void sendData (final String data) {
        if (mConnectionHandler != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mConnectionHandler.send(getServiceChannelId(0), data.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errType = e.getType();
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
                || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
            releaseAgent();
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.");
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.");
            return false;
        }
        return true;
    }

}
