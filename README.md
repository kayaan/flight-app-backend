# FlightApp Backend

Backend service for FlightApp, a flight analysis and sharing application for IGC/paragliding flight data.

The backend provides authentication, user management, flight metadata storage, original IGC file upload/download, and later synchronization/sharing functionality for the Angular frontend.

## Tech Stack

* Kotlin
* Spring Boot 4
* Spring Security
* Google OAuth2 / OpenID Connect Login
* Spring Data JPA / Hibernate
* PostgreSQL
* Flyway database migrations
* Azure Blob Storage SDK
* Azurite for local blob storage development
* Gradle Kotlin DSL
* Java 25

## Current Features

* Health endpoint
* Google OAuth2 login
* Session-based authentication
* `/api/me` endpoint for current authenticated user
* App user persistence
* External identity mapping through `user_identities`
* Flight metadata CRUD
* Flight visibility handling: `PRIVATE`, `UNLISTED`, `PUBLIC`
* Soft delete for flights
* Original IGC upload and download
* Basic IGC validation

  * max file size
  * `.igc` extension
  * UTF-8 text decoding
  * requires A-record and B-record
* Local Azure Blob Storage emulation with Azurite
* Global JSON error handling

## Project Structure

```text
src/main/kotlin/com/flightapp/backend
  auth/        Authentication, current user resolution, OAuth user service
  common/      Shared API error handling
  config/      Application configuration properties
  flights/     Flight metadata, flight files, controllers and services
  storage/     Blob storage abstraction and Azure Blob implementation
  users/       App user and external identity persistence
```

## Requirements

* Java 25
* Gradle wrapper included
* PostgreSQL 17 or compatible
* Azurite for local blob storage
* Google OAuth2 client credentials

## Local Development Setup

### 1. Start PostgreSQL

The application expects a PostgreSQL database.

Example database settings:

```text
Database: flightapp
User:     flightapp
Password: flightapp
Port:     5433
```

The current local development setup uses PostgreSQL through Podman.

### 2. Start Azurite

Azurite is used as local Azure Blob Storage emulator.

The backend uses the Azurite development account:

```text
AccountName: devstoreaccount1
```

Example local blob endpoint:

```text
http://<podman-machine-ip>:10000/devstoreaccount1
```

On some Windows/Podman setups, `localhost` may not work. In that case, use the Podman machine IP.

### 3. Configure environment variables

The backend requires Google OAuth credentials.

Set the following environment variables in IntelliJ Run Configuration or your shell:

```text
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

Optional frontend redirect configuration:

```text
FRONTEND_BASE_URL=http://localhost:4200
```

Optional storage configuration:

```text
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=...;BlobEndpoint=http://<podman-machine-ip>:10000/devstoreaccount1;
AZURE_STORAGE_CONTAINER_NAME=flight-files
```

## Google OAuth Setup

Create a Google OAuth client in Google Cloud Console.

Application type:

```text
Web application
```

Authorized redirect URI for local development:

```text
http://localhost:8080/login/oauth2/code/google
```

The frontend starts login through:

```text
/api/auth/login/google
```

Spring Security then redirects to Google and handles the callback at:

```text
/login/oauth2/code/google
```

## Running the Application

From the repository root:

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat bootRun
```

Or start the application from IntelliJ.

If dependencies were changed, reload the Gradle project in IntelliJ before starting.

## Build

```bash
./gradlew clean build
```

On Windows:

```bash
gradlew.bat clean build
```

## API Endpoints

### Health

```http
GET /api/health
```

### Authentication

```http
GET /api/auth/login/google
POST /api/auth/logout
GET /api/me
```

### Flights

```http
GET    /api/flights
POST   /api/flights
GET    /api/flights/{id}
DELETE /api/flights/{id}
PATCH  /api/flights/{id}/visibility
```

### Flight Files / IGC

```http
POST  /api/flights/{id}/file
GET   /api/flights/{id}/file
PATCH /api/flights/{id}/file
POST  /api/flights/{id}/igc
GET   /api/flights/{id}/igc
```

IGC upload uses multipart form field:

```text
file
```

## Authentication Model

The application uses server-side session authentication.

Flow:

```text
Angular frontend
→ /api/auth/login/google
→ Spring Security OAuth2 login
→ Google
→ Spring callback
→ AppUser / UserIdentity persistence
→ Server session
→ Browser receives session cookie
```

The Angular frontend does not store JWT tokens. It calls backend APIs with credentials/cookies.

## Database Migrations

Flyway is used for database schema management.

Migration files are located under:

```text
src/main/resources/db/migration
```

Existing migrations create and update core tables such as:

* `app_users`
* `user_identities`
* `flights`
* `flight_files`
* `flight_shares`
* `flyway_schema_history`

## Local Notes

For local Windows/Podman development, `localhost` may not work for PostgreSQL or Azurite depending on the Podman machine/network setup.

In that case, use the Podman machine IP, for example:

```text
jdbc:postgresql://<podman-machine-ip>:5433/flightapp
BlobEndpoint=http://<podman-machine-ip>:10000/devstoreaccount1
```

## Frontend Integration

The Angular frontend should call the backend using relative URLs:

```text
/api/me
/api/flights
/api/auth/login/google
```

During local development, Angular can proxy `/api` to:

```text
http://localhost:8080
```

For Azure Static Web Apps deployment, `/api/*` should be routed to the Spring Boot backend service.

## Deployment Notes

Planned deployment model:

```text
Azure Static Web Apps
  → Angular frontend

Azure App Service
  → Spring Boot backend

Azure PostgreSQL
  → database

Azure Blob Storage
  → original IGC files
```

Configuration should be provided through environment variables / application settings, not hardcoded values.

Important production settings:

```text
FRONTEND_BASE_URL=https://your-static-web-app-url
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
AZURE_STORAGE_CONNECTION_STRING=...
AZURE_STORAGE_CONTAINER_NAME=...
```

## Status

This backend is currently under active development.

Implemented baseline:

* Authentication
* User persistence
* Flight metadata
* IGC file upload/download
* Local blob storage integration

Next planned steps:

* Angular frontend backend integration
* `GET /api/flights` frontend binding
* Metadata synchronization
* Full IGC upload flow from frontend
* Public/private sharing features
* Production deployment configuration
