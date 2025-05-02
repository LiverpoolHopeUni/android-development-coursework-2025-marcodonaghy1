package uk.ac.hope.mcse.android.coursework;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedPreferences sharedPreferences;
    private MaterialButton buttonSelectDate;
    private MaterialButton buttonSelectTime;
    private TextInputEditText editTextEventName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        selectedDate = Calendar.getInstance();
        selectedTime = Calendar.getInstance();
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

        // Set initial date and time
        updateDateButtonText();
        updateTimeButtonText();
    }

    private void animateViewsIn() {
        // Set initial states
        binding.eventNameLayout.setAlpha(0f);
        binding.buttonSelectDate.setAlpha(0f);
        binding.buttonSelectTime.setAlpha(0f);
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                R.style.CustomTimePickerDialog,
                null,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Apply", (dialog, which) -> {
            DatePickerDialog picker = (DatePickerDialog) dialog;
            selectedDate.set(picker.getDatePicker().getYear(),
                           picker.getDatePicker().getMonth(),
                           picker.getDatePicker().getDayOfMonth());
            updateDateButtonText();
            binding.buttonSelectDate.setIconTintResource(R.color.black);
        });

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                R.style.CustomTimePickerDialog,
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    updateTimeButtonText();
                    binding.buttonSelectTime.setIconTintResource(R.color.black);
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        timePickerDialog.show();
    }

    private void saveEventDetails() {
        // Get input values
        String eventName = editTextEventName.getText().toString().trim();
        String eventDate = buttonSelectDate.getText().toString();
        String eventTime = buttonSelectTime.getText().toString();

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

        // Build event string
        String newEvent = eventName + " – " + eventDate + " – " + eventTime;

        // Get existing events
        String existingEvents = sharedPreferences.getString("event_list", "");

        // Combine with new event
        String updatedEvents = existingEvents.isEmpty() ? newEvent : existingEvents + "\n" + newEvent;

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("event_list", updatedEvents);
        editor.apply();

        // Show success message
        Toast.makeText(requireContext(), "Event saved!", Toast.LENGTH_SHORT).show();

        // Clear form
        editTextEventName.setText("");
        buttonSelectDate.setText(R.string.select_date_button_text);
        buttonSelectTime.setText(R.string.select_time_button_text);

        // Navigate back
        NavHostFragment.findNavController(SecondFragment.this).navigateUp();
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

    private void updateDateButtonText() {
        buttonSelectDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void updateTimeButtonText() {
        buttonSelectTime.setText(timeFormat.format(selectedTime.getTime()));
    }
}