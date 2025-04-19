package gr.dmst.edu.redis.repository;


import gr.dmst.edu.redis.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, String> {
    @Query("SELECT m FROM Meeting m WHERE m.startTime <= ?1 AND m.endTime >= ?1")
    List<Meeting> findActiveMeetings(LocalDateTime now);
}
