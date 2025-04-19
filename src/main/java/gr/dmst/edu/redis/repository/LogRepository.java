package gr.dmst.edu.redis.repository;


import gr.dmst.edu.redis.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {
}