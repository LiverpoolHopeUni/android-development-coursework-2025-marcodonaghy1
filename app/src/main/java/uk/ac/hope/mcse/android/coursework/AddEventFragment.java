package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import uk.ac.hope.mcse.android.coursework.databinding.FragmentAddEventBinding;

public class AddEventFragment extends Fragment {

    private FragmentAddEventBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Intialise Viewbinding
        binding = FragmentAddEventBinding.inflate(inflater, container, false);
        // Return root of the binding
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Save event button click listener
        binding.buttonSaveEvent.setOnClickListener(v -> saveEventDetails());


    }

    private void saveEventDetails() {
        // Retrieve event details from the UI as in Event, Date, Time
        String eventName = binding.editTextEventName.getText().toString().trim();
        String eventDate = binding.buttonSelectDate.getText().toString();
        String eventTime = binding.buttonSelectTime.getText().toString();

        // Check if all fields are filled
        if (eventName.isEmpty()) {
            binding.editTextEventName.requestFocus();
            Toast.makeText(requireContext(), "Event Name is required", Toast.LENGTH_SHORT).show();
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
        // Save to Shared Preferenences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("EventDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("eventName", eventName);
        editor.putString("eventDate", eventDate);
        editor.putString("eventTime", eventTime);
        editor.apply();

        //Show completion message to user
        Toast.makeText(getActivity(), "Event Details Saved Successfully!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the binding reference to prevent memory leaks
        binding = null;
    }
}