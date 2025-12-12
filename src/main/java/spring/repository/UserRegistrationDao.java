package spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.entity.UserEntity;

public interface UserRegistrationDao extends JpaRepository<UserEntity, Integer> {
    boolean existsByLogin(String login);
}
