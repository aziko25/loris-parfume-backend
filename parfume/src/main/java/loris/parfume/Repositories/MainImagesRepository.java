package loris.parfume.Repositories;

import loris.parfume.Models.MainImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainImagesRepository extends JpaRepository<MainImages, Long> {
}