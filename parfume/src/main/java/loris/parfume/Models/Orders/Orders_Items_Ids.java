package loris.parfume.Models.Orders;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class Orders_Items_Ids implements Serializable {

    private Long order;
    private Long item;
    private Long size;

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orders_Items_Ids that = (Orders_Items_Ids) o;

        return Objects.equals(order, that.order) && Objects.equals(item, that.item) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {

        return Objects.hash(order, item, size);
    }
}