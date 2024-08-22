package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Items.Collections_Items_Ids;
import loris.parfume.Models.Items.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Collections_Items_Repository extends JpaRepository<Collections_Items, Collections_Items_Ids> {

    void deleteAllByItem(Items item);

    List<Collections_Items> findAllByCollection(Collections collection);

    @Query("SELECT ci FROM Collections_Items ci WHERE ci.collection.id = :collectionId AND ci.item.id = :itemId")
    Optional<Collections_Items> findByCollectionIdAndItemId(@Param("collectionId") Long collectionId, @Param("itemId") Long itemId);

    @Query("SELECT ci FROM Collections_Items ci WHERE ci.collection.slug = :collectionSlug AND ci.item.slug = :itemSlug")
    Optional<Collections_Items> findByCollectionSlugAndItemSlug(@Param("collectionSlug") String collectionSlug,
                                                                @Param("itemSlug") String itemSlug);

}