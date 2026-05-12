# StayOnTrail

StayOnTrail is a forest-themed productivity and task management Android app designed to help users stay focused, organize tasks, and build consistent habits through visual motivation.

The app combines a to-do list, calendar, focus timer, streak tracking, and AI-assisted task planning into one simple productivity system. As users complete tasks and maintain consistency, their productivity progress is represented through a growing forest.

## Overview

Many task management apps focus only on listing tasks, but they often fail to make productivity feel rewarding or emotionally engaging. StayOnTrail solves this by turning productivity into a visual journey.

Users can create and organize tasks, view their schedule, start focus sessions, and track their progress through a forest-based streak system. The app encourages consistency by making each completed task feel like progress toward growing and maintaining a forest.

## Key Features

- Task creation and editing
- Today-focused task view
- Full task list view
- Calendar view for scheduled tasks
- Focus timer with tree growth animation
- Forest-based streak visualization
- Task categories and filters
- Priority-based task organization
- Repeating task support
- AI-assisted task planning using the DeepSeek API
- Clean and simple Android interface

## App Concept

StayOnTrail uses a nature-inspired productivity system.

When users complete tasks and stay consistent, their forest grows. This creates a visual representation of discipline, progress, and habit-building. The goal is to make productivity feel less like a checklist and more like a journey.

## Screens / Main Sections

### Home Screen

The home screen shows the user’s tasks for the day. It provides a focused view so users can quickly see what needs to be done without being overwhelmed by their entire task list.

### Task View

The task view allows users to create, edit, organize, and manage their tasks. Tasks can be categorized, prioritized, and filtered based on the user’s needs.

### Calendar View

The calendar view helps users see upcoming tasks across different dates, making it easier to plan ahead and manage deadlines.

### Focus Timer

The focus timer encourages deep work. During a focus session, a sapling gradually grows into a tree as the timer progresses. This gives users a visual sense of progress while they work.

### Forest / Streak View

The forest view displays the user’s productivity streak. It acts as a motivational system that rewards consistency over time.

### AI Task Planning

StayOnTrail includes an AI-powered feature that helps users generate or refine tasks using the DeepSeek API. This helps users break down goals, plan work, and organize tasks more effectively.

## Tech Stack

| Area | Technology |
|---|---|
| Platform | Android |
| Language | Java |
| IDE | Android Studio |
| AI Integration | DeepSeek API |
| Networking | HttpURLConnection |
| Concurrency | ExecutorService |
| UI Thread Handling | Handler and Looper |
| App Architecture | Activity-based Android structure |

## Technical Implementation

### Background Processing

Network requests and long-running operations are handled on background threads using `ExecutorService`. This prevents the app from freezing while API calls or other background tasks are running.

### UI Thread Updates

Since Android does not allow direct UI updates from background threads, `Handler` and `Looper` are used to safely update the interface after background tasks finish.

### AI API Integration

The AI feature is implemented using the DeepSeek API. The app sends a POST request with the correct authorization and JSON body, then processes the response to generate useful task suggestions for the user.

### Activity Communication

The app uses Android activity result patterns to send data between screens, allowing users to create or edit tasks and return the updated result to the main interface.

## Project Structure

```text
StayOnTrail/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── net/limaru/stayontrail/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
