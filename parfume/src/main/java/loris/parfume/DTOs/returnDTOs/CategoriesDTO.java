package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import loris.parfume.Models.Items.Categories;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
public class CategoriesDTO {

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private Long collectionId;
    private String collectionNameUz;
    private String collectionNameRu;
    private String collectionNameEng;

    public CategoriesDTO(Categories category) {

        id = category.getId();
        createdTime = category.getCreatedTime();

        nameUz = category.getNameUz();
        nameRu = category.getNameRu();
        nameEng = category.getNameEng();

        collectionId = category.getCollection().getId();
        collectionNameUz = category.getCollection().getNameUz();
        collectionNameRu = category.getCollection().getNameRu();
        collectionNameEng = category.getCollection().getNameEng();
    }
}