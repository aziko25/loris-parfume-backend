package loris.parfume.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Wishlist_Ids {

    private Long user;
    private Long item;
    private Long size;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wishlist_Ids that = (Wishlist_Ids) o;
        return Objects.equals(user, that.user) && Objects.equals(item, that.item) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, item, size);
    }
}