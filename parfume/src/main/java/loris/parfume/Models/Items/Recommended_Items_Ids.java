package loris.parfume.Models.Items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recommended_Items_Ids implements Serializable {

    private Long item;
    private Long recommendedItem;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommended_Items_Ids that = (Recommended_Items_Ids) o;

        return Objects.equals(item, that.item) && Objects.equals(recommendedItem, that.recommendedItem);
    }

    @Override
    public int hashCode() {

        return Objects.hash(item, recommendedItem);
    }
}