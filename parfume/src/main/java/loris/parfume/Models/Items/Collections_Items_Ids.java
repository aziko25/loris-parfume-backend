package loris.parfume.Models.Items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collections_Items_Ids {

    private Long collection;
    private Long item;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Collections_Items_Ids that = (Collections_Items_Ids) o;

        return Objects.equals(collection, that.collection) &&
                Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {

        return Objects.hash(collection, item);
    }
}