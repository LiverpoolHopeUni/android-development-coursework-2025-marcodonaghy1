package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentCompletedEventsBinding;

public class CompletedEventsFragment extends Fragment implements EventAdapter.OnEventUpdatedListener {
    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_EVENT_LIST = "event_list";
    private static final String EVENT_SEPARATOR = "|||";
    
    private FragmentCompletedEventsBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedPreferences sharedPreferences;
    private EventAdapter eventAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCompletedEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadCompletedEvents();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(new ArrayList<>(), requireContext());
        eventAdapter.setOnEventUpdatedListener(this);
        binding.recyclerViewCompletedEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCompletedEvents.setAdapter(eventAdapter);
    }

    private void loadCompletedEvents() {
        Log.d("CompletedEventsFragment", "=== LOADING COMPLETED EVENTS ===");
        executorService.execute(() -> {
            try {
                // Retrieve the entire saved list of events
                String eventList = sharedPreferences.getString(KEY_EVENT_LIST, "");
                Log.d("CompletedEventsFragment", "Raw event list from SharedPreferences: [" + eventList + "]");
                
                List<Event> allEvents = new ArrayList<>();
                
                if (!eventList.isEmpty()) {
                    // Split the event list by the separator
                    String[] eventStrings = eventList.split("\\|\\|\\|");
                    Log.d("CompletedEventsFragment", "Split into " + eventStrings.length + " events");
                    
                    // Parse all events into Event objects
                    for (String eventString : eventStrings) {
                        if (!eventString.isEmpty()) {
                            String[] parts = eventString.split("\n");
                            if (parts.length >= 4) {
                                Event event = new Event(parts[0], parts[1], parts[2], parts[3]);
                                allEvents.add(event);
                                Log.d("CompletedEventsFragment", "Parsed event: " + event.getName());
                            }
                        }
                    }
                }
                
                // Filter for completed events
                List<Event> completedEvents = allEvents.stream()
                    .filter(event -> "Completed".equals(event.getPriority()))
                    .collect(Collectors.toList());
                
                Log.d("CompletedEventsFragment", "Filtered to " + completedEvents.size() + " completed events");
                
                // Update UI on main thread
                mainHandler.post(() -> {
                    // Update the existing adapter with the filtered list
                    if (eventAdapter != null) {
                        eventAdapter.setEvents(completedEvents);
                        
                        if (completedEvents.isEmpty()) {
                            showEmptyState();
                        } else {
                            Toast.makeText(requireContext(), 
                                "Loaded " + completedEvents.size() + " completed events", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("CompletedEventsFragment", "Event adapter is null");
                    }
                });
            } catch (Exception e) {
                Log.e("CompletedEventsFragment", "Error loading completed events", e);
                mainHandler.post(() -> {
                    Toast.makeText(requireContext(), 
                        "Error loading completed events: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showEmptyState() {
        Toast.makeText(requireContext(), "No completed events found", Toast.LENGTH_SHORT).show();
        // Navigate back to FirstFragment
        NavHostFragment.findNavController(this).navigateUp();
    }

    @Override
    public void onEventUpdated() {
        // Refresh the list when an event is updated
        loadCompletedEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCompletedEvents();
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