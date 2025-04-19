# Meeting Management System with Redis

## Description
### Overview
A complete meeting management platform that combines persistent storage (PostgreSQL) with real-time features (Redis) to manage physical meetings. The system tracks active meetings, handles participant joining/leaving, provides chat functionality, and logs all meeting-related activities.

### Features
The core features of the system include:

- **Meeting Management:** Create, schedule, and end meetings.
- **Real-time Participation:** Join and leave meetings based on physical proximity.
- **Chat room:** Message exchange between meeting participants.
- **Location-based Discovery:** Find nearby active meetings based on coordinates
- **Activity Logging:** Track all user actions (join, leave, timeout)
- **Automatic Meeting Activation:** Scheduled activation/deactivation based on meeting times

### Tech Stack

- **Java 17**
- **Spring Boot 3.4.4**
- **Redis** - For active meeting management and chat messages
- **Postgres** - For persistent storage of meetings and logs
- **Docker** - for containerized environments
- **Maven** - For dependency management and build automation

### Setup and Installation
To get started with this project, follow the instructions below.

#### Prerequisites

- Install **Redis** on your local machine (or use a cloud Redis provider).
- Make sure you have **Java 17+ , Docker and Docker Compose, Maven and Postgres** installed.
- 

### Setup

1. Clone the repository:

   ```
   git clone https://github.com/jaspass04/redis-meetings-management-system.git
   ```

   ```
   cd redis-meetings-management-system
   ```
3. Start the Redis and Postgres containers:

   ```
   docker-compose up -d
   ```
5. Build and run the application:
    ```
    mvn spring-boot:run
    ```
6. The application will be available at http://localhost:8080

### API endpoints 
A complete set of examples for API endpoints for postman in the **API_ENDPOINTS.md** file.
#### User Management
- POST /api/users - create a new user (param: email)
- DELETE /api/users - delete a user (param: email)
  
#### Meeting Management
- POST /api/meetings - helper function to Create a new meeting
- GET /api/meetings - helper function to Get all meetings (from postgres)
- DELETE /api/meetings/<meeting-id> - helper function to Delete a meeting from postgres
- POST /api/meetings/<meeting-id>/activate - helper function to manually activate a meeting and not wait for one minute to get activated - else redis is scheduled every minute to activate meeting that have to be activated
- POST /api/meetings/<meeting-id>/end -End an active meeting
- GET /api/meetings/active - Get all active meetings

#### Participation
- GET /api/meetings/nearby (params: e-mail, lat, long)
- POST /api/meetings/<meeting-id>/join - Join a meeting (param: e-mail)
- POST /api/meetings/<meeting-id>/leave - Leave a meeting (param: e-mail)
- GET /api/meetings/<meeting-id>/joined - Get joined participants

#### Chat System 
- POST /api/meetings/<meeting-id>/chat/post - Post a message to meeting chat
- GET /api/meetings/<meeting-id>/chat - Join a meeting (param: e-mail)
- GET /api/users/<meeting-id>/leave - Leave a meeting (param: e-mail)
- GET /api/meetings/<meeting-id>/chat/user/<e-mail> - Get user messages in a specific meeting

#### Debug
- GET /api/redis/debug - debug redis state (get keys and active meetings)

### Data Model

#### PostgreSQL Entities

- **Meeting:** Basic meeting information and schedule
- **User:** User information
- **Log:** Activity Logs

#### Redis Entities

- **ActiveMeeting:** Currently Active Meetings
- **ChatMessage:** Messages exchanged in meetings

### Configuration
Configuration is managed through application.properties with settings for:
- PostgreSQL connection
- Redis connection
- Server port

### Development
The app uses spring boot's built-in scheduling to automatically activate-deactivate meetings based on their scheduled times. Messages are stored in Redis lists with appropriate serialization - deserialization for json.

### Docker Support 
the included docker-compose.yml file sets up:
- PostgreSQL database with persistent volumes
- Redis instance with persistent volume

### License
This project is made for a big-data management systems assignment for educational purposes. 
