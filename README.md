[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/DS3PLs8n)

# Event Management Application Documentation

## Application Overview
This Android application is an event management system designed to help users organise and track their events. The application provides a user-friendly interface for creating, managing, and monitoring events with various priority levels and completion statuses.

## Key Features

### 1. Event Management
- Create new events with name, date, time, and priority
- View and manage existing events
- Edit event details (name, date, time, priority)
- Delete events with swipe gesture
- Mark events as completed
- Track event priorities and completion status
- View completed events history

### 2. User Interface
- Modern Material Design implementation
- Dark/Light theme support
- Intuitive navigation using bottom navigation
- Custom toolbar with contextual titles
- Responsive layout design
- Swipe gestures for quick actions (edit/delete)
- Icons for improved user experience:
  - Calendar icon for date selection
  - Clock icon for time selection
  - Check mark for completed events
  - Edit icon for modifying events
  - Delete icon for removing events
  - Add icon for creating new events
  - Notification icon for reminders
  - Privacy icon for settings
  - Back and close icons for navigation

### 3. Settings and Customization
- Theme preferences (Dark/Light mode)
- Privacy policy access
- User preferences management

## Key Android Constructs

### 1. Architecture Components
- **Fragments**: Used for modular UI components (FirstFragment, SecondFragment, CompletedEventsFragment, SettingsFragment)
- **Navigation Component**: Implements app navigation using the Navigation component
- **ViewBinding**: Utilizes Android's ViewBinding for safe view access
- **SharedPreferences**: Manages user preferences and settings

### 2. UI Components
- **RecyclerView**: Displays lists of events efficiently
- **Custom Adapters**: EventAdapter for managing event display
- **Toolbar**: Custom implementation with dynamic titles
- **Bottom Navigation**: For main app navigation
- **ItemTouchHelper**: Implements swipe gestures for edit/delete actions
- **AlertDialog**: Used for event editing interface
- **Vector Drawables**: Custom icons for intuitive user interaction
- **Material Icons**: Standardised icon set for consistent UI

### 3. Background Processing
- **BroadcastReceiver**: EventReminderReceiver for handling notifications
- **SharedPreferences**: For persistent data storage
- **Theme Management**: Dynamic theme switching support

## User Guide

### Getting Started
1. Launch the application
2. The home screen displays your current events
3. Use the bottom navigation to switch between different sections

### Creating an Event
1. Navigate to the "Create Event" section
2. Fill in the event details:
   - Event name
   - Date
   - Time
   - Priority level
3. Save the event

### Managing Events
- View all events on the home screen
- Tap an event to view details
- Edit events by swiping right on an event
- Delete events by swiping left on an event
- Mark events as completed
- Filter events by priority
- View completed events in the history section

### Editing Events
1. Swipe right on an event to edit
2. Modify any of the following:
   - Event name
   - Date
   - Time
   - Priority level
3. Save changes or cancel to revert

### Deleting Events
1. Swipe left on an event to delete
2. Confirm deletion in the dialog
3. Event will be permanently removed

### Customizing Settings
1. Access settings through the menu or navigation
2. Toggle between light and dark themes
3. View privacy policy
4. Adjust other preferences as needed

### Tips
- Use priority levels to organize important events
- Regularly check completed events
- Utilize the dark theme for night-time use
- Keep your events updated for better organization
- Use swipe gestures for quick event management
- Double-check before deleting events
- Look for recognisible icons to guide your actions

## Technical Implementation
The application follows modern Android development practices:
- Kotlin/Java implementation
- Material Design guidelines
- Responsive layout design
- Efficient data management
- Background processing for notifications
- Persistent storage for user preferences
- Vector drawables for scalable icons
- Consistent icon usage throughout the app

This application demonstrates the implementation of various Android development concepts and best practices while providing a practical solution for event management.

