package loris.parfume.Repositories;

import loris.parfume.Models.Branches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {

    List<Branches> findAllByNameLikeIgnoreCase(String name);
}