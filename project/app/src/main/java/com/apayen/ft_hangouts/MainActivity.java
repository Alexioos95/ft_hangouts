package com.apayen.ft_hangouts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.Menu;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity
{
    //////////////////////////////
    // Creation
    //////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return (insets);
        });
        // Clear previous time check
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().remove("inBackgroundFor").apply();
        // Enable action's bar
        setSupportActionBar(findViewById(R.id.my_toolbar));
        // Ask permissions for SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS}, 1);
        // Go to HomeFragment if there is no savedInstance
        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        // Set back colors
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Utils.updateHeaderColor(this, preferences, Utils.getColorFromStorage(getApplicationContext()));
        return (true);
    }
    //////////////////////////////
    // Permissions
    //////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        if (requestCode == 1)
        {
            SharedPreferences.Editor editor = preferences.edit();

			editor.putBoolean("sms_perms", (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED));
            editor.apply();
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof Utils.PermissionResultCallback)
        {
            if (preferences.getBoolean("sms_perms", false))
                ((Utils.PermissionResultCallback)fragment).onPermissionGranted();
            else
                ((Utils.PermissionResultCallback)fragment).onPermissionDenied();
        }
    }
    //////////////////////////////
    // Action bar's menu
    //////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_settings)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

            if (!(currentFragment instanceof SettingsFragment))
            {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack("")
                        .commit();
            }
            return (true);
        }
        return (super.onOptionsItemSelected(item));
    }
}