package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Items.Sizes_Items;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SizesRepository extends JpaRepository<Sizes, Long> {

    Sizes findByIsDefaultNoSize(boolean b);

    @Query("SELECT s FROM Sizes s WHERE " +
            "(:search IS NULL OR " +
            " s.nameUz ILIKE %:search% OR " +
            " s.nameRu ILIKE %:search% OR " +
            " s.nameEng ILIKE %:search%)")
    Page<Sizes> findAllByFilters(String search, Pageable pageable);
}