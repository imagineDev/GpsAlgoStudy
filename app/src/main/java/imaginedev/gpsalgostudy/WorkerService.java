package imaginedev.gpsalgostudy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkerService extends Service {

    IBinder mBinder = new MyBinder();
    File file;
    FileWriter fileWriter;
    LocationListener locationListener;
    LocationManager locationManager;

    // two minutes
    long TIME_THRESHOLD = 2000 * 60 * 60;

    Location currentLocation;
    Location currentBestLocation;

    public WorkerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (location != null) {
                    try {
                        openFileConnection();
                        saveToFile(logGenerator(location));
                    } finally {
                        closeFileConnection();
                    }
                } else {
                    try {
                        openFileConnection();
                        saveToFile(logErrorGenerator("No Location"));
                    } finally {
                        closeFileConnection();
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        generateNotification();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        return START_STICKY;
    }

    private void generateNotification() {
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 100, new Intent("imaginedev.gpsalgostudy.STOP"), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setContentTitle("Gps Algo Study");
        notification.setContentText("RUNNING...");
        notification.setPriority(Notification.PRIORITY_MAX);
        notification.addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP SERVICE", pi);

        startForeground(200, notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public void openFileConnection() {
        try {
            file = new File(Environment.getExternalStorageDirectory() + "/COLLECTED_LOGS.txt");
            fileWriter = new FileWriter(file, true);

            Log.d("MYTAG", file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile(String txt) {
        try {
            fileWriter.write(txt);
            Log.d("MYTAG", "Log Generated !!!!");
            Log.d("MTAG", ">>>" + txt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFileConnection() {
        try {
            if (fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String logGenerator(Location loc) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n**************************");
        sb.append("\nDate and Time : " + new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss").format(new Date()));
        sb.append("\nLongitude : " + loc.getLongitude());
        sb.append("\nLatitude : " + loc.getLatitude());
        sb.append("\nAltitude : " + loc.getAltitude());
        sb.append("\nAccuracy : " + loc.getAccuracy());
        sb.append("\nLocation Provider : " + loc.getProvider());
        sb.append("\n**************************");

        return sb.toString();
    }

    public String logErrorGenerator(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n**************************");
        sb.append("\nDate and Time : " + new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss").format(new Date()));
        sb.append("\nMessage : " + message);
        sb.append("\n**************************");

        return sb.toString();
    }

    public String getLogData() {
        FileReader fileReader = null;
        StringBuilder sb = new StringBuilder();
        String tmp = "";

        try {
            file = new File(Environment.getExternalStorageDirectory() + "/COLLECTED_LOGS.txt");
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            while ((tmp = br.readLine()) != null) {
                sb.append(tmp);
            }
            if (fileReader != null) {
                fileReader.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return sb.toString();
    }

    public class MyBinder extends Binder {

        public WorkerService getService() {
            return WorkerService.this;
        }

    }


  /*  public void watchLocationSmartly()
    {

    }

    *//* return : if true = use currentLocation   else = use currentBestLocation *//*
    public boolean rocketScienceOnLocation()
    {
          if(currentBestLocation == null)
          {
              currentBestLocation = currentLocation;
              return false;
          }

        long time_delta = currentLocation.getTime() - currentBestLocation.getTime();
        int accuracy_delta = (int)(currentLocation.getAccuracy() - currentBestLocation.getAccuracy());
    }*/


}
