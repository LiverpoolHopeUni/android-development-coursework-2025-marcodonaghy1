package uk.ac.hope.mcse.android.coursework;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();
    private int lastPosition = -1;
    private OnEventDeleteListener deleteListener;

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
        holder.textEventPriority.setText("Priority: " + event.getPriority());
        
        // Apply animation
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
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

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textEventName;
        TextView textEventDate;
        TextView textEventTime;
        TextView textEventPriority;

        EventViewHolder(View itemView) {
            super(itemView);
            textEventName = itemView.findViewById(R.id.textEventName);
            textEventDate = itemView.findViewById(R.id.textEventDate);
            textEventTime = itemView.findViewById(R.id.textEventTime);
            textEventPriority = itemView.findViewById(R.id.textEventPriority);
        }
    }
} 