package loris.parfume.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "blog")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    @Column(length = 512)
    private String titleUz;
    @Column(length = 512)
    private String titleRu;

    @Column(length = 64000)
    private String descriptionUz;
    @Column(length = 64000)
    private String descriptionRu;

    private String mainImage;

    private Boolean isActive;
}