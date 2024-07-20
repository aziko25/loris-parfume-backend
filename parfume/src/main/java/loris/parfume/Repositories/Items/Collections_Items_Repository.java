package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Items.Collections_Items_Ids;
import loris.parfume.Models.Items.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Collections_Items_Repository extends JpaRepository<Collections_Items, Collections_Items_Ids> {

    void deleteAllByItem(Items item);

    List<Collections_Items> findAllByCollection(Collections collection);
}