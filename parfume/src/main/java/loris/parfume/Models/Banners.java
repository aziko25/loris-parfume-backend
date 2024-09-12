package loris.parfume.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "banners")
public class Banners implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String titleUz;
    private String titleRu;
    private String titleEng;

    private String redirectTo;
    private Boolean isActive;

    private String desktopImageNameUz;
    private String desktopImageNameRu;
    private String desktopImageNameEng;

    private String mobileImageNameUz;
    private String mobileImageNameRu;
    private String mobileImageNameEng;
}