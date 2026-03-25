# AppShopTour Architecture

## Purpose

AppShopTour is a training project for building a full mobile product with:

- Android client
- iOS client
- Kotlin backend
- PostgreSQL database
- deployment to a personal VDS

The goal is not maximum code sharing. The goal is to understand each layer well:

- native mobile UI
- shared domain and networking
- backend API design
- database design and migrations
- deployment and operations basics

## Guiding Principles

1. Share business logic, not platform UX.
2. Keep Android and iOS UI native.
3. Build the product in vertical slices from API to UI.
4. Prefer explicit architecture over hidden magic.
5. Add infrastructure gradually, only when the previous layer is understood.

## Target Architecture

### Mobile

- Android UI: Jetpack Compose
- iOS UI: SwiftUI
- Shared Kotlin module: models, use cases, repositories, API client, validation, session state

### Backend

- Ktor server
- REST API
- PostgreSQL
- SQL migrations
- environment-based configuration for local and VDS environments

## Module Responsibilities

### Current Repository Layout

- `composeApp`
- `iosApp`
- `shared`
- `server`

### Planned Responsibilities

#### `shared`

Shared Kotlin Multiplatform logic:

- domain models
- DTO mappers
- API client
- repository implementations
- use cases
- input validation
- shared error model

This module must not contain:

- Android UI
- SwiftUI views
- server-only database code

#### `server`

Backend application:

- routing
- authentication
- business rules that must stay on the server
- database access
- migrations setup
- deployment configuration

#### `iosApp`

Native iOS application shell:

- SwiftUI screens
- iOS navigation
- iOS-specific presentation logic
- integration with shared Kotlin code

#### `composeApp`

Current Android entry point and temporary sandbox for template-generated Compose code.

Near-term role:

- Android app entry point
- Android screens
- Android navigation
- Android-specific UI state wiring

Long-term note:

The project currently uses a starter layout where `composeApp` also includes desktop and web targets. For this training project, mobile and backend are the priority. Desktop and web are optional and should not drive architecture decisions.

## Intended Layering

### Client Side

1. UI layer
2. presentation/state layer
3. use case layer
4. repository layer
5. remote/local data sources

### Server Side

1. routing layer
2. application/service layer
3. repository/data access layer
4. database

## What We Share vs What We Keep Native

### Shared

- business entities
- API contracts used by the client
- validation rules that make sense on the client
- repositories and use cases
- networking code

### Native

- screen layout
- navigation
- platform permissions
- platform storage integrations when they are truly platform-specific
- look and feel

## First Product Scope

The first milestone should be one complete vertical slice:

- one database table
- one server endpoint for read
- one server endpoint for create
- one shared repository
- one Android screen
- one iOS screen

This slice is enough to prove the architecture before new features are added.

## Planned Evolution

### Phase 1

- keep current modules
- treat `composeApp` as Android-first
- start building shared domain and server API

### Phase 2

- add database and migrations
- connect mobile clients to real backend
- introduce authentication if needed

### Phase 3

- consider splitting Android into its own dedicated module if the current starter layout becomes limiting
- keep desktop/web out of scope unless there is a clear learning reason to add them

## Non-Goals For Now

- microservices
- shared UI across Android and iOS by default
- web client as a priority
- advanced CI/CD before the first working product slice

## Working Rule

Every new feature should be added as a full learning loop:

1. define the use case
2. design the API
3. update the database
4. implement the server
5. expose shared client logic
6. build Android UI
7. build iOS UI
8. test the full flow
