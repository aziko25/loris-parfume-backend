package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Items_Images;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Items_Images_Repository extends JpaRepository<Items_Images, Long> {

    List<Items_Images> findAllByItem(Items item);

    void deleteAllByItem(Items item);
}