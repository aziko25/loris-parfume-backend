package loris.parfume.Repositories.Items;

import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Recommended_Items;
import loris.parfume.Models.Items.Recommended_Items_Ids;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface Recommended_Items_Repository extends JpaRepository<Recommended_Items, Recommended_Items_Ids> {

    List<Recommended_Items> findAllByItem(Items item);

    void deleteAllByItem(Items item);
}