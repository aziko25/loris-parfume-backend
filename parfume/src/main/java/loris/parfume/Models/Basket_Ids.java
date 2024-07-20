package loris.parfume.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Basket_Ids implements Serializable {

    private Long user;
    private Long item;
    private Long size;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Basket_Ids basketIds = (Basket_Ids) o;

        return Objects.equals(user, basketIds.user) && Objects.equals(item, basketIds.item) && Objects.equals(size, basketIds.size);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, item, size);
    }
}