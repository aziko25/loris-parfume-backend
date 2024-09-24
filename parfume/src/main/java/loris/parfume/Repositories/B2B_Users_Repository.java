package loris.parfume.Repositories;

import loris.parfume.Models.B2B_Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface B2B_Users_Repository extends JpaRepository<B2B_Users, Long> {
}