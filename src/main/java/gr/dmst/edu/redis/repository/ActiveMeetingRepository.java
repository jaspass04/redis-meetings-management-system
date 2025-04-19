package gr.dmst.edu.redis.repository;

import gr.dmst.edu.redis.model.ActiveMeeting;
import org.springframework.data.repository.CrudRepository;

public interface ActiveMeetingRepository extends CrudRepository<ActiveMeeting, String> {
}