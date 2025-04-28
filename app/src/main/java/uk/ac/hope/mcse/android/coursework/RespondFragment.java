package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import uk.ac.hope.mcse.android.coursework.R;

public class RespondFragment extends Fragment {
    private static final String PREF_NAME = "EventResponsePrefs";
    private static final String KEY_AVAILABILITY = "friend_response";
    private RadioGroup radioGroup;
    private SharedPreferences sharedPreferences;
    private Button submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_respond, container, false);
        
        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Initialize RadioGroup
        radioGroup = view.findViewById(R.id.availability_radio_group);
        
        // Initialize Submit Button
        submitButton = view.findViewById(R.id.button_submit_response);
        
        // Set up submit button click listener
        submitButton.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = view.findViewById(selectedId);
            String response = selectedRadioButton.getText().toString();
            
            // Save the response to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_AVAILABILITY, response);
            editor.apply();
            
            Toast.makeText(getContext(), "Response saved!", Toast.LENGTH_SHORT).show();
        });
        
        // Load previously saved response if any
        String savedResponse = sharedPreferences.getString(KEY_AVAILABILITY, "");
        if (!savedResponse.isEmpty()) {
            int radioId = -1;
            switch (savedResponse) {
                case "Yes":
                    radioId = R.id.radio_yes;
                    break;
                case "No":
                    radioId = R.id.radio_no;
                    break;
                case "Maybe":
                    radioId = R.id.radio_maybe;
                    break;
            }
            if (radioId != -1) {
                radioGroup.check(radioId);
            }
        }
        
        return view;
    }
} 