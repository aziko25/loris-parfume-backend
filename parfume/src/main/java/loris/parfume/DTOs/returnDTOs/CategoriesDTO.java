package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import loris.parfume.Models.Items.Categories;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
public class CategoriesDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String slug;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private String bannerImage;

    private Boolean isRecommendedInMainPage;

    private Long collectionId;
    private String collectionNameUz;
    private String collectionNameRu;
    private String collectionNameEng;

    public CategoriesDTO(Categories category) {

        id = category.getId();
        slug = category.getSlug();
        createdTime = category.getCreatedTime();

        nameUz = category.getNameUz();
        nameRu = category.getNameRu();
        nameEng = category.getNameEng();

        bannerImage = category.getBannerImage();
        isRecommendedInMainPage = category.getIsRecommendedInMainPage();

        if (category.getCollection() != null) {

            collectionId = category.getCollection().getId();
            collectionNameUz = category.getCollection().getNameUz();
            collectionNameRu = category.getCollection().getNameRu();
            collectionNameEng = category.getCollection().getNameEng();
        }
    }
}