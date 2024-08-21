package loris.parfume.PAYME.Result;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.Models.Orders.Orders;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payme_order_transaction")
public class OrderTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paycomId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paycomTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Long createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "perform_time")
    private Long performTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "cancel_time")
    private Long cancelTime;

    @Enumerated(EnumType.STRING)
    private OrderCancelReason reason;

    @Enumerated(EnumType.STRING)
    private TransactionState state;

    @OneToOne
    private Orders order;

    public void setCreateTime(Date createTime) {
        this.createTime = createTime.getTime();
    }

    public void setPerformTimes(Date createTime) {
        this.performTime = createTime.getTime();
    }

    public void setCancelTimes(Date createTime) {
        this.cancelTime = createTime.getTime();
    }
}