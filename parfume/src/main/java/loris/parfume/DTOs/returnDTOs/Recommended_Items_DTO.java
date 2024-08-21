package loris.parfume.DTOs.returnDTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Models.Items.Items;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class Recommended_Items_DTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ItemsDTO> recommendedItems;

    public Recommended_Items_DTO(List<Items> recommendedItems) {

        this.recommendedItems = recommendedItems.stream()
                .map(ItemsDTO::new)
                .collect(Collectors.toList());
    }
}