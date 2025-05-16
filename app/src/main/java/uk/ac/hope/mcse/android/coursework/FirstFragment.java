package uk.ac.hope.mcse.android.coursework;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 *
 */
public class FirstFragment extends Fragment implements EventAdapter.OnEventStatusChangedListener {

    private static final String PREF_NAME = "EventDetails";
    private static final String KEY_EVENTS = "all_events_json";
    
    private FragmentFirstBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedPreferences sharedPreferences;
    private EventAdapter eventAdapter;
    private List<Event> originalEvents = new ArrayList<>(); // Store original events for filtering
    private Context applicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = requireContext().getApplicationContext();
        sharedPreferences = applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSortingSpinner();
        setupSearchField();
        animateViewsIn();
        loadEvents();

        binding.fab.setOnClickListener(view1 -> {
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        });

        binding.fabCompleted.setOnClickListener(view1 -> {
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_CompletedEventsFragment);
        });
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(new ArrayList<>(), requireContext(), this);
        binding.recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewEvents.setAdapter(eventAdapter);

        // Add swipe to edit and delete functionality
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Event event = eventAdapter.getEventAt(position);
                
                if (event != null) {
                    if (direction == ItemTouchHelper.LEFT) {
                        // Delete event
                        deleteEvent(position);
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        // Edit event
                        showEditEventDialog(event, position);
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                
                if (dX > 0) {
                    // Swiping right - edit
                    paint.setColor(Color.BLUE);
                    c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);
                    
                    Drawable editIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit);
                    if (editIcon != null) {
                        int iconSize = 80;
                        int iconMargin = (itemView.getHeight() - iconSize) / 2;
                        editIcon.setBounds(
                            itemView.getLeft() + iconMargin,
                            itemView.getTop() + iconMargin,
                            itemView.getLeft() + iconSize + iconMargin,
                            itemView.getTop() + iconSize + iconMargin
                        );
                        editIcon.draw(c);
                    }
                } else if (dX < 0) {
                    // Swiping left - delete
                    paint.setColor(Color.RED);
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                    
                    Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                    if (deleteIcon != null) {
                        int iconSize = 80;
                        int iconMargin = (itemView.getHeight() - iconSize) / 2;
                        deleteIcon.setBounds(
                            itemView.getRight() - iconSize - iconMargin,
                            itemView.getTop() + iconMargin,
                            itemView.getRight() - iconMargin,
                            itemView.getTop() + iconSize + iconMargin
                        );
                        deleteIcon.draw(c);
                    }
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerViewEvents);
    }

    private void showEditEventDialog(Event event, int position) {
        // Create dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_event, null);
        EditText editName = dialogView.findViewById(R.id.editEventName);
        Button editButtonDate = dialogView.findViewById(R.id.editButtonDate);
        Button editButtonTime = dialogView.findViewById(R.id.editButtonTime);

        // Pre-fill with current values
        editName.setText(event.getName());
        
        // Parse current date and time
        String currentDate = event.getDate();
        String currentTime = event.getTime();
        
        // Set initial date and time
        editButtonDate.setText(currentDate);
        editButtonTime.setText(currentTime);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Edit Event")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", (dialogInterface, which) -> {
                eventAdapter.notifyItemChanged(position);
            })
            .setOnCancelListener(dialogInterface -> {
                eventAdapter.notifyItemChanged(position);
            })
            .create();

        // Set up date picker
        editButtonDate.setOnClickListener(v -> {
            // Parse current date
            String[] dateParts = currentDate.split("/");
            int year = Integer.parseInt(dateParts[2]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based
            int day = Integer.parseInt(dateParts[0]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as dd/MM/yyyy
                    String formattedDate = String.format("%02d/%02d/%04d", 
                        selectedDay, selectedMonth + 1, selectedYear);
                    editButtonDate.setText(formattedDate);
                },
                year, month, day
            );
            datePickerDialog.show();
        });

        // Set up time picker
        editButtonTime.setOnClickListener(v -> {
            // Parse current time
            String[] timeParts = currentTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Light_Dialog,
                (view, selectedHour, selectedMinute) -> {
                    // Format time as HH:mm
                    String formattedTime = String.format("%02d:%02d", 
                        selectedHour, selectedMinute);
                    editButtonTime.setText(formattedTime);
                },
                hour, minute, true // true for 24-hour format
            );
            timePickerDialog.show();
        });

        // Show dialog
        dialog.show();

        // Override the positive button click listener after dialog is shown
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Get raw input values
            String rawName = editName.getText().toString();
            String rawDate = editButtonDate.getText().toString();
            String rawTime = editButtonTime.getText().toString();

            // Log raw input values
            Log.d("EditEventDialog", "Raw input values:");
            Log.d("EditEventDialog", "Name: [" + rawName + "]");
            Log.d("EditEventDialog", "Date: [" + rawDate + "]");
            Log.d("EditEventDialog", "Time: [" + rawTime + "]");

            // Trim values for validation
            String name = rawName.trim();
            String date = rawDate.trim();
            String time = rawTime.trim();

            // Reset errors
            editName.setError(null);

            // Validate name - early return if invalid
            if (name.isEmpty()) {
                Log.d("EditEventDialog", "Validation failed: Name is empty");
                editName.setError("Event name cannot be empty");
                return;
            }

            // Validate date - early return if invalid
            if (date.equals("Select Date")) {
                Log.d("EditEventDialog", "Validation failed: Invalid date [" + date + "]");
                Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate time - early return if invalid
            if (time.equals("Select Time")) {
                Log.d("EditEventDialog", "Validation failed: Invalid time [" + time + "]");
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed
            Log.d("EditEventDialog", "All validations passed, updating event");
            
            // Create and update event
            Event updatedEvent = new Event(name, date, time, event.getPriority());
            eventAdapter.updateEventAt(position, updatedEvent);
            
            // Dismiss dialog only after successful update
            dialog.dismiss();
        });
    }

    private void animateViewsIn() {
        binding.fab.setAlpha(0f);
        binding.fab.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(200)
                .start();
    }

    private void setupSortingSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSortEvents.setAdapter(adapter);

        binding.spinnerSortEvents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOption = parent.getItemAtPosition(position).toString();
                sortEvents(sortOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void sortEvents(String sortOption) {
        List<Event> events = new ArrayList<>(eventAdapter.getEvents());
        switch (sortOption) {
            case "Most Recent First":
                events.sort((e1, e2) -> {
                    int dateCompare = e2.getDate().compareTo(e1.getDate());
                    return dateCompare != 0 ? dateCompare : e2.getTime().compareTo(e1.getTime());
                });
                break;
            case "Oldest First":
                events.sort((e1, e2) -> {
                    int dateCompare = e1.getDate().compareTo(e2.getDate());
                    return dateCompare != 0 ? dateCompare : e1.getTime().compareTo(e2.getTime());
                });
                break;
            case "By Priority":
                events.sort((e1, e2) -> {
                    int priority1 = getPriorityValue(e1.getPriority());
                    int priority2 = getPriorityValue(e2.getPriority());
                    return priority1 != priority2 ? 
                        Integer.compare(priority2, priority1) : 
                        e2.getDate().compareTo(e1.getDate());
                });
                break;
        }
        eventAdapter.setEvents(events);
    }

    private int getPriorityValue(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return 3;
            case "medium":
                return 2;
            case "low":
                return 1;
            default:
                return 0;
        }
    }

    private void setupSearchField() {
        binding.searchEventField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterEvents(String query) {
        if (originalEvents.isEmpty()) {
            originalEvents = new ArrayList<>(eventAdapter.getEvents());
        }

        List<Event> filteredList = query.isEmpty() ? 
            new ArrayList<>(originalEvents) :
            originalEvents.stream()
                .filter(event -> event.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
                
        eventAdapter.setEvents(filteredList);
    }

    private void loadEvents() {
        executorService.execute(() -> {
            try {
                String json = sharedPreferences.getString(KEY_EVENTS, "[]");
                List<Event> allEvents = new Gson().fromJson(
                    json, new TypeToken<List<Event>>(){}.getType()
                );
                
                // Filter for non-completed events and sort by most recent first
                List<Event> events = allEvents.stream()
                    .filter(event -> !"Completed".equals(event.getPriority()))
                    .sorted((e1, e2) -> {
                        int dateCompare = e2.getDate().compareTo(e1.getDate());
                        return dateCompare != 0 ? dateCompare : e2.getTime().compareTo(e1.getTime());
                    })
                    .collect(Collectors.toList());
                
                mainHandler.post(() -> {
                    originalEvents = new ArrayList<>(events);
                    eventAdapter = new EventAdapter(events, requireContext(), this);
                    binding.recyclerViewEvents.setAdapter(eventAdapter);
                    
                    String message = events.isEmpty() ? "No events found" : 
                        "Loaded " + events.size() + " events";
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("FirstFragment", "Error loading events", e);
                mainHandler.post(() -> 
                    Toast.makeText(applicationContext, "Error loading events", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void deleteEvent(int position) {
        List<Event> events = eventAdapter.getEvents();
        events.remove(position);
        eventAdapter.setEvents(events);
        
        try {
            String json = new Gson().toJson(events);
            sharedPreferences.edit()
                .putString(KEY_EVENTS, json)
                .apply();
            Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("FirstFragment", "Error deleting event", e);
            Toast.makeText(requireContext(), "Error deleting event", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Override
    public void onEventStatusChanged() {
        // Reload events to update the list
        loadEvents();
    }
}