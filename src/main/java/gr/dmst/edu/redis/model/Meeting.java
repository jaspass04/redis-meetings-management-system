package gr.dmst.edu.redis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {
    @Id
    private String meetingId;
    private String title;
    private String description;
    private LocalDateTime startTime; // t1
    private LocalDateTime endTime;   // t2
    private Double latitude;
    private Double longitude;
    private String participants; // Comma-separated list of emails
}