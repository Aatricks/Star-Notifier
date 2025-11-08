# Quickstart: GitHub Star/Fork Notifier

## Prerequisites

- Android Studio Arctic Fox or later
- Android device or emulator with API 26+ (Android 8.0)
- GitHub account (optional, for testing)

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Aatricks/Star-Notifier.git
   cd Star-Notifier
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Open existing project
   - Select the cloned directory

3. **Build the project**
   - Wait for Gradle sync to complete
   - Build > Make Project

## Configuration

1. **Install the app**
   - Run on device/emulator
   - Grant notification permissions when prompted

2. **Initial Setup**
   - Enter your GitHub username
   - Select repositories to monitor
   - Optionally enter Personal Access Token for higher API limits

## Testing

1. **Manual Testing**
   - Star or fork a monitored repository externally
   - Wait up to 30 minutes for notification
   - Check notification appears with correct count

2. **API Testing**
   - Use GitHub API directly to verify rate limits
   - Test with/without PAT

## Troubleshooting

- **No notifications**: Check WorkManager is running, verify API responses
- **Rate limit errors**: Add PAT or wait for reset
- **App crashes**: Check logs for API parsing errors

## Development

- Run unit tests: `./gradlew test`
- Run UI tests: `./gradlew connectedAndroidTest`
- Debug background work: Use Android Profiler