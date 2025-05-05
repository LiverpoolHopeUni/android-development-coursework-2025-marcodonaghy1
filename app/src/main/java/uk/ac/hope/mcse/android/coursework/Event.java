package uk.ac.hope.mcse.android.coursework;

public class Event {
    private String name;
    private String date;
    private String time;
    private String priority;

    public Event(String name, String date, String time, String priority) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.priority = priority;
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

    @Override
    public String toString() {
        return name + "\n" + date + "\n" + time;
    }
} 