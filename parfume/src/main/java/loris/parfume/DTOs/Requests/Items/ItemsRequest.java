package loris.parfume.DTOs.Requests.Items;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ItemsRequest {

    private String slug;
    private String barcode;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    private Double price;
    private Integer discountPercent;

    private Long categoryId;
    private List<Long> collectionIds;

    private List<Map<String, Object>> sizesMap;
}