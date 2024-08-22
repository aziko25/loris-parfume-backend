package loris.parfume.Repositories;

import loris.parfume.Models.Catalogues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CataloguesRepository extends JpaRepository<Catalogues, Long> {
}