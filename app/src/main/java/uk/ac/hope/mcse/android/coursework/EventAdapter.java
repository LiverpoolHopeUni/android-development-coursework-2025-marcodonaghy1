package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_EVENT_LIST = "event_list";
    private static final String EVENT_SEPARATOR = "|||";
    
    private List<Event> events;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private OnEventUpdatedListener listener;

    public interface OnEventUpdatedListener {
        void onEventUpdated();
    }

    public EventAdapter(List<Event> events, Context context) {
        this.events = events;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        this.events = events;
        notifyDataSetChanged();
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
        StringBuilder eventList = new StringBuilder();
        for (Event event : events) {
            if (eventList.length() > 0) {
                eventList.append(EVENT_SEPARATOR);
            }
            eventList.append(event.getName()).append("\n")
                    .append(event.getDate()).append("\n")
                    .append(event.getTime()).append("\n")
                    .append(event.getPriority());
        }
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EVENT_LIST, eventList.toString());
        editor.apply();

        if (listener != null) {
            listener.onEventUpdated();
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
                    String newPriority;
                    
                    if (!"Completed".equals(currentEvent.getPriority())) {
                        // Store original priority in a tag
                        currentEvent.setOriginalPriority(currentEvent.getPriority());
                        newPriority = "Completed";
                    } else {
                        // Restore original priority or default to Medium
                        newPriority = currentEvent.getOriginalPriority() != null ? 
                            currentEvent.getOriginalPriority() : "Medium";
                    }
                    
                    // Update event priority
                    currentEvent.setPriority(newPriority);
                    updateTickButtonAppearance(currentEvent);
                    
                    // Save changes
                    saveEvents();
                }
            });
        }

        private void updateTickButtonAppearance(Event event) {
            if ("Completed".equals(event.getPriority())) {
                buttonTickEvent.setIconResource(R.drawable.ic_done);
                buttonTickEvent.setBackgroundTintList(context.getColorStateList(R.color.completed_event));
            } else {
                buttonTickEvent.setIconResource(R.drawable.ic_check);
                buttonTickEvent.setBackgroundTintList(context.getColorStateList(R.color.primary));
            }
        }
    }
} 