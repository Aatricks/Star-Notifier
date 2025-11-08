---

description: "Task list template for feature implementation"
---

# Tasks: GitHub Star/Fork Notifier (Android)

**Input**: Design documents from `/specs/001-star-fork-notifications/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are MANDATORY per Test-Driven Development principle - include them for all user stories.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android app**: `app/src/main/java/io/aatricks/starnotifier/` for source
- **Tests**: `app/src/test/java/io/aatricks/starnotifier/` for unit tests
- **Android tests**: `app/src/androidTest/java/io/aatricks/starnotifier/` for integration tests

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.
  
  The /speckit.tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/
  
  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment
  
  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Add required dependencies to app/build.gradle.kts (WorkManager, Retrofit, OkHttp, Hilt)
- [x] T002 Configure Hilt dependency injection in app/build.gradle.kts and create App class
- [x] T003 [P] Setup basic MVVM directory structure in app/src/main/java/io/aatricks/starnotifier/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 Create Repository data model in app/src/main/java/io/aatricks/starnotifier/data/model/Repository.kt
- [x] T005 Create UserConfig data model in app/src/main/java/io/aatricks/starnotifier/data/model/UserConfig.kt
- [x] T006 Create GitHubApiService interface in app/src/main/java/io/aatricks/starnotifier/data/repository/GitHubApiService.kt
- [x] T007 Implement GitHubRepository in app/src/main/java/io/aatricks/starnotifier/data/repository/GitHubRepository.kt
- [x] T008 Create LocalStorageRepository interface in app/src/main/java/io/aatricks/starnotifier/data/local/LocalStorageRepository.kt
- [x] T009 Implement SharedPreferencesStorage in app/src/main/java/io/aatricks/starnotifier/data/local/SharedPreferencesStorage.kt
- [x] T010 Create NotificationHelper in app/src/main/java/io/aatricks/starnotifier/notification/NotificationHelper.kt
- [x] T011 Create GitHubCheckWorker in app/src/main/java/io/aatricks/starnotifier/worker/GitHubCheckWorker.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Receive Star Notifications (Priority: P1) 🎯 MVP

**Goal**: Enable users to receive push notifications when their selected repositories gain new stars

**Independent Test**: Select a repository, star it externally, verify notification appears within 35 minutes

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T012 [P] [US1] Unit test for star notification in app/src/test/java/io/aatricks/starnotifier/notification/NotificationHelperTest.kt
- [x] T013 [P] [US1] Integration test for star change detection in app/src/androidTest/java/io/aatricks/starnotifier/worker/GitHubCheckWorkerTest.kt

### Implementation for User Story 1

- [x] T014 [US1] Implement star notification logic in NotificationHelper.kt
- [x] T015 [US1] Implement star change detection in GitHubCheckWorker.kt
- [x] T016 [US1] Integrate star notifications with WorkManager scheduling

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Receive Fork Notifications (Priority: P2)

**Goal**: Enable users to receive push notifications when their selected repositories gain new forks

**Independent Test**: Select a repository, fork it externally, verify notification appears within 35 minutes

### Tests for User Story 2 ⚠️

- [ ] T017 [P] [US2] Unit test for fork notification in app/src/test/java/io/aatricks/starnotifier/notification/NotificationHelperTest.kt
- [ ] T018 [P] [US2] Integration test for fork change detection in app/src/androidTest/java/io/aatricks/starnotifier/worker/GitHubCheckWorkerTest.kt

### Implementation for User Story 2

- [ ] T019 [US2] Implement fork notification logic in NotificationHelper.kt
- [ ] T020 [US2] Implement fork change detection in GitHubCheckWorker.kt
- [ ] T021 [US2] Integrate fork notifications with WorkManager scheduling

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Select Repositories to Monitor (Priority: P3)

**Goal**: Allow users to choose which repositories to monitor for stars and forks

**Independent Test**: Configure repository selection, verify only selected repos trigger notifications

### Tests for User Story 3 ⚠️

- [ ] T022 [P] [US3] Unit test for repository selection in app/src/test/java/io/aatricks/starnotifier/ui/SettingsViewModelTest.kt
- [ ] T023 [P] [US3] UI test for settings screen in app/src/androidTest/java/io/aatricks/starnotifier/ui/SettingsActivityTest.kt

### Implementation for User Story 3

- [ ] T024 [US3] Create SettingsActivity in app/src/main/java/io/aatricks/starnotifier/ui/view/SettingsActivity.kt
- [ ] T025 [US3] Create SettingsViewModel in app/src/main/java/io/aatricks/starnotifier/ui/viewmodel/SettingsViewModel.kt
- [ ] T026 [US3] Create RepositoryAdapter in app/src/main/java/io/aatricks/starnotifier/ui/adapter/RepositoryAdapter.kt
- [ ] T027 [US3] Implement repository selection UI and PAT input

**Checkpoint**: All user stories should now be independently functional

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T028 [P] Add ProGuard rules for release builds in app/proguard-rules.pro
- [ ] T029 Code cleanup and documentation updates
- [ ] T030 Performance optimization and battery usage monitoring
- [ ] T031 [P] Additional integration tests for full workflow
- [ ] T032 Security review and hardening
- [ ] T033 Run quickstart.md validation and update if needed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Shares notification infrastructure with US1
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Uses storage and API components

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before UI/workers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Unit test for star notification in app/src/test/java/io/aatricks/starnotifier/notification/NotificationHelperTest.kt"
Task: "Integration test for star change detection in app/src/androidTest/java/io/aatricks/starnotifier/worker/GitHubCheckWorkerTest.kt"

# Launch implementation tasks for User Story 1:
Task: "Implement star notification logic in NotificationHelper.kt"
Task: "Implement star change detection in GitHubCheckWorker.kt"
Task: "Integrate star notifications with WorkManager scheduling"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
