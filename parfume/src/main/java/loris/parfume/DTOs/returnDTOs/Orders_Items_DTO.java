package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Configurations.Serializers.DoubleSerializer;
import loris.parfume.Models.Orders.Orders_Items;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders_Items_DTO {

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

    public Orders_Items_DTO(Orders_Items ordersItem) {

        id = ordersItem.getItem().getId();
        imageName = ordersItem.getItem().getImageName();

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
            collectionNameUz = ordersItem.getCollection().getNameUz();
            collectionNameRu = ordersItem.getCollection().getNameRu();
            collectionNameEng = ordersItem.getCollection().getNameEng();
        }

        sizeId = ordersItem.getSize().getId();
        sizeNameUz = ordersItem.getSize().getNameUz();
        sizeNameRu = ordersItem.getSize().getNameRu();
        sizeNameEng = ordersItem.getSize().getNameEng();
    }
}