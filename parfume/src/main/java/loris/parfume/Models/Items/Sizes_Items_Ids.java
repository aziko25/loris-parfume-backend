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
public class Sizes_Items_Ids implements Serializable {

    private Long size;
    private Long item;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Sizes_Items_Ids that = (Sizes_Items_Ids) o;

        return Objects.equals(size, that.size) &&
                Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {

        return Objects.hash(size, item);
    }
}