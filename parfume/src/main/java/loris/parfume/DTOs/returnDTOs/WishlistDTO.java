package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items_Images;
import loris.parfume.Models.Items.Sizes_Items;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDTO {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
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

    private Long collectionId;
    private String collectionNameUz;
    private String collectionNameRu;
    private String collectionNameEng;

    @JsonSerialize(using = DoubleSerializer.class)
    private Double price;

    private Integer discountPercent;

    public WishlistDTO(Sizes_Items sizeItem, Collections collections) {

        id = sizeItem.getItem().getId();
        createdTime = sizeItem.getItem().getCreatedTime();
        nameUz = sizeItem.getItem().getNameUz();
        nameRu = sizeItem.getItem().getNameRu();
        nameEng = sizeItem.getItem().getNameEng();
        descriptionUz = sizeItem.getItem().getDescriptionUz();
        descriptionRu = sizeItem.getItem().getDescriptionRu();
        descriptionEng = sizeItem.getItem().getDescriptionEng();
        price = sizeItem.getPrice();
        discountPercent = sizeItem.getDiscountPercent();

        if (!sizeItem.getSize().getIsDefaultNoSize()) {

            sizeId = sizeItem.getSize().getId();
            sizeNameUz = sizeItem.getSize().getNameUz();
            sizeNameRu = sizeItem.getSize().getNameRu();
            sizeNameEng = sizeItem.getSize().getNameEng();
        }

        if (collections != null) {

            collectionId = collections.getId();
            collectionNameUz = collections.getNameUz();
            collectionNameRu = collections.getNameRu();
            collectionNameEng = collections.getNameEng();
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
    }
}
