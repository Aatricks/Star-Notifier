# GitHub API Contracts

## Get User Repositories

**Endpoint**: GET https://api.github.com/users/{username}/repos

**Headers**:
- Accept: application/vnd.github.v3+json
- Authorization: token {personal_access_token} (optional)

**Response**: Array of Repository objects

```json
[
  {
    "name": "repo-name",
    "full_name": "owner/repo-name",
    "stargazers_count": 42,
    "forks_count": 7,
    "private": false
  }
]
```

**Error Responses**:
- 404: User not found
- 403: Rate limit exceeded or token invalid
- 401: Bad credentials

## Rate Limits
- Unauthenticated: 60 requests per hour
- Authenticated: 5000 requests per hour

## Contract for Local Interfaces

### GitHubRepository Interface

```kotlin
interface GitHubRepository {
    suspend fun getUserRepositories(username: String, token: String?): Result<List<Repository>>
}

data class Repository(
    val name: String,
    val fullName: String,
    val stars: Int,
    val forks: Int,
    val isPrivate: Boolean
)
```

### LocalStorageRepository Interface

```kotlin
interface LocalStorageRepository {
    suspend fun saveRepositoryData(repo: RepositoryData): Result<Unit>
    suspend fun getRepositoryData(repoName: String): Result<RepositoryData?>
    suspend fun getAllSelectedRepos(): Result<List<String>>
    suspend fun saveUserConfig(config: UserConfig): Result<Unit>
    suspend fun getUserConfig(): Result<UserConfig?>
}

data class RepositoryData(
    val name: String,
    val stars: Int,
    val forks: Int,
    val lastChecked: Long
)

data class UserConfig(
    val username: String,
    val selectedRepos: List<String>,
    val token: String?
)
```

### NotificationHelper Interface

```kotlin
interface NotificationHelper {
    fun sendStarNotification(repoName: String, newCount: Int)
    fun sendForkNotification(repoName: String, newCount: Int)
}
```