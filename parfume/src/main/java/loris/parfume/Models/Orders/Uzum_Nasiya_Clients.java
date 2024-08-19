package loris.parfume.Models.Orders;

import jakarta.persistence.*;
import lombok.*;
import loris.parfume.Models.Users;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "uzum_nasiya_clients")
public class Uzum_Nasiya_Clients {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    private Double sum;

    private Boolean isApproved;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders order;
}