package loris.parfume.Models.Orders;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Models.Branches;
import loris.parfume.Models.Users;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdTime;

    private String address;
    private String addressLocationLink;
    private Double distance;
    private String phone;

    private Double sumForDelivery;
    private Double totalSum;

    private String comments;
    private Boolean isDelivered;
    private Boolean isDelivery;

    private Boolean isSoonDeliveryTime;

    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime scheduledDeliveryTime;

    private String paymentLink;
    private String paymentType;
    private Boolean isPaid;

    private String paymentResponseUz;
    private String paymentResponseRu;
    private String paymentResponseEng;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branches branch;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(mappedBy = "order")
    private List<Orders_Items> itemsList;
}