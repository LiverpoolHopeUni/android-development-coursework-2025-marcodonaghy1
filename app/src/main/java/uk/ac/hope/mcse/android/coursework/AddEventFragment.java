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

import uk.ac.hope.mcse.android.coursework.databinding.FragmentAddEventBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEventFragment extends Fragment {

    private FragmentAddEventBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonSelectDate.setOnClickListener(v -> showDatePicker());
        binding.buttonSelectTime.setOnClickListener(v -> showTimePicker());
        binding.buttonSaveEvent.setOnClickListener(v -> saveEventDetails());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                android.R.style.Theme_Material_Light_Dialog,
                null, // We'll handle the date setting in the Apply button
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set up Apply button
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Apply", (dialog, which) -> {
            DatePickerDialog picker = (DatePickerDialog) dialog;
            selectedDate.set(picker.getDatePicker().getYear(),
                           picker.getDatePicker().getMonth(),
                           picker.getDatePicker().getDayOfMonth());
            binding.buttonSelectDate.setText(dateFormat.format(selectedDate.getTime()));
        });

        // Set up Cancel button
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                R.style.CustomTimePickerDialog,
                (view, hourOfDay, minute) -> {
                    // This will be called when the time is set
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    binding.buttonSelectTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true
        );

        // Set up Cancel button
        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        timePickerDialog.show();
    }

    private void saveEventDetails() {
        String eventName = binding.editTextEventName.getText().toString().trim();
        String eventDate = binding.buttonSelectDate.getText().toString();
        String eventTime = binding.buttonSelectTime.getText().toString();

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

        // Create the new event line
        String newEvent = String.format("%s - %s - %s", eventName, eventDate, eventTime);

        // Save in background
        executorService.execute(() -> {
            // Load existing event list
            String existingEvents = sharedPreferences.getString("event_list", "");
            
            // Append new event to the list
            String updatedEventList;
            if (existingEvents.isEmpty()) {
                updatedEventList = newEvent;
            } else {
                updatedEventList = existingEvents + "\n" + newEvent;
            }

            // Save the updated event list
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("event_list", updatedEventList);
            editor.apply();

            // Update UI on main thread
            mainHandler.post(() -> {
                // Clear input fields
                binding.editTextEventName.setText("");
                binding.buttonSelectDate.setText(R.string.select_date_button_text);
                binding.buttonSelectTime.setText(R.string.select_time_button_text);

                Toast.makeText(requireContext(), "Event saved!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(AddEventFragment.this)
                        .navigate(R.id.action_AddEventFragment_to_FirstFragment);
            });
        });
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