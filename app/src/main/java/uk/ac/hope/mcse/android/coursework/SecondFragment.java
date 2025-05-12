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

public class SecondFragment extends Fragment {

    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_EVENT_LIST = "event_list";
    private static final String EVENT_SEPARATOR = "|||";
    private static final int ANIMATION_DURATION = 300;
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
    private MaterialButton buttonSelectDate;
    private MaterialButton buttonSelectTime;
    private TextInputEditText editTextEventName;
    private AutoCompleteTextView priorityDropdown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        animateViewsIn();

        // Initialize views
        editTextEventName = view.findViewById(R.id.editTextEventName);
        buttonSelectDate = view.findViewById(R.id.buttonSelectDate);
        buttonSelectTime = view.findViewById(R.id.buttonSelectTime);
        priorityDropdown = view.findViewById(R.id.spinnerPriority);

        // Set up priority dropdown
        String[] priorities = getResources().getStringArray(R.array.priority_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.dropdown_item,
            priorities
        );
        priorityDropdown.setAdapter(adapter);
        priorityDropdown.setText(priorities[0], false); // Set default value

        // Set initial date and time
        buttonSelectDate.setText(currentDate);
        buttonSelectTime.setText(currentTime);

        // Date picker
        binding.buttonSelectDate.setOnClickListener(v -> {
            animateButtonClick(v, this::showDatePicker);
        });

        // Time picker
        binding.buttonSelectTime.setOnClickListener(v -> {
            animateButtonClick(v, this::showTimePicker);
        });
    }

    private void animateViewsIn() {
        // Set initial states
        binding.eventNameLayout.setAlpha(0f);
        binding.buttonSelectDate.setAlpha(0f);
        binding.buttonSelectTime.setAlpha(0f);
        binding.priorityLayout.setAlpha(0f);
        binding.buttonSaveEvent.setAlpha(0f);

        // Create a single animation set
        binding.eventNameLayout.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> {
                    binding.buttonSelectDate.animate()
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION)
                            .withEndAction(() -> {
                                binding.buttonSelectTime.animate()
                                        .alpha(1f)
                                        .setDuration(ANIMATION_DURATION)
                                        .withEndAction(() -> {
                                            binding.priorityLayout.animate()
                                                    .alpha(1f)
                                                    .setDuration(ANIMATION_DURATION)
                                                    .withEndAction(() -> {
                                                        binding.buttonSaveEvent.animate()
                                                                .alpha(1f)
                                                                .setDuration(ANIMATION_DURATION)
                                                                .start();
                                                    })
                                                    .start();
                                        })
                                        .start();
                            })
                            .start();
                })
                .start();
    }

    private void setupClickListeners() {
        binding.buttonSelectDate.setOnClickListener(v -> {
            animateButtonClick(v, this::showDatePicker);
        });
        
        binding.buttonSelectTime.setOnClickListener(v -> {
            animateButtonClick(v, this::showTimePicker);
        });
        
        binding.buttonSaveEvent.setOnClickListener(v -> {
            animateButtonClick(v, this::saveEventDetails);
        });
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
            android.R.style.Theme_Holo_Light_Dialog,
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
            android.R.style.Theme_Holo_Light_Dialog,
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
        // Get input values
        String eventName = binding.editTextEventName.getText().toString().trim();
        String eventDate = binding.buttonSelectDate.getText().toString();
        String eventTime = binding.buttonSelectTime.getText().toString();
        String priority = ((AutoCompleteTextView) binding.spinnerPriority).getText().toString();

        // Debug logging
        Log.d("SecondFragment", "=== SAVING EVENT ===");
        Log.d("SecondFragment", "Event Name: [" + eventName + "]");
        Log.d("SecondFragment", "Event Date: [" + eventDate + "]");
        Log.d("SecondFragment", "Event Time: [" + eventTime + "]");
        Log.d("SecondFragment", "Priority: [" + priority + "]");

        // Validate inputs
        if (eventName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an event name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventDate.equals(getString(R.string.select_date_button_text))) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventTime.equals(getString(R.string.select_time_button_text))) {
            Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build event string with correct format
        String newEvent = eventName + "\n" + eventDate + "\n" + eventTime + "\n" + priority;
        Log.d("SecondFragment", "New Event String: [" + newEvent + "]");

        // Get existing events
        String existingEvents = sharedPreferences.getString(KEY_EVENT_LIST, "");
        Log.d("SecondFragment", "Existing Events: [" + existingEvents + "]");

        // Combine with new event using the correct separator
        String updatedEvents = existingEvents.isEmpty() ? newEvent : existingEvents + EVENT_SEPARATOR + newEvent;
        Log.d("SecondFragment", "Updated Events: [" + updatedEvents + "]");

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EVENT_LIST, updatedEvents);
        boolean success = editor.commit();
        Log.d("SecondFragment", "Save Success: " + success);

        // Schedule alarm for notification
        try {
            // Parse the date and time
            Calendar eventDateTime = Calendar.getInstance();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            eventDateTime.setTime(dateTimeFormat.parse(eventDate + " " + eventTime));

            // Calculate trigger time (1 hour before the event)
            eventDateTime.add(Calendar.HOUR_OF_DAY, -1);
            long triggerTime = eventDateTime.getTimeInMillis();

            // Check if the trigger time is in the past
            if (triggerTime <= System.currentTimeMillis()) {
                Log.d("SecondFragment", "Event is less than 1 hour away, skipping alarm scheduling");
                return;
            }

            // Create intent for the broadcast receiver
            Intent intent = new Intent(requireContext(), EventReminderReceiver.class);
            intent.putExtra("event_name", eventName);
            intent.putExtra("event_time", eventTime);

            // Create pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                (int) System.currentTimeMillis(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get AlarmManager and schedule the alarm
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
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

        // Verify the save
        String savedEvents = sharedPreferences.getString(KEY_EVENT_LIST, "");
        Log.d("SecondFragment", "Verified Saved Events: [" + savedEvents + "]");

        if (success) {
            // Show success message
            Toast.makeText(requireContext(), "Event saved!", Toast.LENGTH_SHORT).show();

            // Clear form
            binding.editTextEventName.setText("");
            binding.buttonSelectDate.setText(R.string.select_date_button_text);
            binding.buttonSelectTime.setText(R.string.select_time_button_text);
            priorityDropdown.setText(getString(R.string.select_priority_button_text), false);

            // Navigate up in the back stack
            NavHostFragment.findNavController(SecondFragment.this).navigateUp();
        } else {
            Toast.makeText(requireContext(), "Failed to save event", Toast.LENGTH_SHORT).show();
        }
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
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}