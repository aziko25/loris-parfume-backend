package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionsRepository extends JpaRepository<Collections, Long> {

    @Query("SELECT c FROM Collections c WHERE " +
            "(:name IS NULL OR c.nameUz ILIKE %:name%) OR " +
            "(:name IS NULL OR c.nameRu ILIKE %:name%) OR " +
            "(:name IS NULL OR c.nameEng ILIKE %:name%)")
    Page<Collections> findAllByAnyNameLikeIgnoreCase(@Param("name") String name, Pageable pageable);
}