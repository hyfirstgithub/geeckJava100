package option_2;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class OrderExample {

    private List<Order> orders = new ArrayList<>();

    private void filterOrder() {
        orders.stream()
                .filter(Objects::nonNull)
                .filter(order -> order.getPlacedAt().isAfter(LocalDateTime.now().minusMonths(6)))
                .filter(order -> order.getTotalPrice() > 40)
                .forEach(System.out::println);
    }

    private void mapOrder() {
        LongAdder longAdder = new LongAdder();
        orders.stream().forEach(order ->
                order.getOrderItemList().forEach(orderItem -> longAdder.add(orderItem.getProductQuantity())));

        assert longAdder.longValue() == orders.stream().mapToLong(order ->
                order.getOrderItemList().stream()
                        .mapToLong(OrderItem::getProductQuantity).sum()).sum();
    }

    private void floatMapOrder() {
        Double price1 = orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .mapToDouble(item -> item.getProductQuantity() * item.getProductPrice()).sum();

        Double price2 = orders.stream()
                .flatMapToDouble(order -> order.getOrderItemList()
                        .stream().mapToDouble(item -> item.getProductPrice() * item.getProductQuantity()))
                .sum();
    }

    private void sortedOrder() {
        orders.stream().filter(order -> order.getTotalPrice() > 50)
                .sorted(Comparator.comparing(Order::getTotalPrice).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    private void distinctOrder() {
        System.out.println(orders.stream().map(order -> order.getCustomerName())
                .distinct().collect(Collectors.joining(",")));


        System.out.println(orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .map(OrderItem::getProductName).distinct().collect(Collectors.joining(",")));
    }

    private void skipAndLimitOrder() {
        orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName() + "@" + order.getPlacedAt())
                .limit(2).forEach(System.out::println);

        orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName() + "@" + order.getPlacedAt())
                .skip(2).limit(2).forEach(System.out::println);
    }
}
