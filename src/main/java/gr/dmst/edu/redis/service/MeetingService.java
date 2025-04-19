package gr.dmst.edu.redis.service;

// src/main/java/com/meetingapp/service/MeetingService.java

import gr.dmst.edu.redis.model.ActiveMeeting;
import gr.dmst.edu.redis.model.ChatMessage;
import gr.dmst.edu.redis.model.Log;
import gr.dmst.edu.redis.model.Meeting;
import gr.dmst.edu.redis.repository.ActiveMeetingRepository;
import gr.dmst.edu.redis.repository.LogRepository;
import gr.dmst.edu.redis.repository.MeetingRepository;
import gr.dmst.edu.redis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final ActiveMeetingRepository activeMeetingRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_KEY_PREFIX = "chat:";
    private static final double MAX_DISTANCE_METERS = 100.0;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void updateActiveMeetings() {
        LocalDateTime now = LocalDateTime.now();

        // Find meetings that should be active
        List<Meeting> activeMeetings = meetingRepository.findActiveMeetings(now);

        // Add meetings to Redis that aren't already active
        for (Meeting meeting : activeMeetings) {
            if (!activeMeetingRepository.existsById(meeting.getMeetingId())) {
                ActiveMeeting activeMeeting = convertToActiveMeeting(meeting);
                activeMeetingRepository.save(activeMeeting);
            }
        }

        // Find meetings that should be deactivated
        List<String> activeIds = activeMeetings.stream()
                .map(Meeting::getMeetingId)
                .collect(Collectors.toList());

        // Get all active meeting IDs from Redis
        List<String> redisIds = StreamSupport.stream(activeMeetingRepository.findAll().spliterator(), false)
                .map(ActiveMeeting::getMeetingId)
                .collect(Collectors.toList());

        // Deactivate meetings that should no longer be active
        for (String id : redisIds) {
            if (!activeIds.contains(id)) {
                endMeeting(id);
            }
        }
    }

    private ActiveMeeting convertToActiveMeeting(Meeting meeting) {
        ActiveMeeting activeMeeting = new ActiveMeeting();
        activeMeeting.setMeetingId(meeting.getMeetingId());
        activeMeeting.setTitle(meeting.getTitle());
        activeMeeting.setDescription(meeting.getDescription());
        activeMeeting.setStartTime(meeting.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        activeMeeting.setEndTime(meeting.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        activeMeeting.setLatitude(meeting.getLatitude());
        activeMeeting.setLongitude(meeting.getLongitude());

        // Parse participants
        Set<String> participants = Arrays.stream(meeting.getParticipants().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        activeMeeting.setParticipants(participants);

        return activeMeeting;
    }

    // Function 1: Find nearby active meetings for a user
    public List<String> findNearbyMeetings(String email, double x, double y) {
        List<String> result = new ArrayList<>();

        // Get all active meetings
        Iterable<ActiveMeeting> meetings = activeMeetingRepository.findAll();

        for (ActiveMeeting meeting : meetings) {
            // Check if user is a participant
            if (meeting.getParticipants().contains(email)) {
                // Calculate distance
                double distance = calculateDistance(x, y, meeting.getLatitude(), meeting.getLongitude());
                if (distance <= MAX_DISTANCE_METERS) {
                    result.add(meeting.getMeetingId());
                }
            }
        }

        return result;
    }

    // Function 2: User joins a meeting
    public boolean joinMeeting(String email, String meetingId) {
        Optional<ActiveMeeting> optionalMeeting = activeMeetingRepository.findById(meetingId);
        if (optionalMeeting.isEmpty()) {
            return false; // Meeting not active
        }

        ActiveMeeting meeting = optionalMeeting.get();

        // Check if user is a participant
        if (!meeting.getParticipants().contains(email)) {
            return false;
        }

        // Add to joined participants
        meeting.getJoinedParticipants().add(email);
        activeMeetingRepository.save(meeting);

        // Log the action
        Log log = new Log();
        log.setEmail(email);
        log.setMeetingId(meetingId);
        log.setTimestamp(LocalDateTime.now());
        log.setAction(Log.JOIN_MEETING);
        logRepository.save(log);

        return true;
    }

    // Function 3: User leaves a meeting
    public boolean leaveMeeting(String email, String meetingId) {
        Optional<ActiveMeeting> optionalMeeting = activeMeetingRepository.findById(meetingId);
        if (optionalMeeting.isEmpty()) {
            return false; // Meeting not active
        }

        ActiveMeeting meeting = optionalMeeting.get();

        // Check if user is joined
        if (!meeting.getJoinedParticipants().contains(email)) {
            return false;
        }

        // Remove from joined participants
        meeting.getJoinedParticipants().remove(email);
        activeMeetingRepository.save(meeting);

        // Log the action
        Log log = new Log();
        log.setEmail(email);
        log.setMeetingId(meetingId);
        log.setTimestamp(LocalDateTime.now());
        log.setAction(Log.LEAVE_MEETING);
        logRepository.save(log);

        return true;
    }

    // Function 4: List joined participants
    public List<String> getJoinedParticipants(String meetingId) {
        Optional<ActiveMeeting> optionalMeeting = activeMeetingRepository.findById(meetingId);
        if (optionalMeeting.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(optionalMeeting.get().getJoinedParticipants());
    }

    // Function 5: List all active meetings
    public List<String> getAllActiveMeetings() {
        return StreamSupport.stream(activeMeetingRepository.findAll().spliterator(), false)
                .map(ActiveMeeting::getMeetingId)
                .collect(Collectors.toList());
    }

    // Function 6: End a meeting
    public boolean endMeeting(String meetingId) {
        Optional<ActiveMeeting> optionalMeeting = activeMeetingRepository.findById(meetingId);
        if (optionalMeeting.isEmpty()) {
            return false;
        }

        ActiveMeeting meeting = optionalMeeting.get();

        // Log timeout for all joined participants
        LocalDateTime now = LocalDateTime.now();
        for (String email : meeting.getJoinedParticipants()) {
            Log log = new Log();
            log.setEmail(email);
            log.setMeetingId(meetingId);
            log.setTimestamp(now);
            log.setAction(Log.TIME_OUT);
            logRepository.save(log);
        }

        // Delete the meeting from Redis
        activeMeetingRepository.deleteById(meetingId);

        // Delete chat messages
        redisTemplate.delete(CHAT_KEY_PREFIX + meetingId);

        return true;
    }

    // Function 7: Post a message to chat
    public boolean postMessage(String email, String text) {
        // Find which meeting the user has joined
        Optional<ActiveMeeting> joinedMeeting = StreamSupport.stream(activeMeetingRepository.findAll().spliterator(), false)
                .filter(m -> m.getJoinedParticipants().contains(email))
                .findFirst();

        if (joinedMeeting.isEmpty()) {
            return false; // User has not joined any meeting
        }

        String meetingId = joinedMeeting.get().getMeetingId();
        String chatKey = CHAT_KEY_PREFIX + meetingId;

        ChatMessage message = new ChatMessage(email, text, System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(chatKey, message);

        return true;
    }

    // Function 8: Get all chat messages for a meeting
    public List<ChatMessage> getMeetingChatMessages(String meetingId) {
        String chatKey = CHAT_KEY_PREFIX + meetingId;

        Long size = redisTemplate.opsForList().size(chatKey);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }

        List<Object> messages = redisTemplate.opsForList().range(chatKey, 0, size - 1);
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages.stream()
                .filter(obj -> obj instanceof ChatMessage)
                .map(obj -> (ChatMessage) obj)
                .collect(Collectors.toList());
    }

    // Function 9: Get all messages posted by a user
    public List<ChatMessage> getUserMessages(String email) {
        // Find which meeting the user has joined
        Optional<ActiveMeeting> joinedMeeting = StreamSupport.stream(activeMeetingRepository.findAll().spliterator(), false)
                .filter(m -> m.getJoinedParticipants().contains(email))
                .findFirst();

        if (joinedMeeting.isEmpty()) {
            return Collections.emptyList();
        }

        String meetingId = joinedMeeting.get().getMeetingId();

        // Get all messages from the meeting
        List<ChatMessage> allMessages = getMeetingChatMessages(meetingId);

        // Filter only user's messages
        return allMessages.stream()
                .filter(msg -> email.equals(msg.getEmail()))
                .collect(Collectors.toList());
    }

    // Helper method to calculate distance between two points
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        // Simple Euclidean distance - in a real app would use Haversine formula for geographic coordinates
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}