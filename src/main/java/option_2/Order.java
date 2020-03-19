package option_2;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {

    private Long id;
    private Long customerId;
    private String customerName;
    private List<OrderItem> orderItemList;
    private Double totalPrice;
    private LocalDateTime placedAt;

}
