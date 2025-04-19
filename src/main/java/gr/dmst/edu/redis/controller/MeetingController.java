package gr.dmst.edu.redis.controller;

import gr.dmst.edu.redis.model.ActiveMeeting;
import gr.dmst.edu.redis.model.ChatMessage;
import gr.dmst.edu.redis.model.Meeting;
import gr.dmst.edu.redis.model.User;
import gr.dmst.edu.redis.repository.ActiveMeetingRepository;
import gr.dmst.edu.redis.repository.MeetingRepository;
import gr.dmst.edu.redis.repository.UserRepository;
import gr.dmst.edu.redis.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final ActiveMeetingRepository activeMeetingRepository;
    // User and meeting management
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @DeleteMapping("/users/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        if (!userRepository.existsById(email)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/meetings")
    public Meeting createMeeting(@RequestBody Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @DeleteMapping("/meetings/{id}")
    public ResponseEntity<?> deleteMeeting(@PathVariable String id) {
        if (!meetingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        meetingRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Function 1: Find nearby active meetings
    @GetMapping("/meetings/nearby")
    public List<String> getNearbyMeetings(
            @RequestParam String email,
            @RequestParam double x,
            @RequestParam double y) {
        return meetingService.findNearbyMeetings(email, x, y);
    }

    // Function 2: Join a meeting
    @PostMapping("/meetings/{meetingId}/join")
    public ResponseEntity<?> joinMeeting(
            @PathVariable String meetingId,
            @RequestParam String email) {
        boolean success = meetingService.joinMeeting(email, meetingId);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Unable to join meeting");
    }

    // Function 3: Leave a meeting
    @PostMapping("/meetings/{meetingId}/leave")
    public ResponseEntity<?> leaveMeeting(
            @PathVariable String meetingId,
            @RequestParam String email) {
        boolean success = meetingService.leaveMeeting(email, meetingId);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Unable to leave meeting");
    }

    // Function 4: Get joined participants
    @GetMapping("/meetings/{meetingId}/joined")
    public List<String> getJoinedParticipants(@PathVariable String meetingId) {
        return meetingService.getJoinedParticipants(meetingId);
    }

    // Function 5: Get all active meetings
    @GetMapping("/meetings/active")
    public List<String> getActiveMeetings() {
        return meetingService.getAllActiveMeetings();
    }

    // Function 6: End a meeting
    @PostMapping("/meetings/{meetingId}/end")
    public ResponseEntity<?> endMeeting(@PathVariable String meetingId) {
        boolean success = meetingService.endMeeting(meetingId);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Unable to end meeting");
    }

    // Function 7: Post message
    @PostMapping("/chat/post")
    public ResponseEntity<?> postMessage(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String message = payload.get("message");

        boolean success = meetingService.postMessage(email, message);
        if (success) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Unable to post message");
    }

    // Function 8: Get meeting messages
    @GetMapping("/meetings/{meetingId}/chat")
    public List<ChatMessage> getMeetingMessages(@PathVariable String meetingId) {
        return meetingService.getMeetingChatMessages(meetingId);
    }

    // Function 9: Get user messages
    @GetMapping("/users/{email}/messages")
    public List<ChatMessage> getUserMessages(@PathVariable String email) {
        return meetingService.getUserMessages(email);
    }
    //Bonus function
    @PostMapping("/meetings/{meetingId}/activate")
    public ResponseEntity<?> activateMeeting(@PathVariable String meetingId) {
        Optional<Meeting> optionalMeeting = meetingRepository.findById(meetingId);
        if (optionalMeeting.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Meeting meeting = optionalMeeting.get();
        ActiveMeeting activeMeeting = new ActiveMeeting();
        activeMeeting.setMeetingId(meeting.getMeetingId());
        activeMeeting.setTitle(meeting.getTitle());
        activeMeeting.setDescription(meeting.getDescription());
        activeMeeting.setStartTime(meeting.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        activeMeeting.setEndTime(meeting.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        activeMeeting.setLatitude(meeting.getLatitude());
        activeMeeting.setLongitude(meeting.getLongitude());

        Set<String> participants = Arrays.stream(meeting.getParticipants().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        activeMeeting.setParticipants(participants);

        activeMeetingRepository.save(activeMeeting);

        return ResponseEntity.ok("Meeting " + meetingId + " manually activated");
    }
}