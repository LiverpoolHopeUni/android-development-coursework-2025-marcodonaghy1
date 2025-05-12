package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_EVENT_LIST = "event_list";
    private static final String EVENT_SEPARATOR = "|||";
    
    private List<Event> events = new ArrayList<>();
    private int lastPosition = -1;
    private OnEventDeleteListener deleteListener;
    private SharedPreferences sharedPreferences;

    public EventAdapter(List<Event> events, Context context) {
        this.events = events;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public interface OnEventDeleteListener {
        void onEventDelete(int position);
    }

    public void setOnEventDeleteListener(OnEventDeleteListener listener) {
        this.deleteListener = listener;
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
        holder.textEventName.setText(event.getName());
        holder.textEventDate.setText(event.getDate());
        holder.textEventTime.setText(event.getTime());
        
        // Update priority text and appearance
        if ("Completed".equals(event.getPriority())) {
            holder.textEventPriority.setText("Completed");
            holder.textEventPriority.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            holder.textEventPriority.setPaintFlags(holder.textEventPriority.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textEventPriority.setText("Priority: " + event.getPriority());
            holder.textEventPriority.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
            holder.textEventPriority.setPaintFlags(holder.textEventPriority.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        
        // Update UI based on completion status
        updateEventItemAppearance(holder, "Completed".equals(event.getPriority()));
        
        // Set click listener for completion button
        holder.buttonTickEvent.setOnClickListener(v -> {
            if (!"Completed".equals(event.getPriority())) {
                // Save original priority and mark as completed
                event.setPriority("Completed");
            } else {
                // Restore original priority
                event.restoreOriginalPriority();
            }
            
            // Update UI
            notifyItemChanged(position);
            // Save changes to SharedPreferences
            saveEventsToPreferences();
        });
        
        // Apply animation
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    private void updateEventItemAppearance(EventViewHolder holder, boolean isCompleted) {
        // Update text appearance
        int flags = holder.textEventName.getPaintFlags();
        if (isCompleted) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;
            holder.textEventName.setAlpha(0.5f);
            holder.textEventDate.setAlpha(0.5f);
            holder.textEventTime.setAlpha(0.5f);
            holder.textEventPriority.setAlpha(0.5f);
        } else {
            flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
            holder.textEventName.setAlpha(1.0f);
            holder.textEventDate.setAlpha(1.0f);
            holder.textEventTime.setAlpha(1.0f);
            holder.textEventPriority.setAlpha(1.0f);
        }
        holder.textEventName.setPaintFlags(flags);
    }

    private void saveEventsToPreferences() {
        StringBuilder eventList = new StringBuilder();
        for (Event event : events) {
            if (eventList.length() > 0) {
                eventList.append(EVENT_SEPARATOR);
            }
            eventList.append(event.toString());
        }
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EVENT_LIST, eventList.toString());
        editor.apply();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull EventViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public void removeEvent(int position) {
        if (position >= 0 && position < events.size()) {
            events.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Event getEventAt(int position) {
        if (position >= 0 && position < events.size()) {
            return events.get(position);
        }
        return null;
    }

    public List<Event> getEvents() {
        return events;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textEventName;
        TextView textEventDate;
        TextView textEventTime;
        TextView textEventPriority;
        MaterialButton buttonTickEvent;

        EventViewHolder(View itemView) {
            super(itemView);
            textEventName = itemView.findViewById(R.id.textEventName);
            textEventDate = itemView.findViewById(R.id.textEventDate);
            textEventTime = itemView.findViewById(R.id.textEventTime);
            textEventPriority = itemView.findViewById(R.id.textEventPriority);
            buttonTickEvent = itemView.findViewById(R.id.buttonTickEvent);
        }
    }
} 