package loris.parfume.DTOs.Filters;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemFilters {

    private String search;

    private Boolean firstA;
    private Boolean firstZ;

    private Boolean firstExpensive;
    private Boolean firstCheap;
}