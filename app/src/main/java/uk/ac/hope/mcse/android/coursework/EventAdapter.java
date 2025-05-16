package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final String PREF_NAME = "EventDetails";
    private static final String KEY_EVENTS = "all_events_json";
    
    private List<Event> events;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private OnEventUpdatedListener listener;
    private final OnEventStatusChangedListener statusListener;

    public interface OnEventUpdatedListener {
        void onEventUpdated();
    }

    public interface OnEventStatusChangedListener {
        void onEventStatusChanged();
    }

    public EventAdapter(List<Event> events, Context context, OnEventStatusChangedListener statusListener) {
        this.events = events;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.statusListener = statusListener;
    }

    public void setOnEventUpdatedListener(OnEventUpdatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> events) {
        Log.d("EventAdapter", "Setting events: " + events.size() + " events");
        this.events = new ArrayList<>(events);
        notifyDataSetChanged();
    }

    public void updateEvents(List<Event> events) {
        Log.d("EventAdapter", "Updating events: " + events.size() + " events");
        this.events = new ArrayList<>(events);
        notifyDataSetChanged();
        saveEvents();
    }

    public List<Event> getEvents() {
        return events;
    }

    public Event getEventAt(int position) {
        if (position >= 0 && position < events.size()) {
            return events.get(position);
        }
        return null;
    }

    public void updateEventAt(int position, Event updatedEvent) {
        if (position >= 0 && position < events.size()) {
            events.set(position, updatedEvent);
            notifyItemChanged(position);
            saveEvents();
        }
    }

    public void removeEvent(int position) {
        if (position >= 0 && position < events.size()) {
            events.remove(position);
            notifyItemRemoved(position);
            saveEvents();
        }
    }

    private void saveEvents() {
        try {
            String json = new Gson().toJson(events);
            Log.d("EventAdapter", "Saving events to SharedPreferences: " + json);
            sharedPreferences.edit()
                .putString(KEY_EVENTS, json)
                .apply();

            if (listener != null) {
                listener.onEventUpdated();
            }
            if (statusListener != null) {
                statusListener.onEventStatusChanged();
            }
        } catch (Exception e) {
            Log.e("EventAdapter", "Error saving events", e);
            e.printStackTrace();
        }
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView textEventName;
        private final TextView textEventDate;
        private final TextView textEventTime;
        private final TextView textEventPriority;
        private final MaterialButton buttonTickEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventName = itemView.findViewById(R.id.textEventName);
            textEventDate = itemView.findViewById(R.id.textEventDate);
            textEventTime = itemView.findViewById(R.id.textEventTime);
            textEventPriority = itemView.findViewById(R.id.textEventPriority);
            buttonTickEvent = itemView.findViewById(R.id.buttonTickEvent);
        }

        public void bind(Event event) {
            Log.d("EventAdapter", "Binding event: " + event.getName() + " with priority: " + event.getPriority());
            textEventName.setText(event.getName());
            textEventDate.setText(event.getDate());
            textEventTime.setText(event.getTime());
            textEventPriority.setText(event.getPriority());

            // Update tick button appearance based on completion status
            updateTickButtonAppearance(event);

            buttonTickEvent.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Event currentEvent = events.get(position);
                    String newPriority = "Completed".equals(currentEvent.getPriority()) ? 
                        (currentEvent.getOriginalPriority() != null ? currentEvent.getOriginalPriority() : "Medium") : 
                        "Completed";
                    
                    Log.d("EventAdapter", "Toggling event status: " + currentEvent.getName() + 
                        " from " + currentEvent.getPriority() + " to " + newPriority);
                    
                    // Store original priority if marking as completed
                    if (!"Completed".equals(currentEvent.getPriority())) {
                        currentEvent.setOriginalPriority(currentEvent.getPriority());
                    }
                    
                    // Update event priority
                    currentEvent.setPriority(newPriority);
                    
                    // Get all events from SharedPreferences
                    String json = sharedPreferences.getString(KEY_EVENTS, "[]");
                    List<Event> allEvents = new Gson().fromJson(json, new TypeToken<List<Event>>(){}.getType());
                    
                    // Update the event in the complete list
                    for (int i = 0; i < allEvents.size(); i++) {
                        Event e = allEvents.get(i);
                        if (e.getName().equals(currentEvent.getName()) && 
                            e.getDate().equals(currentEvent.getDate()) && 
                            e.getTime().equals(currentEvent.getTime())) {
                            allEvents.set(i, currentEvent);
                            break;
                        }
                    }
                    
                    // Save the updated complete list
                    String updatedJson = new Gson().toJson(allEvents);
                    Log.d("EventAdapter", "Saving updated events to SharedPreferences: " + updatedJson);
                    sharedPreferences.edit()
                        .putString(KEY_EVENTS, updatedJson)
                        .apply();
                    
                    // Remove from current list and notify adapter
                    events.remove(position);
                    notifyItemRemoved(position);
                    
                    // Notify listeners
                    if (listener != null) {
                        listener.onEventUpdated();
                    }
                    if (statusListener != null) {
                        statusListener.onEventStatusChanged();
                    }
                }
            });
        }

        private void updateTickButtonAppearance(Event event) {
            boolean isCompleted = "Completed".equals(event.getPriority());
            buttonTickEvent.setIconResource(isCompleted ? R.drawable.ic_done : R.drawable.ic_check);
            buttonTickEvent.setBackgroundTintList(context.getColorStateList(
                isCompleted ? R.color.completed_event : R.color.primary));
        }
    }
} 