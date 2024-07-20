package loris.parfume.DTOs.Requests.Items;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sizes_Items_Request {

    private Long itemId;
    private Double price;
    private Integer quantity;
    private Integer discountPercent;
}