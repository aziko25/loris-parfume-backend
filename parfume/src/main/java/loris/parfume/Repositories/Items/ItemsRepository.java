package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Long> {

    @Query("SELECT i FROM Items i JOIN i.collectionsItemsList ci WHERE " +
            "(:search IS NULL OR " +
            " i.barcode ILIKE %:search% OR " +
            " i.nameUz ILIKE %:search% OR " +
            " i.nameRu ILIKE %:search% OR " +
            " i.nameEng ILIKE %:search% OR " +
            " i.descriptionUz ILIKE %:search% OR " +
            " i.descriptionRu ILIKE %:search% OR " +
            " i.descriptionEng ILIKE %:search%) " +
            "AND (:collectionId IS NULL OR ci.collection.id = :collectionId) " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
            "ORDER BY " +
            "CASE WHEN :firstA IS NOT NULL AND :firstA = TRUE THEN i.nameUz END ASC, " +
            "CASE WHEN :firstZ IS NOT NULL AND :firstZ = TRUE THEN i.nameUz END DESC, " +
            "CASE WHEN :firstExpensive IS NOT NULL AND :firstExpensive = TRUE THEN i.price END DESC, " +
            "CASE WHEN :firstCheap IS NOT NULL AND :firstCheap = TRUE THEN i.price END ASC")
    Page<Items> findAllItemsByFilters(
            @Param("search") String search,
            @Param("firstA") Boolean firstA,
            @Param("firstZ") Boolean firstZ,
            @Param("firstExpensive") Boolean firstExpensive,
            @Param("firstCheap") Boolean firstCheap,
            @Param("collectionId") Long collectionId,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    List<Items> findAllByCategory(Categories category);

    Page<Items> findAllByCollectionsItemsList_CollectionAndCategory(Collections collection, Categories category, Pageable pageable);

    Page<Items> findAllByCollectionsItemsList_Collection(Collections collection, Pageable pageable);

    Optional<Items> findBySlug(String slug);

    Optional<Items> findByBarcode(String barcode);
}