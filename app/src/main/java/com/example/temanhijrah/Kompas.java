package com.example.temanhijrah;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.temanhijrah.jadwalModel.ApiClient;
import com.example.temanhijrah.jadwalModel.ApiInterface;
import com.example.temanhijrah.jadwalModel.Items;
import com.example.temanhijrah.jadwalModel.Jadwal;
import com.example.temanhijrah.kiblatModel.ApiKiblatClient;
import com.example.temanhijrah.kiblatModel.ApiKiblatInterface;
import com.example.temanhijrah.kiblatModel.Item;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Kompas extends AppCompatActivity implements SensorEventListener {
    ArrayList<PendingIntent> intentArray = new ArrayList<PendingIntent>();
    private final int REQUEST_ZUHUR = 0;
    private final int REQUEST_ASHR = 1;
    private final int REQUEST_MAGHRIB = 2;
    private final int REQUEST_ISHA = 3;
    private final int REQUEST_IMSAK = 4;
    private final int REQUEST_FAJR = 5;


    private TextView displayDate;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private FusedLocationProviderClient fusedLocationClient;
    private SensorManager sensorManager;
    private Double kiblatDegree;
    private float DegreeStart = 0f;
    private LinearLayout compass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kompas);

        displayDate = findViewById(R.id.button_date);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String dd = day / 10 == 0 ? "0" + day : String.valueOf(day);
        String mm = (month + 1) / 10 == 0 ? "0" + (month + 1) : String.valueOf(month + 1);
        String yyyy = String.valueOf(year);
        displayDate.setText(dd + "-" + mm + "-" + yyyy);

        displayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        Kompas.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        onDateSetListener,
                        year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String dd = dayOfMonth / 10 == 0 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                String mm = (month + 1) / 10 == 0 ? "0" + month : String.valueOf(month + 1);
                String yyyy = String.valueOf(year);
                String date = dd + "-" + mm + "-" + yyyy;
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Calendar c = Calendar.getInstance();
                try {
                    c.setTime(sdf.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                displayDate.setText(date);
                getJadwal(c.getTime());
            }
        };

        compass = findViewById(R.id.compass_pointer);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        getJadwal(Calendar.getInstance().getTime());

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String id = sharedPref.getString("id", "uname");
        String email = sharedPref.getString("email", "example@google.com");
        String accessToken = sharedPref.getString("accessToken", "default");
        String password = sharedPref.getString("password", "password");
        Log.i("email", email);
        Log.i("password", password);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    private void getJadwal(final Date c) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //
            //TOD: Consider calling
            //ActivityCompat#requestPermissions
            //here to request the missing permissions, and then overriding
            //public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //int[] grantResults)
            //to handle the case where the user grants the permission. See the documentation
            //for ActivityCompat#requestPermissions for more details.
            //
            Log.e("Error", "Permission not granted");
            return;
        }
        final double[] currentLat = new double[1];
        final double[] currentLong = new double[1];
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.i("Location", location.toString());
                            currentLat[0] = location.getLatitude();
                            currentLong[0] = location.getLongitude();
                            try {

                                Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                                Log.i("Latitude", String.valueOf(currentLat[0]));
                                Log.i("Longitude", String.valueOf(currentLong[0]));
                                final List<Address> addresses = geo.getFromLocation(currentLat[0], currentLong[0], 1);
                                Log.i("Address", addresses.get(0).getSubAdminArea());
                                Log.i("Address", addresses.get(0).getAdminArea());

                                final String loc = addresses.get(0).getSubAdminArea();

                                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                                Call<Items> call = apiInterface.getJadwalSholat(loc, c.toString());
                                call.enqueue(new Callback<Items>() {
                                    @Override
                                    public void onResponse(Call<Items> call, Response<Items> response) {
                                        Log.d("Data ", " respon" + response.toString());
                                        Jadwal jadwal = response.body().getData();
                                        Log.d("respon data ", "" + new Gson().toJson(jadwal));

                                        if (jadwal != null) {
                                            String zuhur = jadwal.getDhuhr();
                                            setAlarm(convStrToDate(zuhur), REQUEST_ZUHUR);
                                            String ashar = jadwal.getAsr();
                                            setAlarm(convStrToDate(ashar), REQUEST_ASHR);
                                            String magrib = jadwal.getMaghrib();
                                            setAlarm(convStrToDate(magrib), REQUEST_MAGHRIB);
                                            String isya = jadwal.getIsha();
                                            setAlarm(convStrToDate(isya), REQUEST_ISHA);
                                            String subuh = jadwal.getFajr();
                                            setAlarm(convStrToDate(subuh), REQUEST_FAJR);
                                            String imsak = jadwal.getImsak();
                                            setAlarm(convStrToDate(imsak), REQUEST_IMSAK);
                                            Log.d("respon :", "" + zuhur);
                                            TextView txtDzuhur = findViewById(R.id.time_dzuhur);
                                            txtDzuhur.setText(zuhur);
                                            TextView txtAshar = findViewById(R.id.time_ashar);
                                            txtAshar.setText(ashar);
                                            TextView txtMaghrib = findViewById(R.id.time_maghrib);
                                            txtMaghrib.setText(magrib);
                                            TextView txtIsya = findViewById(R.id.time_isya);
                                            txtIsya.setText(isya);
                                            TextView txtSubuh = findViewById(R.id.time_subuh);
                                            txtSubuh.setText(subuh);
                                            TextView txtImsak = findViewById(R.id.time_imsak);
                                            txtImsak.setText(imsak);

                                            Log.d("", "lokasi" + loc);
                                        }
// loadData(jadwal);
                                    }

                                    @Override
                                    public void onFailure(Call<Items> call, Throwable t) {
                                        Log.d("Data ", "" + t.getMessage());
                                    }
                                });

                                ApiKiblatInterface apiKiblatInterface = ApiKiblatClient.getClient().create(ApiKiblatInterface.class);
                                Call<Item> getKiblat = apiKiblatInterface.getKiblatPosition(String.valueOf(currentLat[0]), String.valueOf(currentLong[0]));
                                getKiblat.enqueue((new Callback<Item>() {
                                    @Override
                                    public void onResponse(Call<Item> call, Response<Item> response) {
                                        Log.d("Data ", " respon" + response.toString());
                                        Log.d("Data ", " respon" + response.body().getItem());
                                        LinkedTreeMap kiblat = response.body().getItem();
                                        Log.d("respon Data ", "" + new Gson().toJson(kiblat.get("derajat")));

                                        kiblatDegree = (Double) kiblat.get("derajat");
                                    }

                                    @Override
                                    public void onFailure(Call<Item> call, Throwable t) {
                                        Log.d("DataError ", "" + t.getMessage());
                                    }
                                }));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.i("Location", "null");
                        }
                    }
                });
    }

    private Calendar convStrToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(strDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        RotateAnimation rotateAnimation = new RotateAnimation(DegreeStart, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setFillAfter(true);

        rotateAnimation.setDuration(210);

        compass.startAnimation(rotateAnimation);
        if (kiblatDegree != null) DegreeStart = kiblatDegree.floatValue() - degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void getPrevSchedule(View view) {
        TextView dateView = (TextView) findViewById(R.id.button_date);
        String date = dateView.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, -1);
        String prevDate = sdf.format(c.getTime());
        dateView.setText(prevDate);
        Log.i("Date", prevDate);
        getJadwal(c.getTime());
    }

    public void getNextSchedule(View view) {
        TextView dateView = (TextView) findViewById(R.id.button_date);
        String date = dateView.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, 1);
        String prevDate = sdf.format(c.getTime());
        dateView.setText(prevDate);
        Log.i("Date", prevDate);
        getJadwal(c.getTime());
    }

    public void setAlarm(Calendar c, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 0);
        intentArray.add(pendingIntent);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, c.getTime().getHours());
        cal.set(Calendar.MINUTE, c.getTime().getMinutes());

        long diff = Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis();
        if (diff < 0) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), 24 * 60 * 60 * 1000,
                    intentArray.get(intentArray.indexOf(pendingIntent)));
            Log.i("Next Alarm will be in:", String.valueOf(cal.getTime()));
        }
    }

    public void setAlarm(View view) {
        String waktu = "";
        int viewText = view.getId();
        switch (viewText) {
            case R.id.alarm_imsak:
                waktu = "Imsak";
                break;
            case R.id.alarm_subuh:
                waktu = "Subuh";
                break;
            case R.id.alarm_zuhur:
                waktu = "Dzuhur";
                break;
            case R.id.alarm_ashar:
                waktu = "Ashar";
                break;
            case R.id.alarm_maghrib:
                waktu = "Maghrib";
                break;
            case R.id.alarm_isya:
                waktu = "Isya";
                break;
        }
        createDialog(waktu);
    }

    public void createDialog(String waktu) {
        DialogFragment dialogFragment = new AlarmFragment(waktu);
        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), "Adzan");
    }
}
