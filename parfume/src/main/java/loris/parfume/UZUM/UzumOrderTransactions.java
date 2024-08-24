package loris.parfume.UZUM;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Models.Orders.Orders;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "uzum_order_transaction")
public class UzumOrderTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String transId;

    private String status;
    private String errorCode;

    private Long transTime;
    private Long confirmTime;
    private Long reverseTime;
    private Integer amount;

    private String paymentSource;

    @OneToOne
    private Orders order;
}