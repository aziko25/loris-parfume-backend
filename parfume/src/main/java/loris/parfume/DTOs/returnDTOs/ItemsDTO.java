package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Items_Images;
import loris.parfume.Models.Items.Sizes_Items;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static loris.parfume.Configurations.Serializers.DoubleSerializer.getFormattedPrice;

import loris.parfume.Configurations.Serializers.DoubleSerializer;

@Getter
@Setter
public class ItemsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String slug;
    private String barcode;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    private Integer discountPercent;
    private Boolean isFiftyPercentSaleApplied;
    private Boolean isActive;

    private List<String> imagesList;

    private Long categoryId;
    private String categorySlug;
    private String categoryNameUz;
    private String categoryNameRu;
    private String categoryNameEng;
    private Integer categorySortOrderWithinCollection;

    private List<Map<String, Object>> collectionsItemsList;
    private List<Map<String, Object>> sizesItemsList;

    public ItemsDTO(Items item) {

        id = item.getId();

        slug = item.getSlug();
        barcode = item.getBarcode();

        createdTime = item.getCreatedTime();

        nameUz = item.getNameUz();
        nameRu = item.getNameRu();
        nameEng = item.getNameEng();

        descriptionUz = item.getDescriptionUz();
        descriptionRu = item.getDescriptionRu();
        descriptionEng = item.getDescriptionEng();

        price = item.getPrice();
        discountPercent = item.getDiscountPercent();

        isFiftyPercentSaleApplied = false;
        isActive = item.getIsActive();

        imagesList = new ArrayList<>();
        if (item.getItemsImagesList() != null && !item.getItemsImagesList().isEmpty()) {

            Set<Items_Images> itemsImagesSet = new HashSet<>(item.getItemsImagesList());

            imagesList = itemsImagesSet.stream()
                    .sorted(Comparator.comparing(Items_Images::getImageName))
                    .map(Items_Images::getImageName)
                    .collect(Collectors.toList());
        }

        categorySortOrderWithinCollection = null;
        if (item.getCategory() != null) {

            categoryId = item.getCategory().getId();
            categorySlug = item.getCategory().getSlug();
            categoryNameUz = item.getCategory().getNameUz();
            categoryNameRu = item.getCategory().getNameRu();
            categoryNameEng = item.getCategory().getNameEng();
            categorySortOrderWithinCollection = item.getCategory().getSortOrderWithinCollection();
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
                        map.put("collectionSlug", collection.getCollection().getSlug());
                        map.put("collectionNameUz", collection.getCollection().getNameUz());
                        map.put("collectionNameRu", collection.getCollection().getNameRu());
                        map.put("collectionNameEng", collection.getCollection().getNameEng());

                        List<Map<String, Object>> collectionCategoriesList = collection.getCollection().getCategoriesList().stream()
                                .map(category -> {

                                    Map<String, Object> categoryMap = new LinkedHashMap<>();

                                    categoryMap.put("id", category.getId());
                                    categoryMap.put("nameUz", category.getNameUz());
                                    categoryMap.put("nameRu", category.getNameRu());
                                    return categoryMap;
                                }).collect(Collectors.toList());

                        map.put("collectionCategoriesList", collectionCategoriesList);

                        if (Boolean.TRUE.equals(collection.getCollection().getIsFiftyPercentSaleApplied())) {
                            isFiftyPercentSaleApplied = true;
                        }

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

                        map.put("price", getFormattedPrice(size.getPrice()));
                        map.put("discountPercent", size.getDiscountPercent());

                        return map;
                    }).collect(Collectors.toList());
        }
    }
}