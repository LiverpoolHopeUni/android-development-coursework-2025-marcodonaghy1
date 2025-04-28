package uk.ac.hope.mcse.android.coursework;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;

/**
 *
 */
public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize SharedPreferences in onCreate
        sharedPreferences = requireActivity().getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up click listeners first for better responsiveness
        setupClickListeners();
        
        // Load event list in background
        loadEventList();
    }

    private void setupClickListeners() {
        binding.fab.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_AddEventFragment));

        binding.buttonRespondEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_FriendResponseFragment));
    }

    private void loadEventList() {
        // Run SharedPreferences operation in background
        new Thread(() -> {
            final String eventList = sharedPreferences.getString("event_list", "");
            
            // Update UI on main thread
            mainHandler.post(() -> {
                if (eventList.isEmpty()) {
                    binding.textviewFirst.setText("No events created yet.");
                } else {
                    binding.textviewFirst.setText(eventList);
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}