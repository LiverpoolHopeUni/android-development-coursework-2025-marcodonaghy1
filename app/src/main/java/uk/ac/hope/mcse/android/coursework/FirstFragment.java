package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uk.ac.hope.mcse.android.coursework.databinding.FirstFragmentLayoutBinding;

/**
 *
 */
public class FirstFragment extends Fragment {

    private FirstFragmentLayoutBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FirstFragmentLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve event details from Shared Preferences
        SharedPreferences sharedPreferences =  requireContext().getSharedPreferences("EventDetails", Context.MODE_PRIVATE);
        String eventName = sharedPreferences.getString("EventName", "No Event");
        String eventDate = sharedPreferences.getString("EventDate", "No Date");
        String eventTime = sharedPreferences.getString("EventTime", "No Time");

        //Format retrieved data to display in TextView
        String displayText = "Event Name: " + eventName + "\n" +
                "Date: " + eventDate + "\n" +
                "Time: " + eventTime;

        // Set to formated TextView
        binding.textviewFirst.setText(displayText);

        // FloatingActionButton click listener to navigate to AddEventFragment
        binding.fab.setOnClickListener(v -> {
            // Navigate to AddEventFragment using the appropriate action
            NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_AddEventFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}