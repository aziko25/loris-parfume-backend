package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
public class CollectionsDTO implements Serializable {

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

    private Integer sortOrder;
    private Boolean isFiftyPercentSaleApplied;

    private Boolean isRecommendedInMainPage;

    private List<Map<String, Object>> categoriesList;

    public CollectionsDTO(Collections collection) {

        id = collection.getId();
        slug = collection.getSlug();
        createdTime = collection.getCreatedTime();
        nameUz = collection.getNameUz();
        nameRu = collection.getNameRu();
        nameEng = collection.getNameEng();
        bannerImage = collection.getBannerImage();
        sortOrder = collection.getSortOrder();
        isFiftyPercentSaleApplied = collection.getIsFiftyPercentSaleApplied();
        isRecommendedInMainPage = collection.getIsRecommendedInMainPage();

        categoriesList = new ArrayList<>();
        if (collection.getCategoriesList() != null && !collection.getCategoriesList().isEmpty()) {

            List<Categories> sortedCategories = collection.getCategoriesList().stream()
                    .sorted(Comparator.comparing(Categories::getNameUz))
                    .toList();

            for (Categories category : sortedCategories) {

                Map<String, Object> categoryMap = new LinkedHashMap<>();

                categoryMap.put("categoryId", category.getId());
                categoryMap.put("categoryNameUz", category.getNameUz());
                categoryMap.put("categoryNameRu", category.getNameRu());
                categoryMap.put("categoryNameEng", category.getNameEng());
                categoryMap.put("categoryBannerImage", category.getBannerImage());

                categoriesList.add(categoryMap);
            }
        }
    }
}