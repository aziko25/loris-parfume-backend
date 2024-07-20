package loris.parfume.Repositories;

import loris.parfume.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    boolean existsByPhone(String phone);

    Users findByPhone(String phone);
}