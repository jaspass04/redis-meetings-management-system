package gr.dmst.edu.redis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String meetingId;
    private LocalDateTime timestamp;
    private Integer action; // 1=join, 2=leave, 3=timeout

    public static final int JOIN_MEETING = 1;
    public static final int LEAVE_MEETING = 2;
    public static final int TIME_OUT = 3;
}