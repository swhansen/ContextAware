package org.obliquevision.contextaware.app;

        import android.app.Activity;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.View;
        import android.view.MenuItem;
        import android.os.RemoteException;
        import android.widget.EditText;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;

        import com.radiusnetworks.ibeacon.IBeaconConsumer;
        import com.radiusnetworks.ibeacon.IBeaconManager;
        import com.radiusnetworks.ibeacon.MonitorNotifier;
        import com.radiusnetworks.ibeacon.Region;





public class MainActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "MonitoringActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        verifyBluetooth();
        iBeaconManager.bind(this);
    }



    private void verifyBluetooth() {

        try {
            if (!IBeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }

    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (iBeaconManager.isBound(this)) iBeaconManager.setBackgroundMode(this, false);
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)MainActivity.this
                        .findViewById(R.id.monitoringText);
                editText.append(line+"\n");
            }
        });
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                logToDisplay("I just saw an iBeacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                logToDisplay("I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                logToDisplay("I have just switched from seeing/not seeing iBeacons: "+state);
            }


        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

}
