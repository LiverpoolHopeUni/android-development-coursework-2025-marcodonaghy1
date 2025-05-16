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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentCompletedEventsBinding;

public class CompletedEventsFragment extends Fragment implements EventAdapter.OnEventUpdatedListener {
    private FragmentCompletedEventsBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EventAdapter eventAdapter;
    private Context applicationContext;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "EventDetails";
    private static final String KEY_EVENTS = "all_events_json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = requireContext().getApplicationContext();
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        loadCompletedEventsFromPrefs();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(new ArrayList<>(), requireContext(), null);
        eventAdapter.setOnEventUpdatedListener(this);
        binding.recyclerViewCompletedEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCompletedEvents.setAdapter(eventAdapter);
    }

    private void loadCompletedEventsFromPrefs() {
        Log.d("CompletedEventsFragment", "=== LOADING COMPLETED EVENTS ===");
        executorService.execute(() -> {
            try {
                String json = prefs.getString(KEY_EVENTS_JSON, "[]");
                Log.d("CompletedEventsFragment", "Raw JSON from SharedPreferences: [" + json + "]");
                
                List<Event> allEvents = new Gson().fromJson(
                    json, new TypeToken<List<Event>>(){}.getType()
                );
                
                // Filter for completed events
                List<Event> completedEvents = new ArrayList<>();
                for (Event event : allEvents) {
                    if (event.isCompleted()) {
                        completedEvents.add(event);
                        Log.d("CompletedEventsFragment", "Added completed event: " + event.getName() + 
                            " on " + event.getDate() + 
                            " at " + event.getTime() + 
                            " Priority: " + event.getPriority());
                    }
                }
                
                Log.d("CompletedEventsFragment", "Final parsed completed events count: " + completedEvents.size());
                
                mainHandler.post(() -> {
                    if (isAdded()) { // Check if fragment is still attached
                        eventAdapter.updateEvents(completedEvents);
                        if (completedEvents.isEmpty()) {
                            Toast.makeText(applicationContext, "No completed events found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(applicationContext, "Loaded " + completedEvents.size() + " completed events", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("CompletedEventsFragment", "Error loading completed events", e);
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(applicationContext, "Error loading completed events", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onEventUpdated() {
        // Refresh the list when an event is updated
        loadCompletedEventsFromPrefs();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCompletedEventsFromPrefs();
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