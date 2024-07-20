package loris.parfume.DTOs.Requests.Items;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecommendedItemsRequest {

    private Long itemId;
    private List<Long> recommendedItemsIds;
}