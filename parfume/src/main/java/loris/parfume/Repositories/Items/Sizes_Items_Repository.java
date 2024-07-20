package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Models.Items.Sizes_Items_Ids;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Sizes_Items_Repository extends JpaRepository<Sizes_Items, Sizes_Items_Ids> {

    void deleteAllByItem(Items item);

    void deleteAllBySize(Sizes size);

    List<Sizes_Items> findAllBySize(Sizes size);

    Sizes_Items findByItemAndSize(Items item, Sizes size);
}