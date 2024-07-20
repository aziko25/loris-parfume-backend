package loris.parfume.DTOs.Filters;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrderFilters {

    private LocalDate startDate;
    private LocalDate endDate;
}