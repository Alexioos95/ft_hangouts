package com.apayen.ft_hangouts;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.content.SharedPreferences;

public class SettingsFragment extends Fragment
{
    //////////////////////////////
    // Creation
    //////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    { super.onCreate(savedInstanceState); }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    { return (inflater.inflate(R.layout.fragment_settings, container, false)); }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // Seekbars
        SeekBar seekBarRed = view.findViewById(R.id.settings_seekBarRed);
        SeekBar seekBarGreen = view.findViewById(R.id.settings_seekBarGreen);
        SeekBar seekBarBlue = view.findViewById(R.id.settings_seekBarBlue);
        // Restore previous state
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        int[] rgb = Utils.getColorFromStorage(requireContext());
        SharedPreferences preferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        seekBarRed.setProgress(rgb[0]);
        seekBarGreen.setProgress(rgb[1]);
        seekBarBlue.setProgress(rgb[2]);
        Utils.updateHeaderColor(activity, preferences, rgb);
        // Event Listener for colors
        SeekBar.OnSeekBarChangeListener colorChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                int red = seekBarRed.getProgress();
                int green = seekBarGreen.getProgress();
                int blue = seekBarBlue.getProgress();

                Utils.updateHeaderColor(activity, preferences, new int[]{red, green, blue});
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        };
        seekBarRed.setOnSeekBarChangeListener(colorChangeListener);
        seekBarGreen.setOnSeekBarChangeListener(colorChangeListener);
        seekBarBlue.setOnSeekBarChangeListener(colorChangeListener);
    }
}