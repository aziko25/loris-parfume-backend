package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes_Items;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import loris.parfume.Configurations.Serializers.DoubleSerializer;

@Getter
@Setter
public class ItemsDTO {

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    private Integer quantity;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    private Integer discountPercent;
    private String imageName;

    private Long categoryId;
    private String categoryNameUz;
    private String categoryNameRu;
    private String categoryNameEng;

    private List<Map<String, Object>> collectionsItemsList;
    private List<Map<String, Object>> sizesItemsList;

    public ItemsDTO(Items item) {

        id = item.getId();
        createdTime = item.getCreatedTime();

        nameUz = item.getNameUz();
        nameRu = item.getNameRu();
        nameEng = item.getNameEng();

        descriptionUz = item.getDescriptionUz();
        descriptionRu = item.getDescriptionRu();
        descriptionEng = item.getDescriptionEng();

        quantity = item.getQuantity();
        price = item.getPrice();
        discountPercent = item.getDiscountPercent();
        imageName = item.getImageName();

        if (item.getCategory() != null) {

            categoryId = item.getCategory().getId();
            categoryNameUz = item.getCategory().getNameUz();
            categoryNameRu = item.getCategory().getNameRu();
            categoryNameEng = item.getCategory().getNameEng();
        }

        collectionsItemsList = new ArrayList<>();
        if (item.getCollectionsItemsList() != null && !item.getCollectionsItemsList().isEmpty()) {

            Set<Collections_Items> collectionsItems = new HashSet<>(item.getCollectionsItemsList());
            collectionsItemsList = collectionsItems.stream()
                    .sorted(Comparator.comparing((Collections_Items pm) -> pm.getCollection().getId())
                            .thenComparing(pm -> pm.getCollection().getNameUz()))
                    .map(collection -> {

                        Map<String, Object> map = new LinkedHashMap<>();

                        map.put("collectionId", collection.getCollection().getId());

                        map.put("collectionNameUz", collection.getCollection().getNameUz());
                        map.put("collectionNameRu", collection.getCollection().getNameRu());
                        map.put("collectionNameEng", collection.getCollection().getNameEng());

                        return map;
                    }).collect(Collectors.toList());
        }

        sizesItemsList = new ArrayList<>();
        if (item.getSizesItemsList() != null && !item.getSizesItemsList().isEmpty()) {

            Set<Sizes_Items> sizesItems = new HashSet<>(item.getSizesItemsList());
            sizesItemsList = sizesItems.stream()
                    .sorted(Comparator.comparing((Sizes_Items si) -> si.getSize().getId())
                            .thenComparing(si -> si.getSize().getNameUz()))
                    .map(size -> {

                        Map<String, Object> map = new LinkedHashMap<>();

                        map.put("sizeId", size.getSize().getId());

                        map.put("sizeNameUz", size.getSize().getNameUz());
                        map.put("sizeNameRu", size.getSize().getNameRu());
                        map.put("sizeNameEng", size.getSize().getNameEng());

                        map.put("quantity", size.getQuantity());
                        map.put("price", size.getPrice());
                        map.put("discountPercent", size.getDiscountPercent());

                        return map;
                    }).collect(Collectors.toList());
        }
    }
}