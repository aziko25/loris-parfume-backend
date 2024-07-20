package loris.parfume.DTOs.Requests.Orders;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Orders_Items_Request {

    private Long itemId;
    private Long collectionId;
    private Long sizeId;
    private Integer quantity;
    private Double totalSum;
}