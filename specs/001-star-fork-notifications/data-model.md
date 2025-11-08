# Data Model: GitHub Star/Fork Notifier (Android)

**Date**: November 8, 2025
**Feature**: specs/001-star-fork-notifications/spec.md

## Entities

### Repository Entity

Represents a GitHub repository being monitored.

**Fields**:
- `name` (String): Repository name (e.g., "owner/repo")
- `currentStars` (Int): Current star count from GitHub API
- `currentForks` (Int): Current fork count from GitHub API
- `lastChecked` (Long): Timestamp of last API check (milliseconds since epoch)
- `isSelected` (Boolean): Whether user has selected this repo for monitoring

**Validation Rules**:
- `name` must be non-empty and follow GitHub repo format (owner/repo)
- `currentStars` and `currentForks` must be non-negative
- `lastChecked` must be a valid timestamp

**Relationships**:
- Belongs to User Configuration (many repos per user)

### User Configuration Entity

Stores user settings and preferences.

**Fields**:
- `username` (String): GitHub username
- `selectedRepos` (List<String>): List of selected repository names
- `personalAccessToken` (String?): Optional GitHub PAT (nullable)
- `checkIntervalMinutes` (Int): Background check interval (default 30)

**Validation Rules**:
- `username` must be non-empty and valid GitHub username format
- `selectedRepos` must contain valid repository names
- `personalAccessToken` if present, must be valid GitHub token format
- `checkIntervalMinutes` must be positive

**Relationships**:
- Has many Repositories

## State Transitions

### Repository State
- **Initial**: No data stored
- **Loaded**: Data fetched from API, stored locally
- **Updated**: New data detected, notification sent
- **Error**: API call failed, retry scheduled

### User Configuration State
- **Not Configured**: No username set
- **Configured**: Username and repos selected
- **Authenticated**: PAT provided for higher limits

## Data Flow

1. User configures username and selects repos
2. App fetches initial repo data from GitHub API
3. Data stored in local storage
4. Background worker periodically checks for changes
5. On change detection, update local storage and send notification
6. UI displays current status and allows configuration changes

## Storage Strategy

- **Primary**: SharedPreferences for simple serialization
- **Alternative**: Room database for future complex queries
- **Migration**: Support migration from SharedPreferences to Room if needed