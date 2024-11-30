package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Models.Orders.Orders_Items;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static loris.parfume.Configurations.Serializers.DoubleSerializer.getFormattedPrice;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders_Items_DTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String imageName;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEng;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double totalPrice;

    private Integer quantity;

    private Long collectionId;
    private String collectionSlug;
    private String collectionNameUz;
    private String collectionNameRu;
    private String collectionNameEng;

    private Long categoryId;
    private String categoryNameUz;
    private String categoryNameRu;
    private String categoryNameEng;

    private Long sizeId;
    private String sizeNameUz;
    private String sizeNameRu;
    private String sizeNameEng;

    private List<Map<String, Object>> collectionsItemsList;
    private List<Map<String, Object>> sizesItemsList;

    public Orders_Items_DTO(Orders_Items ordersItem) {

        id = ordersItem.getItem().getId();

        if (!ordersItem.getItem().getItemsImagesList().isEmpty()) {

            imageName = ordersItem.getItem().getItemsImagesList().get(0).getImageName();
        }

        nameUz = ordersItem.getItem().getNameUz();
        nameRu = ordersItem.getItem().getNameRu();
        nameEng = ordersItem.getItem().getNameEng();

        descriptionUz = ordersItem.getItem().getDescriptionUz();
        descriptionRu = ordersItem.getItem().getDescriptionRu();
        descriptionEng = ordersItem.getItem().getDescriptionEng();

        totalPrice = ordersItem.getTotalPrice();
        quantity = ordersItem.getQuantity();

        if (ordersItem.getCollection() != null) {

            collectionId = ordersItem.getCollection().getId();
            collectionSlug = ordersItem.getCollection().getSlug();
            collectionNameUz = ordersItem.getCollection().getNameUz();
            collectionNameRu = ordersItem.getCollection().getNameRu();
            collectionNameEng = ordersItem.getCollection().getNameEng();
        }

        if (ordersItem.getItem().getCategory() != null) {

            categoryId = ordersItem.getItem().getCategory().getId();
            categoryNameUz = ordersItem.getItem().getCategory().getNameUz();
            categoryNameRu = ordersItem.getItem().getCategory().getNameRu();
            categoryNameEng = ordersItem.getItem().getCategory().getNameEng();
        }

        sizeId = ordersItem.getSize().getId();
        sizeNameUz = ordersItem.getSize().getNameUz();
        sizeNameRu = ordersItem.getSize().getNameRu();
        sizeNameEng = ordersItem.getSize().getNameEng();

        collectionsItemsList = new ArrayList<>();
        if (ordersItem.getItem().getCollectionsItemsList() != null && !ordersItem.getItem().getCollectionsItemsList().isEmpty()) {

            Set<Collections_Items> collectionsItems = new HashSet<>(ordersItem.getItem().getCollectionsItemsList());
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

                        return map;
                    }).collect(Collectors.toList());
        }

        sizesItemsList = new ArrayList<>();
        if (ordersItem.getItem().getSizesItemsList() != null && !ordersItem.getItem().getSizesItemsList().isEmpty()) {

            Set<Sizes_Items> sizesItems = new HashSet<>(ordersItem.getItem().getSizesItemsList());
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