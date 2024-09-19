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
            "JOIN i.collectionsItemsList ci " +
            "JOIN ci.collection c " +
            "LEFT JOIN i.category cat " +
            "WHERE " +
            "(:search IS NULL OR " +
            " i.barcode ILIKE %:search% OR " +
            " i.nameUz ILIKE %:search% OR " +
            " i.nameRu ILIKE %:search% OR " +
            " i.nameEng ILIKE %:search% OR " +
            " i.descriptionUz ILIKE %:search% OR " +
            " i.descriptionRu ILIKE %:search% OR " +
            " i.descriptionEng ILIKE %:search%) " +
            "AND (:collectionSlug IS NULL OR c.slug = :collectionSlug) " +
            "AND (:categorySlug IS NULL OR cat.slug = :categorySlug) " +
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
            @Param("collectionSlug") String collectionSlug,
            @Param("categorySlug") String categorySlug,
            Pageable pageable);

    List<Items> findAllByCategory(Categories category);

    Page<Items> findAllByCollectionsItemsList_CollectionAndCategory(Collections collection, Categories category, Pageable pageable);
    List<Items> findAllByCollectionsItemsList_CollectionAndCategory(Collections collection, Categories category);

    Page<Items> findAllByCollectionsItemsList_Collection(Collections collection, Pageable pageable);

    //List<Items> findAllByIsRecommendedInMainPageAndCollectionsItemsList_Collection(Boolean isRecommended, Collections collection);
    List<Items> findAllByIsRecommendedInMainPageAndCollectionsItemsList_CollectionAndCategory(boolean b, Collections collection, Categories category);

    @Query(value = "SELECT * FROM items i "
            + "JOIN collections_items ci ON i.id = ci.item_id "
            + "WHERE ci.collection_id = :collectionId "
            + "ORDER BY RANDOM() "
            + "LIMIT 8", nativeQuery = true)
    List<Items> findTop8RandomItemsByCollection(@Param("collectionId") Long collectionId);

    Optional<Items> findBySlug(String slug);

    Optional<Items> findByBarcode(String barcode);

    @Query(value = "SELECT i.* FROM items i " +
            "JOIN collections_items ci ON ci.item_id = i.id " +
            "WHERE ci.collection_id = :collectionId AND i.category_id = :categoryId " +
            "AND (:excludedIds IS NULL OR i.id NOT IN (:excludedIds)) " +
            "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Items> findRandomByCollectionAndCategoryExcludeItems(@Param("collectionId") Long collectionId,
                                                              @Param("categoryId") Long categoryId,
                                                              @Param("excludedIds") List<Long> excludedIds,
                                                              @Param("limit") int limit);

    @Query(value = "SELECT i.* FROM Items i " +
            "JOIN collections_items ci ON ci.item_id = i.id " +
            "WHERE ci.collection_id = :collectionId " +
            "AND (:excludedIds IS NULL OR i.id NOT IN (:excludedIds)) " +
            "ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Items> findRandomByCollectionExcludeItems(@Param("collectionId") Long collectionId,
                                                   @Param("excludedIds") List<Long> excludedIds,
                                                   @Param("limit") int limit);
}