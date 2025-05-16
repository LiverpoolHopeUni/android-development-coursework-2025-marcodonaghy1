package uk.ac.hope.mcse.android.coursework;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    private static final String PREF_NAME = "EventDetails";
    private static final String KEY_EVENTS = "all_events_json";
    private static final int ANIMATION_DURATION = 400;
    private static final int ANIMATION_DELAY = 100;
    private static final int BUTTON_ANIMATION_DURATION = 100;
    
    private FragmentSecondBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    private final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedPreferences sharedPreferences;
    private Context applicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = requireContext().getApplicationContext();
        sharedPreferences = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        selectedDate = Calendar.getInstance();
        selectedTime = Calendar.getInstance();

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "event_channel",
                "Upcoming Events",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies you when your events are coming up");
            
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (binding == null) {
            Log.e("SecondFragment", "Binding is null in onViewCreated");
            return;
        }

        try {
            // Set up priority dropdown
            String[] priorities = getResources().getStringArray(R.array.priority_levels);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                priorities
            );
            binding.spinnerPriority.setAdapter(adapter);
            binding.spinnerPriority.setText(priorities[0], false); // Set default value

            // Set initial date and time
            binding.buttonSelectDate.setText(currentDate);
            binding.buttonSelectTime.setText(currentTime);

            setupClickListeners();
            
            // Post the animation to ensure views are laid out
            view.post(this::animateViewsIn);
        } catch (Exception e) {
            Log.e("SecondFragment", "Error in onViewCreated", e);
        }
    }

    private void animateViewsIn() {
        if (binding == null || !isAdded() || getView() == null) {
            Log.w("SecondFragment", "Cannot animate views: binding is null or fragment not attached");
            return;
        }

        try {
            // Set initial states
            binding.eventNameLayout.setAlpha(0f);
            binding.eventNameLayout.setTranslationY(50f);
            
            binding.buttonSelectDate.setAlpha(0f);
            binding.buttonSelectDate.setTranslationY(50f);
            
            binding.buttonSelectTime.setAlpha(0f);
            binding.buttonSelectTime.setTranslationY(50f);
            
            binding.priorityLayout.setAlpha(0f);
            binding.priorityLayout.setTranslationY(50f);
            
            binding.buttonSaveEvent.setAlpha(0f);
            binding.buttonSaveEvent.setTranslationY(50f);

            // Animate event name
            binding.eventNameLayout.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // Animate date button
            binding.buttonSelectDate.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(ANIMATION_DELAY)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // Animate time button
            binding.buttonSelectTime.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(ANIMATION_DELAY * 2)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // Animate priority layout
            binding.priorityLayout.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(ANIMATION_DELAY * 3)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // Animate save button
            binding.buttonSaveEvent.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .setStartDelay(ANIMATION_DELAY * 4)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        } catch (Exception e) {
            Log.e("SecondFragment", "Error during animation", e);
        }
    }

    private void setupClickListeners() {
        if (binding == null) return;
        
        try {
            binding.buttonSelectDate.setOnClickListener(v -> {
                if (binding != null && isAdded()) {
                    animateButtonClick(v, this::showDatePicker);
                }
            });
            
            binding.buttonSelectTime.setOnClickListener(v -> {
                if (binding != null && isAdded()) {
                    animateButtonClick(v, this::showTimePicker);
                }
            });
            
            binding.buttonSaveEvent.setOnClickListener(v -> {
                if (binding != null && isAdded()) {
                    animateButtonClick(v, this::saveEventDetails);
                }
            });
        } catch (Exception e) {
            Log.e("SecondFragment", "Error setting up click listeners", e);
        }
    }

    private void animateButtonClick(View v, Runnable action) {
        v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(BUTTON_ANIMATION_DURATION)
                .withEndAction(() -> {
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(BUTTON_ANIMATION_DURATION)
                            .withEndAction(action)
                            .start();
                })
                .start();
    }

    private void showDatePicker() {
        String[] dateParts = currentDate.split("/");
        int year = Integer.parseInt(dateParts[2]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int day = Integer.parseInt(dateParts[0]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                binding.buttonSelectDate.setText(formattedDate);
                binding.buttonSelectDate.setIconTintResource(R.color.black);
            },
            year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        String[] timeParts = currentTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, selectedHour, selectedMinute) -> {
                String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                binding.buttonSelectTime.setText(formattedTime);
                binding.buttonSelectTime.setIconTintResource(R.color.black);
            },
            hour, minute, true
        );
        timePickerDialog.show();
    }

    private void saveEventDetails() {
        if (binding == null) return;
        
        // Get input values
        String eventName = binding.editTextEventName.getText().toString().trim();
        String eventDate = binding.buttonSelectDate.getText().toString();
        String eventTime = binding.buttonSelectTime.getText().toString();
        String priority = binding.spinnerPriority.getText().toString();

        // Debug logging
        Log.d("SecondFragment", "=== SAVING EVENT ===");
        Log.d("SecondFragment", "Event Name: [" + eventName + "]");
        Log.d("SecondFragment", "Event Date: [" + eventDate + "]");
        Log.d("SecondFragment", "Event Time: [" + eventTime + "]");
        Log.d("SecondFragment", "Priority: [" + priority + "]");

        // Validate inputs
        if (eventName.isEmpty()) {
            binding.editTextEventName.setError("Event name cannot be empty");
            return;
        }

        if (eventDate.equals("Select Date")) {
            Toast.makeText(applicationContext, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventTime.equals("Select Time")) {
            Toast.makeText(applicationContext, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new event
        Event newEvent = new Event(eventName, eventDate, eventTime, priority);

        // Save to SharedPreferences using JSON
        executorService.execute(() -> {
            try {
                // Read existing events
                String json = sharedPreferences.getString(KEY_EVENTS, "[]");
                List<Event> events = new Gson().fromJson(json, new TypeToken<List<Event>>(){}.getType());
                
                // Add new event
                events.add(newEvent);
                
                // Save updated list
                String updatedJson = new Gson().toJson(events);
                sharedPreferences.edit()
                    .putString(KEY_EVENTS, updatedJson)
                    .apply();

                // Schedule notification
                scheduleNotification(newEvent);

                // Show success message and navigate back
                mainHandler.post(() -> {
                    Toast.makeText(applicationContext, "Event saved successfully", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
                });
            } catch (Exception e) {
                Log.e("SecondFragment", "Error saving event", e);
                mainHandler.post(() -> 
                    Toast.makeText(applicationContext, "Error saving event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showError(View view, String message) {
        if (view instanceof com.google.android.material.textfield.TextInputLayout) {
            ((com.google.android.material.textfield.TextInputLayout) view).setError(message);
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear all animations
        if (binding != null) {
            binding.eventNameLayout.clearAnimation();
            binding.buttonSelectDate.clearAnimation();
            binding.buttonSelectTime.clearAnimation();
            binding.priorityLayout.clearAnimation();
            binding.buttonSaveEvent.clearAnimation();
        }
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void scheduleNotification(Event event) {
        try {
            // Parse the date and time
            Calendar eventDateTime = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            eventDateTime.setTime(dateTimeFormat.parse(event.getDate() + " " + event.getTime()));

            // Calculate trigger time (1 hour before the event)
            eventDateTime.add(Calendar.HOUR_OF_DAY, -1);
            long triggerTime = eventDateTime.getTimeInMillis();

            // Check if the trigger time is in the past
            if (triggerTime <= System.currentTimeMillis()) {
                Log.d("SecondFragment", "Event is less than 1 hour away, skipping alarm scheduling");
                return;
            }

            // Create intent for the broadcast receiver
            Intent intent = new Intent(applicationContext, EventReminderReceiver.class);
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_time", event.getTime());

            // Create pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                (int) System.currentTimeMillis(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get AlarmManager and schedule the alarm
            AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    );
                }
                Log.d("SecondFragment", "Alarm scheduled for: " + new Date(triggerTime));
            }
        } catch (Exception e) {
            Log.e("SecondFragment", "Error scheduling alarm: " + e.getMessage());
        }
    }
}