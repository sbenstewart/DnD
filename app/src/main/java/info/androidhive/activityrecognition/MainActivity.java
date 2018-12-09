package info.androidhive.activityrecognition;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.DetectedActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver,broadcastWidget;
    public String phonenumber,message="Hello World!";
    private TextView txtActivity, txtConfidence;
    private ImageView imgActivity;
    private Button btnStartTrcking, btnStopTracking;
    private DetectedActivitiesIntentService bg1;
    private BackgroundDetectedActivitiesService bg2;
    Intent mServiceIntent,mServiceIntent2;
    private  Boolean stop=true;

    Context ctx;
    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        if (getIntent().getBooleanExtra("crash", false)) {
            Toast.makeText(this, "App restarted after crash", Toast.LENGTH_SHORT).show();
        }

        txtActivity = findViewById(R.id.txt_activity);
        txtConfidence = findViewById(R.id.txt_confidence);
        imgActivity = findViewById(R.id.img_activity);


        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle_button);
        toggle.setChecked(true);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startTracking();
                } else {
                    stopTracking();
                }
            }
        });
        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        final FloatingActionButton delaysmall = (FloatingActionButton) findViewById(R.id.delaysmall);
        final FloatingActionButton delaymedium = (FloatingActionButton) findViewById(R.id.delaymedium);
        final FloatingActionButton delaylarge = (FloatingActionButton) findViewById(R.id.delaylarge);
        final FloatingActionButton delaysample = (FloatingActionButton) findViewById(R.id.delaysample);
        delaysmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    toggle.setChecked(false);
                    stopTracking();
                    stop = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toggle.setChecked(true);
                            startTracking();
                        }
                    },30*60*1000);
                    menuMultipleActions.collapse();
                }
            });

        delaymedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(false);
                stopTracking();
                stop = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toggle.setChecked(true);
                        startTracking();
                    }
                },60*60*1000);
                menuMultipleActions.collapse();
            }
        });

        delaylarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(false);
                stopTracking();
                stop = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toggle.setChecked(true);
                        startTracking();
                    }
                },2*60*60*1000);
                menuMultipleActions.collapse();
            }
        });

        delaysample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(false);
                stopTracking();
                stop = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toggle.setChecked(true);
                        startTracking();
                    }
                },5*1000); //5 seconds
                menuMultipleActions.collapse();
            }
        });


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

        broadcastWidget =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
             startTracking();
            }
        };

        startTracking();
    }

  /*  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
*/
    private void handleUserActivity(int type, int confidence) {
        String label = getString(R.string.activity_unknown);
        int icon = R.drawable.ic_still;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.e(TAG, "User activity: " + label + ", Confidence: " + confidence);

        if (confidence > Constants.CONFIDENCE) {
            txtActivity.setText(label);
            txtConfidence.setText("Confidence: " + confidence);
            imgActivity.setImageResource(icon);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
