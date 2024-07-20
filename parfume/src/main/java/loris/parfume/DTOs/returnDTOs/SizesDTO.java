package loris.parfume.DTOs.returnDTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Items.Sizes_Items;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SizesDTO {

    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;

    private Boolean isDefaultNoSize;

    private List<ItemsDTO> itemsList;

    public SizesDTO(Sizes size) {

        id = size.getId();
        createdTime = size.getCreatedTime();
        nameUz = size.getNameUz();
        nameRu = size.getNameRu();
        nameEng = size.getNameEng();
        isDefaultNoSize = size.getIsDefaultNoSize();

        itemsList = new ArrayList<>();
        if (size.getItemsList() != null && !size.getItemsList().isEmpty()) {

            for (Sizes_Items sizesItem : size.getItemsList()) {

                itemsList.add(new ItemsDTO(sizesItem.getItem()));
            }
        }
    }
}