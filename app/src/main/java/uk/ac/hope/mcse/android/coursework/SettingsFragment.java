package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.navigation.fragment.NavHostFragment;

public class SettingsFragment extends Fragment {

    private static final int FADE_DURATION = 300; // milliseconds
    private Handler mainHandler;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "EventDetails";
    private static final String KEY_EVENTS = "all_events_json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());
        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Dark mode switch setup
        SwitchCompat darkModeSwitch = view.findViewById(R.id.switch_dark_mode);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference first
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            // Apply the new night mode
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            // Create fade out animation
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(FADE_DURATION);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Recreate the activity after fade out
                    requireActivity().recreate();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            // Start fade out animation
            requireActivity().getWindow().getDecorView().startAnimation(fadeOut);
        });

        // Notifications switch setup
        SwitchMaterial notificationsSwitch = view.findViewById(R.id.switch_notifications);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        notificationsSwitch.setChecked(notificationsEnabled);

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            
            // Show toast message
            String message = isChecked ? "Notifications enabled" : "Notifications disabled";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        // Clear Events button setup
        MaterialButton clearEventsButton = view.findViewById(R.id.button_clear_events);
        clearEventsButton.setOnClickListener(v -> showClearEventsConfirmationDialog());

        // Privacy Policy button setup
        MaterialButton privacyPolicyButton = view.findViewById(R.id.button_privacy_policy);
        privacyPolicyButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SettingsFragment.this)
                .navigate(R.id.action_SettingsFragment_to_PrivacyPolicyFragment);
        });

        return view;
    }

    private void showClearEventsConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete All Events?")
            .setMessage("Are you sure you want to permanently delete all saved events?")
            .setPositiveButton("Delete", (dialog, which) -> clearAllEvents())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void clearAllEvents() {
        SharedPreferences eventPrefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        eventPrefs.edit()
            .putString(KEY_EVENTS, "[]")
            .apply();
        
        Toast.makeText(requireContext(), "All events have been deleted.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure the switch state matches the current theme when returning to the fragment
        SwitchCompat darkModeSwitch = requireView().findViewById(R.id.switch_dark_mode);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDarkMode);
    }
} 