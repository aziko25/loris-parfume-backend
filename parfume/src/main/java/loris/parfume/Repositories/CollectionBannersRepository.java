package loris.parfume.Repositories;

import loris.parfume.Models.CollectionBanners;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionBannersRepository extends JpaRepository<CollectionBanners, Long> {

    @Query("SELECT cb FROM CollectionBanners cb WHERE " +
            "((:title IS NULL OR cb.titleUz ILIKE %:title%) OR " +
            "(:title IS NULL OR cb.titleRu ILIKE %:title%) OR " +
            "(:title IS NULL OR cb.titleEng ILIKE %:title%)) " +
            "AND (:isActive IS NULL OR cb.isActive = :isActive)")
    Page<CollectionBanners> findAllByTitleLikeIgnoreCaseOrIsActive(
            @Param("title") String title,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}