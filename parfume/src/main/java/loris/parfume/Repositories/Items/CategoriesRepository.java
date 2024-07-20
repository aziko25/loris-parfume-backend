package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Long> {

    @Query("SELECT c FROM Categories c WHERE " +
            "((:name IS NULL OR c.nameUz ILIKE %:name%) OR " +
            "(:name IS NULL OR c.nameRu ILIKE %:name%) OR " +
            "(:name IS NULL OR c.nameEng ILIKE %:name%)) " +
            "AND (:collectionId IS NULL OR c.collection.id = :collectionId)")
    Page<Categories> findAllByFilters(
            @Param("name") String name,
            @Param("collectionId") Long collectionId,
            Pageable pageable);

    List<Categories> findAllByCollection(Collections collection);
}