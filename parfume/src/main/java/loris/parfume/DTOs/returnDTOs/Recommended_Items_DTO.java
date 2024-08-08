package loris.parfume.DTOs.returnDTOs;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class Recommended_Items_DTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ItemsDTO item;
    private List<ItemsDTO> recommendedItems;

    public Recommended_Items_DTO(Items item, List<Items> recommendedItems) {

        this.item = new ItemsDTO(item);
        this.recommendedItems = recommendedItems.stream()
                .map(ItemsDTO::new)
                .collect(Collectors.toList());
    }
}