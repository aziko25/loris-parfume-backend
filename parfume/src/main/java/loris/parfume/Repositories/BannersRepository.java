package loris.parfume.Repositories;

import loris.parfume.Models.Banners;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BannersRepository extends JpaRepository<Banners, Long> {

    @Query("SELECT b FROM Banners b WHERE " +
            "((:title IS NULL OR b.titleUz ILIKE %:title%) OR " +
            "(:title IS NULL OR b.titleRu ILIKE %:title%) OR " +
            "(:title IS NULL OR b.titleEng ILIKE %:title%)) " +
            "AND (:isActive IS NULL OR b.isActive = :isActive)")
    Page<Banners> findAllByTitleLikeIgnoreCaseOrIsActive(
            @Param("title") String title,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}