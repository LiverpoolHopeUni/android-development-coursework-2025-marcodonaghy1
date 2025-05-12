package uk.ac.hope.mcse.android.coursework;

public class Event {
    private String name;
    private String date;
    private String time;
    private String priority;
    private String originalPriority;
    private boolean isCompleted;

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

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPriority() {
        return priority;
    }

    public String getOriginalPriority() {
        return originalPriority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void restoreOriginalPriority() {
        this.priority = this.originalPriority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public String toString() {
        return name + "\n" + date + "\n" + time + "\n" + priority;
    }
} 