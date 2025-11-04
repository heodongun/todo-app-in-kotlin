# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Ugoal** is a Toss-inspired minimalist Android goal management app built with Kotlin and Jetpack Compose. The app uses MongoDB Atlas for data persistence and follows MVVM architecture with reactive Flow-based state management.

### Key Features
- Big Goals: Long-term goal tracking with progress indicators
- Daily Goals: Focus tasks for each day with MongoDB persistence
- Todos: Task management with big goal association
- Pomodoro Timer: Built-in foreground service for time management
- Notifications & Alarms: Reminder system for todos

## Build & Development Commands

### Building
```bash
# Clean build
./gradlew clean assembleDebug

# Build and install on connected device/emulator
./gradlew installDebug

# Build release (if needed)
./gradlew assembleRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.heodongun.ugoal.viewmodel.HomeViewModelTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific instrumented test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.heodongun.ugoal.ui.HomeScreenTest
```

### Gradle Operations
```bash
# Sync dependencies
./gradlew --refresh-dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Clean build cache
./gradlew clean build --refresh-dependencies
```

## Architecture & Code Structure

### MVVM Flow Pattern
The app follows strict unidirectional data flow:

```
UI (Composable) → ViewModel → Repository → MongoDbClient → MongoDB Atlas
                      ↓
                  StateFlow/Flow
                      ↓
                UI observes with collectAsState()
```

**Critical**: Always maintain this flow direction. UI should never directly access Repository or MongoDbClient.

### Dependency Injection Pattern
ViewModels are manually injected through factory pattern in MainActivity:
- Repository is instantiated once in `UgoalApplication`
- ViewModels receive repository through factory pattern
- Use same pattern when adding new ViewModels

### State Management Rules
1. ViewModels expose StateFlow/Flow, never MutableStateFlow
2. Repository manages single source of truth via `_userData: MutableStateFlow`
3. All state updates go through Repository methods
4. UI uses `collectAsState()` for Flow observation

## MongoDB Integration

### Architecture
- **Data API Endpoint**: Uses MongoDB Atlas Data API (REST-based, not Realm SDK)
- **Client**: Ktor HttpClient with content negotiation
- **Collection Structure**: Single `users` collection with embedded documents

### Important Patterns

**Never bypass the Repository layer**. All MongoDB operations must go through:
```kotlin
ViewModel → Repository → MongoDbClient → MongoDB Atlas
```

**State Synchronization**: Repository updates local `_userData` state immediately on success, ensuring UI updates without waiting for network round-trip.

### Data Flow Example
```kotlin
// User adds a todo
UI calls: viewModel.addTodo(todo)
→ ViewModel calls: repository.addTodo(todo)
→ Repository calls: mongoClient.addTodo(todo)
→ On success: repository updates _userData (local state)
→ Flow emits: UI automatically updates via collectAsState()
```

## Navigation Structure

