package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentCompletedEventsBinding;

public class CompletedEventsFragment extends Fragment implements EventAdapter.OnEventStatusChangedListener {
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
        sharedPreferences = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        loadEvents();
        setupSwipeToDelete();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(new ArrayList<>(), requireContext(), this);
        binding.recyclerViewCompletedEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCompletedEvents.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        executorService.execute(() -> {
            try {
                String json = sharedPreferences.getString(KEY_EVENTS, "[]");
                Log.d("CompletedEventsFragment", "Loading events from JSON: " + json);
                
                List<Event> allEvents = new Gson().fromJson(
                    json, new TypeToken<List<Event>>(){}.getType()
                );
                
                // Filter and sort completed events
                List<Event> completedEvents = allEvents.stream()
                    .filter(event -> "Completed".equals(event.getPriority()))
                    .sorted((e1, e2) -> {
                        int dateCompare = e2.getDate().compareTo(e1.getDate());
                        return dateCompare != 0 ? dateCompare : e2.getTime().compareTo(e1.getTime());
                    })
                    .collect(Collectors.toList());
                
                Log.d("CompletedEventsFragment", "Found " + completedEvents.size() + " completed events");
                
                mainHandler.post(() -> {
                    if (isAdded()) {
                        eventAdapter.setEvents(completedEvents);
                        String message = completedEvents.isEmpty() ? 
                            "No completed events" : 
                            "Loaded " + completedEvents.size() + " completed events";
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("CompletedEventsFragment", "Error loading events", e);
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(applicationContext, "Error loading events", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onEventStatusChanged() {
        Log.d("CompletedEventsFragment", "Event status changed, reloading events");
        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CompletedEventsFragment", "Fragment resumed, reloading events");
        loadEvents();
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

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private Drawable deleteIcon;
            private Paint paint;

            {
                deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check);
                paint = new Paint();
                paint.setColor(ContextCompat.getColor(requireContext(), R.color.green));
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Event event = eventAdapter.getEvents().get(position);
                removeEvent(event);
                Toast.makeText(requireContext(), "Completed event removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                if (dX < 0) { // Swiping to the left
                    // Draw green background
                    paint.setColor(ContextCompat.getColor(requireContext(), R.color.green));
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);

                    // Draw check icon
                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        deleteIcon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerViewCompletedEvents);
    }

    private void removeEvent(Event event) {
        executorService.execute(() -> {
            try {
                // Remove from SharedPreferences
                String json = sharedPreferences.getString(KEY_EVENTS, "[]");
                List<Event> allEvents = new Gson().fromJson(json, new TypeToken<List<Event>>(){}.getType());
                
                // Find and remove the event
                allEvents.removeIf(e -> 
                    e.getName().equals(event.getName()) && 
                    e.getDate().equals(event.getDate()) && 
                    e.getTime().equals(event.getTime())
                );
                
                // Save updated list
                sharedPreferences.edit()
                    .putString(KEY_EVENTS, new Gson().toJson(allEvents))
                    .apply();

                // Update UI on main thread
                mainHandler.post(() -> {
                    if (isAdded()) {
                        // Remove from adapter
                        List<Event> currentEvents = new ArrayList<>(eventAdapter.getEvents());
                        currentEvents.removeIf(e -> 
                            e.getName().equals(event.getName()) && 
                            e.getDate().equals(event.getDate()) && 
                            e.getTime().equals(event.getTime())
                        );
                        eventAdapter.setEvents(currentEvents);
                        
                        // Show success message
                        Toast.makeText(applicationContext, "Completed event removed", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("CompletedEventsFragment", "Error removing event", e);
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(applicationContext, "Error removing event", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
} 