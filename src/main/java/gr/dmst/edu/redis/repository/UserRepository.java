package gr.dmst.edu.redis.repository;


import gr.dmst.edu.redis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
