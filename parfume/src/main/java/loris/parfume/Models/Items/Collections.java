package loris.parfume.Models.Items;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
/*@Table(name = "collections",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name_uz", "name_ru", "name_eng"})
})*/
@Table(name = "collections")
public class Collections {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String nameUz;
    private String nameRu;
    private String nameEng;
}