### Route Pattern
Navigation uses sealed class pattern with type-safe routes:
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Goals : Screen("goals")
    // Add new screens here
}
```

### Adding New Screens
1. Add route to `Screen` sealed class in `NavGraph.kt`
2. Add composable to NavHost with same animation pattern
3. If bottom nav: add to `bottomNavItems` list
4. Create screen in `ui/screens/` directory

### Navigation Transitions
All screens use consistent slide + fade animations. Maintain this pattern for new screens.

## Toss Design System

### Color Usage
- **TossBlue (#3182F6)**: Primary actions, selected states, CTAs
- **TossGray900**: Primary text
- **TossGray400**: Unselected states, secondary text
- **BackgroundWhite**: All screen backgrounds
- **SuccessGreen**: Completed states
- **ErrorRed**: High priority, errors

### Typography Scale
- `displayLarge` (32sp Bold): Main screen titles
- `headlineLarge` (26sp Bold): Section headers
- `titleLarge` (18sp SemiBold): Card titles
- `bodyLarge` (16sp): Content text
- `labelLarge` (14sp Medium): Buttons, small labels

### Animation Standards
- **Duration**: 300ms for transitions
- **Type**: Spring animations for interactive elements
- **Pattern**: FadeIn + SlideIn for screen transitions
- **Haptics**: Use `HapticFeedback.performClick()` for interactive elements

## Service Architecture

### PomodoroService (Foreground Service)
- **Type**: Foreground service with notification
- **State**: Managed via StateFlow (Running, Paused, Completed)
- **Lifecycle**: Bind from UI, observe timer state reactively
- **Notification**: Shows timer progress in notification channel

### AlarmReceiver (BroadcastReceiver)
- **Purpose**: Handles scheduled todo reminders
- **Scheduling**: Use `AlarmReceiver.scheduleTodoReminder()`
- **Permissions**: Requires SCHEDULE_EXACT_ALARM for Android 12+

## Testing Patterns

### ViewModel Testing
```kotlin
// Use Turbine for Flow testing
viewModel.state.test {
    viewModel.performAction()
    assertEquals(expected, awaitItem())
}
```

### Compose UI Testing
```kotlin
composeTestRule.setContent {
    ScreenToTest(viewModel = mockViewModel)
}
composeTestRule.onNodeWithText("Expected").assertExists()
```

## Common Development Tasks

### Adding a New Data Model
1. Create `@Serializable data class` in `data/models/`
2. Add field to `UserData.kt` if persisting to MongoDB
3. Add CRUD methods to `MongoDbClient` (use `$push`, `$set`, `$pull` operators)
4. Add CRUD methods to `UgoalRepository` with local state updates
5. Add ViewModel methods if needed

### Adding a New Screen with ViewModel
1. Create screen Composable in `ui/screens/ScreenName.kt`
2. Create ViewModel in `viewmodel/ScreenNameViewModel.kt`
3. Add route to `Screen` sealed class in `NavGraph.kt`
4. Add factory in `MainActivity` (follow existing pattern)
5. Add composable to NavHost with transitions

### Adding Reusable UI Components
1. Create in `ui/components/ComponentName.kt`
2. Follow Toss design system (colors, typography, spacing)
3. Make stateless where possible (hoist state to caller)
4. Use `@Preview` annotations for easy visualization

## Known Issues & Solutions

### Build Issues
- **JDK Version**: Must use JDK 17 (check in Android Studio Settings)
- **Gradle Sync Fails**: Try `./gradlew --refresh-dependencies`
- **Compose Issues**: Ensure Compose BOM version is consistent

### MongoDB Connection
- **Connection Fails**: Check internet permission in AndroidManifest.xml
- **Timeout**: MongoDB Atlas Data API has rate limits, handle appropriately
- **Empty Response**: Check `dataSource`, `database`, `collection` names in MongoDbClient

### Runtime Issues
- **Service Not Starting**: Verify all service permissions in manifest
- **Alarms Not Triggering**: Request exact alarm permission on Android 12+
- **Notifications Missing**: Request POST_NOTIFICATIONS permission on Android 13+

## Development Status

### ✅ Implemented
- Core MVVM architecture with Flow
- MongoDB Atlas integration
- Home screen with daily goals
- Goals screen with big goals
- Add Todo/Goal screens
- Notification system
- Pomodoro service
- Haptic feedback utilities

### 🚧 Planned (see IMPLEMENTATION_GUIDE.md)
- TodoDetailScreen (enhanced todo editing)
- PomodoroScreen (timer UI)
- CalendarScreen (date-based views)
- StatisticsScreen (analytics)
- SearchScreen (filtering)
- SettingsScreen (preferences)

## Important Conventions

### File Naming
- Screens: `ScreenNameScreen.kt` (e.g., `HomeScreen.kt`)
- ViewModels: `ScreenNameViewModel.kt` (e.g., `HomeViewModel.kt`)
- Components: `ComponentName.kt` (e.g., `TodoItem.kt`)
- Models: `ModelName.kt` (e.g., `BigGoal.kt`)

### Package Organization
```
com.heodongun.ugoal/
├── data/
│   ├── models/      # Data classes with @Serializable
│   ├── remote/      # MongoDbClient
│   └── repository/  # Single source of truth layer
├── ui/
│   ├── screens/     # Full-screen Composables
│   ├── components/  # Reusable UI components
│   ├── theme/       # Toss design system
│   └── navigation/  # NavGraph and routing
├── viewmodel/       # ViewModels with StateFlow
├── service/         # Background services
└── utils/           # Utilities (haptics, date formatting, etc.)
```

### Code Style
- Use Kotlin coroutines for async operations
- Prefer `Flow` over `LiveData`
- Use `@Composable` functions for UI
- Follow Material 3 design patterns
- Keep ViewModels framework-agnostic (no Android imports except ViewModel)

## References

- Main documentation: `README.md`
- Setup guide: `SETUP_GUIDE.md`
- Feature status: `EXPANDED_FEATURES_STATUS.md`
- Implementation templates: `IMPLEMENTATION_GUIDE.md`
- Quick start: `QUICK_START.md`
