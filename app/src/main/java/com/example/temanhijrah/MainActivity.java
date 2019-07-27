package com.example.temanhijrah;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.temanhijrah.userModel.ApiUserClient;
import com.example.temanhijrah.userModel.ApiUserInterface;
import com.example.temanhijrah.userModel.Result;
import com.example.temanhijrah.userModel.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        setContentView(R.layout.activity_main);
    }

    private void autologin(String id, String email, String accessToken, String password) {
        Log.i("email", email);
        Log.i("password", password);
        ApiUserInterface apiUserInterface = ApiUserClient.getClient(getResources().getString(R.string.url_api_main)).create(ApiUserInterface.class);
        final Call<User> u = apiUserInterface.login(email, password);
        u.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("Data ", " respon" + response.raw().toString());
                User user = response.body();
                if (response.code() == 200) {
                    Result result = user.getResult();
                    String id = result.getId();
                    String name = result.getFirstName() + " " + result.getLastName();
                    String accessToken = result.getAccessToken();
                    String email = result.getUsername();
                    String password = result.getPassword();

                    launchBeranda(id, email, password, name, accessToken);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("DataError ", "" + t.getMessage());
            }
        });
    }

    public void launchBeranda(String id, String email, String password, String name, String accessToken) {

        Intent intent = new Intent(this, Beranda.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("name", name);
        bundle.putString("accessToken", accessToken);
        bundle.putString("email", email);
        bundle.putString("password", password);
        intent.putExtras(bundle);
        FragmentMain fragmentMain = new FragmentMain();
        fragmentMain.setArguments(bundle);

        startActivity(intent);
        finish();
    }

    public void createNotificationChannel() {
        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        Intent ii = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("BIGTE");
        bigText.setBigContentTitle("Today's Bible Verse");
        bigText.setSummaryText("Text in detail");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_home_black_24dp);
        mBuilder.setContentTitle("Your Title");
        mBuilder.setContentText("Your text");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }

    public void launchRegister(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    public void launchLogin(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void launchBeranda(View view) {
        Intent intent = new Intent(this, Beranda.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String id = sharedPref.getString("id", "uname");
        String email = sharedPref.getString("email", "example@google.com");
        String accessToken = sharedPref.getString("accessToken", "default");
        String password = sharedPref.getString("password", "password");

        autologin(id, email, accessToken, password);
    }
}
