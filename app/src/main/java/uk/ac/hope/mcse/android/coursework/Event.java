package uk.ac.hope.mcse.android.coursework;

/**
 * Represents an event with name, date, and time.
 */
public class Event {
    private final String eventName;
    private final String eventDate;
    private final String eventTime;

    /**
     * Constructor for creating a new Event.
     *
     * @param eventName The name of the event
     * @param eventDate The date of the event (format: DD/MM/YYYY)
     * @param eventTime The time of the event (format: HH:MM)
     */
    public Event(String eventName, String eventDate, String eventTime) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
    }

    /**
     * @return The name of the event
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @return The date of the event (format: DD/MM/YYYY)
     */
    public String getEventDate() {
        return eventDate;
    }

    /**
     * @return The time of the event (format: HH:MM)
     */
    public String getEventTime() {
        return eventTime;
    }
} 