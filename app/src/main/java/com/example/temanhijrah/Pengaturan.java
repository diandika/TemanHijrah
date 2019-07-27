package com.example.temanhijrah;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Pengaturan extends AppCompatActivity {

    private String id;
    private String name;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        name = bundle.getString("name");
        accessToken = bundle.getString("accessToken");
    }

    public void LaunchEditProfile(View view) {
        Intent intent = new Intent(this, EditProfil.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("name", name);
        bundle.putString("accessToken", accessToken);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void launchUbahPassword(View view) {
        Intent intent = new Intent(this, UbahPassword.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("name", name);
        bundle.putString("accessToken", accessToken);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void launchAbout(View view) {
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    public void logout(View view) {
        SharedPreferences sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear().commit();
    }
}
