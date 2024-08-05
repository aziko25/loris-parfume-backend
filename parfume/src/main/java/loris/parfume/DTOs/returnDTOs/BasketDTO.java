package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Items.Items_Images;
import loris.parfume.Models.Items.Sizes_Items;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasketDTO {

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    private List<String> imagesList;

    private Long categoryId;
    private String categoryNameUz;
    private String categoryNameRu;
    private String categoryNameEng;

    private Long sizeId;
    private String sizeNameUz;
    private String sizeNameRu;
    private String sizeNameEng;

    private Integer quantity;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    private Integer discountPercent;

    private List<Map<String, Object>> collectionsItemsList;

    public BasketDTO(Sizes_Items sizeItem, Integer quantity) {

        id = sizeItem.getItem().getId();
        createdTime = sizeItem.getItem().getCreatedTime();
        nameUz = sizeItem.getItem().getNameUz();
        nameRu = sizeItem.getItem().getNameRu();
        nameEng = sizeItem.getItem().getNameEng();
        descriptionUz = sizeItem.getItem().getDescriptionUz();
        descriptionRu = sizeItem.getItem().getDescriptionRu();
        descriptionEng = sizeItem.getItem().getDescriptionEng();
        this.quantity = quantity;
        price = sizeItem.getPrice();
        discountPercent = sizeItem.getDiscountPercent();

        if (!sizeItem.getSize().getIsDefaultNoSize()) {

            sizeId = sizeItem.getSize().getId();
            sizeNameUz = sizeItem.getSize().getNameUz();
            sizeNameRu = sizeItem.getSize().getNameRu();
            sizeNameEng = sizeItem.getSize().getNameEng();
        }

        imagesList = new ArrayList<>();
        if (sizeItem.getItem().getItemsImagesList() != null && !sizeItem.getItem().getItemsImagesList().isEmpty()) {

            Set<Items_Images> itemsImagesSet = new HashSet<>(sizeItem.getItem().getItemsImagesList());

            imagesList = itemsImagesSet.stream()
                    .sorted(Comparator.comparing(Items_Images::getImageName))
                    .map(Items_Images::getImageName)
                    .collect(Collectors.toList());
        }

        if (sizeItem.getItem().getCategory() != null) {

            categoryId = sizeItem.getItem().getCategory().getId();
            categoryNameUz = sizeItem.getItem().getCategory().getNameUz();
            categoryNameRu = sizeItem.getItem().getCategory().getNameRu();
            categoryNameEng = sizeItem.getItem().getCategory().getNameEng();
        }

        collectionsItemsList = new ArrayList<>();
        if (sizeItem.getItem().getCollectionsItemsList() != null && !sizeItem.getItem().getCollectionsItemsList().isEmpty()) {

            Set<Collections_Items> collectionsItems = new HashSet<>(sizeItem.getItem().getCollectionsItemsList());
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
    }
}