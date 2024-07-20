package loris.parfume.Repositories;

import loris.parfume.Models.Branches;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {

    Page<Branches> findAllByNameLikeIgnoreCase(String name, Pageable pageable);
}