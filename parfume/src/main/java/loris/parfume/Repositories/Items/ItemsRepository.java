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

    @Query("SELECT i FROM Items i " +
            "LEFT JOIN i.collectionsItemsList ci " +
            "LEFT JOIN ci.collection c " +
            "LEFT JOIN i.category cat " +
            "WHERE " +
            "(:ids IS NULL OR i.id IN :ids) " +
            "AND (:collectionSlug IS NULL OR c.slug = :collectionSlug) " +
            "AND (:categorySlug IS NULL OR cat.slug = :categorySlug) " +
            "ORDER BY " +
            "CASE WHEN :firstA = TRUE THEN i.nameUz END ASC, " +
            "CASE WHEN :firstZ = TRUE THEN i.nameUz END DESC, " +
            "CASE WHEN :firstExpensive = TRUE THEN i.price END DESC, " +
            "CASE WHEN :firstCheap = TRUE THEN i.price END ASC, " +
            "i.id") // Default sorting by ID
    Page<Items> findAllItemsByFiltersAndIds(
            @Param("ids") List<Long> ids,
            @Param("firstA") Boolean firstA,
            @Param("firstZ") Boolean firstZ,
            @Param("firstExpensive") Boolean firstExpensive,
            @Param("firstCheap") Boolean firstCheap,
            @Param("collectionSlug") String collectionSlug,
            @Param("categorySlug") String categorySlug,
            Pageable pageable);

    @Query("SELECT i FROM Items i " +
            "LEFT JOIN i.collectionsItemsList ci " +
            "LEFT JOIN ci.collection c " +
            "LEFT JOIN i.category cat " +
            "WHERE " +
            "(:ids IS NULL OR i.id IN :ids) " +
            "AND i.isActive = true " +
            "AND (:collectionSlug IS NULL OR c.slug = :collectionSlug) " +
            "AND (:categorySlug IS NULL OR cat.slug = :categorySlug) " +
            "ORDER BY " +
            "CASE WHEN :firstA = TRUE THEN i.nameUz END ASC, " +
            "CASE WHEN :firstZ = TRUE THEN i.nameUz END DESC, " +
            "CASE WHEN :firstExpensive = TRUE THEN i.price END DESC, " +
            "CASE WHEN :firstCheap = TRUE THEN i.price END ASC, " +
            "i.id") // Default sorting by ID
    Page<Items> findAllItemsByFiltersAndIdsAndIsActive(
            @Param("ids") List<Long> ids,
            @Param("firstA") Boolean firstA,
            @Param("firstZ") Boolean firstZ,
            @Param("firstExpensive") Boolean firstExpensive,
            @Param("firstCheap") Boolean firstCheap,
            @Param("collectionSlug") String collectionSlug,
            @Param("categorySlug") String categorySlug,
            Pageable pageable
    );

    List<Items> findAllByCategory(Categories category);

    Page<Items> findAllByCollectionsItemsList_CollectionAndCategory(Collections collection, Categories category, Pageable pageable);
    Page<Items> findAllByCollectionsItemsList_CollectionAndCategoryAndIsActive(Collections collection, Categories category, boolean isActive, Pageable pageable);

    Page<Items> findAllByCollectionsItemsList_Collection(Collections collection, Pageable pageable);
    Page<Items> findAllByCollectionsItemsList_CollectionAndIsActive(Collections collection, boolean isActive, Pageable pageable);

    @Query(value = "SELECT * FROM items i "
            + "JOIN collections_items ci ON i.id = ci.item_id "
            + "WHERE ci.collection_id = :collectionId "
            + "ORDER BY RANDOM() "
            + "LIMIT 8", nativeQuery = true)
    List<Items> findTop8RandomItemsByCollection(@Param("collectionId") Long collectionId);

    Optional<Items> findBySlug(String slug);

    Optional<Items> findByBarcode(String barcode);

    Page<Items> findAllByIsActive(boolean isActive, Pageable pageable);
}