# API Documentation - Redis Meetings Management System
This document provides examples for using all available API endpoints in the Redis Meetings Management System. Some have dependencies with others, for example you have to create a meeting in order to access it. This means you have to be careful when deleting something: keep this in mind while trying to see the rest of functionalities (for example deleting a user, re-create him in order to see his access rights or his chatting functionalities)
### User Management

#### Create a User

```
  curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "name": "John Doe",
    "photoUrl": "https://example.com/john.jpg"
  }'
```
#### Delete a User 

```
  curl -X DELETE http://localhost:8080/api/users/john.doe@example.com
```

### Meeting Management
once we've set our basic users, let's set up the meetings!

#### Create a Meeting (to db, non redis function)

```
curl -X POST http://localhost:8080/api/meetings \
  -H "Content-Type: application/json" \
  -d '{
    "meetingId": "meet-001",
    "title": "Weekly Sprint Planning",
    "description": "Review progress and plan next sprint",
    "startTime": "2025-01-01T14:00:00Z",
    "endTime": "2023-12-30T15:30:00Z",
    "latitude": 37.7749,
    "longitude": -122.4194,
    "participants": "john.doe@example.com,jane.smith@example.com"
  }'
```
#### Get all Meetings (from DB, non redis function)

```
curl -X GET http://localhost:8080/api/meetings
```

#### Delete a Meeting (from DB, non redis function)

```
curl -X DELETE http://localhost:8080/api/meetings/meet-001
```

#### Manually activate a meeting (store it to redis, if you're in a rush and cannot wait to see scheduler in action)
```
curl -X POST http://localhost:8080/api/meetings/meet-001/activate
```

#### End an Active Meeting (Redis Function)

```
curl -X POST http://localhost:8080/api/meetings/meet-001/end
```

#### Get All Active Meetings (Redis Function0

```
curl -X GET http://localhost:8080/api/meetings/active
```

### Meeting Participation 

#### Find Nearby Meetings

```
curl -X GET "http://localhost:8080/api/meetings/nearby?email=john.doe@example.com&x=37.7749&y=-122.4194"

```

#### Join a Meeting

```
curl -X POST "http://localhost:8080/api/meetings/meet-001/join?email=john.doe@example.com"
```

#### Leave a Meeting

```
curl -X POST "http://localhost:8080/api/meetings/meet-001/leave?email=john.doe@example.com"
```

#### Get Joined Participants

```
curl -X GET http://localhost:8080/api/meetings/meet-001/joined
```

### Chat System

#### Post Message to Meeting Chat

```
curl -X POST http://localhost:8080/api/meetings/meet-001/chat/post \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "message": "Hello everyone, I am joining the meeting now."
  }' 
```
####Get All Meeting Chat Messages

```
curl -X GET http://localhost:8080/api/meetings/meet-001/chat
```

#### Get All User Messages

```
curl -X GET http://localhost:8080/api/users/john.doe@example.com/messages
```

#### Get User Messages in a Specific Meeting

```
curl -X GET http://localhost:8080/api/meetings/meet-001/chat/user/john.doe@example.com
```

### Debug Endpoints

#### Debug Redis State

```
curl -X GET http://localhost:8080/api/redis/debug
```
