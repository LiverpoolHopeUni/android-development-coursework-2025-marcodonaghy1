package uk.ac.hope.mcse.android.coursework;

import androidx.annotation.NonNull;

public class Event {
    private String name;
    private final String date;
    private final String time;
    private String priority;
    private String originalPriority;
    private final boolean isCompleted;

    public Event(String name, String date, String time, String priority) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.originalPriority = priority;
        this.isCompleted = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getOriginalPriority() {
        return originalPriority;
    }

    public void setOriginalPriority(String originalPriority) {
        this.originalPriority = originalPriority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    @NonNull
    @Override
    public String toString() {
        return name + "\n" + date + "\n" + time + "\n" + priority;
    }
} 