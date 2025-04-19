package gr.dmst.edu.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashSet;
import java.util.Set;

@RedisHash("active_meeting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMeeting {
    private static final long serialVersionUID = 1L;

    @Id
    private String meetingId;
    private String title;
    private String description;
    private Long startTime; // timestamp in millis
    private Long endTime;   // timestamp in millis
    private Double latitude;
    private Double longitude;
    private Set<String> participants = new HashSet<>(); // All participants
    private Set<String> joinedParticipants = new HashSet<>(); // Only joined participants
}