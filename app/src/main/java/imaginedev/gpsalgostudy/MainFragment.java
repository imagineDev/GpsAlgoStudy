package imaginedev.gpsalgostudy;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wunderlist.slidinglayer.SlidingLayer;


public class MainFragment extends Fragment {


    SensorManager sensorManager;
    Sensor mSensor;
    SensorEventListener sensorEventListener;

    TextView tv_sensorType;
    TextView tv_sensorValue;
    FloatingActionButton playBtn;
    FloatingActionButton openSliderBtn;

    WorkerService mService;

    boolean isServiceConnected = false;

    Intent intent;

    float result = 0.0f;


    ServiceConnection serviceConnection;
    private SlidingLayer mSlidingLayer;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            if (mSensor == null) {
                mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Toast.makeText(getActivity(), "API : " + Build.VERSION.SDK_INT + "\nBut STEP_COUNTER is not Available", Toast.LENGTH_LONG).show();
            }
        } else {
            mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Toast.makeText(getActivity(), "Pre-Kitkat Device\nSo Using ACCELEROMETER", Toast.LENGTH_LONG).show();
        }

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    result = (event.values[0] + event.values[1] + event.values[2]) / 3;
                    tv_sensorValue.setText("" + result);
                } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

                    // result = (event.values[0] + event.values[1] + event.values[2])/3;
                    tv_sensorValue.setText("" + event.values[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                /* This is called when the connection with the service has been
                   established, giving us the service object we can use to
                   interact with the service. */

                WorkerService.MyBinder myBinder = (WorkerService.MyBinder) service;
                mService = myBinder.getService();
                isServiceConnected = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                /*  Called only when our Service is Crashed. */
                mService = null;
                isServiceConnected = false;
            }
        };


        intent = new Intent(getActivity(), WorkerService.class);

        getActivity().startService(intent);

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onPause() {
        super.onPause();

        sensorManager.unregisterListener(sensorEventListener);
        if (isServiceConnected) {
            getActivity().unbindService(serviceConnection);
        }
        // getActivity().stopService(intent);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        tv_sensorType = (TextView) view.findViewById(R.id.simple_sensor_sensorType);
        tv_sensorValue = (TextView) view.findViewById(R.id.simple_sensor_value);
        mSlidingLayer = (SlidingLayer) view.findViewById(R.id.slidingLayer1);

        playBtn = (FloatingActionButton) view.findViewById(R.id.simple_sensor_btn);
        openSliderBtn = (FloatingActionButton) view.findViewById(R.id.simple_sensor_sliderBtn);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals("START")) {

                    playBtn.setTag("STOP");
                    sensorManager.unregisterListener(sensorEventListener);


                    /*
                     * Stop The Service First... Then unbind.
                     * Because if a stopped service still has ServiceConnection objects bound to it with the BIND_AUTO_CREATE set,
                     * it will not be destroyed until all of these bindings are removed.
                     */
                    getActivity().stopService(intent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isServiceConnected) {
                                getActivity().unbindService(serviceConnection);
                                isServiceConnected = false;
                            }
                            playBtn.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }, 500);

                } else {

                    playBtn.setTag("START");
                    sensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

                    getActivity().startService(intent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isServiceConnected) {
                                getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                                isServiceConnected = true;
                            }
                            playBtn.setImageResource(android.R.drawable.ic_media_pause);
                        }
                    }, 500);

                }
            }
        });

        openSliderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSlidingLayer.isOpened()) {
                    mSlidingLayer.closeLayer(true);
                    openSliderBtn.setImageResource(R.drawable.map);
                    openSliderBtn.setBackgroundTintList(getResources().getColorStateList(R.color.colorSliderClosed));
                } else {
                    mSlidingLayer.openLayer(true);
                    openSliderBtn.setImageResource(R.drawable.close);
                    openSliderBtn.setBackgroundTintList(getResources().getColorStateList(R.color.colorSliderOpened));
                }
            }
        });


        if (mSensor != null) {

            if (mSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                tv_sensorType.setText("Using ACCELEROMETER");
            } else {
                tv_sensorType.setText("Using STEP_COUNTER");
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem mn0 = menu.add(0, 0, 0, "LOGS");
        mn0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mn0.setIcon(android.R.drawable.ic_menu_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == 0) {
            new Thread() {

                @Override
                public void run() {
                    final String txt = mService.getLogData();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(getActivity()).setMessage(txt).setPositiveButton("Okay", null).show();
                        }
                    });
                }
            }.start();


        }

        return true;

    }


}
