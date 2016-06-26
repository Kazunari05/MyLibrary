package com.jetman.altbeaconlibrary;

import com.unity3d.player.UnityPlayerActivity;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;



/**
 * Created by Kazunari on 2016/06/26.
 */
public class BeaconLibrary extends UnityPlayerActivity implements BeaconConsumer {

    private static final String TAG = BeaconLibrary.class.getSimpleName();

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String UUID = "00000000-9ABA-1001-B000-001C4D25538C";
    
    private static String callbackLogTo = "";
    private static String beaconInfoString = "";

    private BeaconManager manager;
    private Identifier identifier = null;//Identifier.parse(UUID);

    protected static void DebugLog(String message) {
        if (callbackLogTo != null) {
            UnityPlayer.UnitySendMessage(callbackLogTo, "DebugLog", "[" + TAG + "] " + message);
        }
    }

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        DebugLog("onCreate()");

        init();
    }
    @Override
    protected void onPause() {
        super.onPause();
        DebugLog("onPause()");

        if (manager != null) {
            DebugLog("BeaconManager.unbind()");
            manager.unbind(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DebugLog("onResume()");

        if (manager != null) {
            DebugLog("BeaconManager.bind()");
            manager.bind(this);
        }
    }

    private void init() {
        BeaconLibrary.DebugLog("init()");

        final Activity activity = UnityPlayer.currentActivity;

        // CHECK Permission for Android 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        manager = BeaconManager.getInstanceForApplication(activity.getApplicationContext());
        manager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

    }

    @Override
    public void onBeaconServiceConnect() {
        BeaconLibrary.DebugLog("onBeaconServiceConnect()");

        manager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                BeaconLibrary.DebugLog("didEnterRegion()");

                try {
                    manager.startRangingBeaconsInRegion(new Region("unique-id-001", identifier, null, null));
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void didExitRegion(Region region) {
                BeaconLibrary.DebugLog("didExitRegion()");

                try {
                    manager.stopRangingBeaconsInRegion(new Region("unique-id-111", identifier, null, null));
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                //Log.d(TAG, "didDetermineStateForRegion");
                BeaconLibrary.DebugLog("didDetermineStateForRegion()");
            }
        });

        manager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons != null) {
                    for(Beacon beacon : beacons) {
                        final String outStr = "UUID:" + beacon.getId1() + ", major:" + beacon.getId2() + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance() + ",RSSI" + beacon.getRssi() + ", TxPower" + beacon.getTxPower();
                        beaconInfoString = outStr;
                        BeaconLibrary.DebugLog(outStr);

                        runOnUiThread(new Runnable() {
                            public void run() {

                            }
                        });
                    }
                }
            }
        });

        try {
            manager.startMonitoringBeaconsInRegion(new Region("unique-id-111", identifier, null, null));
        } catch(RemoteException e) {
            e.printStackTrace();
        }
    }

}
