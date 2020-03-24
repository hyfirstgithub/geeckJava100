package option_2;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toList;

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

    @Test
    public void collectorSample() {
        Random random = new Random();
        System.out.println(random.ints(48, 122)
                .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                .mapToObj(i -> (char) i)
                .limit(20)
                .collect(StringBuffer::new, StringBuffer::append, StringBuffer::append)
                .toString());

        System.out.println(orders.stream().map(order -> order.getCustomerName()).collect(Collectors.toSet())
                .stream().collect(Collectors.joining(",", "[", "]")));

        System.out.println(orders.stream().limit(2).collect(Collectors.toCollection(LinkedList::new)).getClass());

        orders.stream().collect(Collectors
                .toMap(Order::getId, Order::getCustomerName))
                .entrySet().forEach(System.out::println);

        orders.stream()
                .collect(Collectors.toMap(Order::getCustomerName, Order::getPlacedAt, (x, y) -> x.isAfter(y) ? x : y))
                .entrySet().forEach(System.out::println);

        System.out.println(orders.stream().collect(Collectors.averagingInt(order ->
                order.getOrderItemList().stream()
                        .collect(Collectors.summingInt(OrderItem::getProductQuantity)))));

    }

    public void collectorGroupBy() {
        System.out.println(orders.stream().collect(Collectors.groupingBy(Order::getCustomerName, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).collect(toList()));

        //按照用户名分组，统计订单总金额
        System.out.println(orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomerName, summingDouble(Order::getTotalPrice)))
                .entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(toList()));

        //按照用户名分组，统计商品采购数量
        System.out.println(orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomerName, Collectors.summingInt(order ->
                        order.getOrderItemList()
                                .stream().collect(Collectors.summingInt(OrderItem::getProductQuantity)))))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(toList()));

        //统计最受欢迎的商品，倒序后取第一个
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey).findFirst().ifPresent(System.out::println);

        //统计最受欢迎的商品的另一种方式，直接利用maxBy
        orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream()
                .collect(Collectors.maxBy(Map.Entry.<String, Integer>comparingByValue()))
                .map(Map.Entry::getKey)
                .ifPresent(System.out::println);

        //按照用户名分组，选用户下的总金额最大的订单
        orders.stream().collect(Collectors.groupingBy(Order::getCustomerName,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.
                        comparingDouble(Order::getTotalPrice)), Optional::get)))
                .forEach((k, v) -> System.out.println(k + "#" + v.getTotalPrice()));

        //根据下单年月分组，统计订单ID列表
        orders.stream().collect(Collectors
                .groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyyMM")),
                        Collectors.mapping(order -> order.getId(), Collectors.toList())));

        //根据下单年月+用户名两次分组，统计订单ID列表
        Map<String, Map<String, List<Long>>> maps = orders.stream().collect(Collectors
                .groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("yyyyMM")),
                        Collectors.groupingBy(Order::getCustomerName,
                                Collectors.mapping(Order::getId, Collectors.toList()))));

        //根据是否有下单记录进行分区
        Customer.getData().stream()
                .collect(Collectors
                        .partitioningBy(customer -> orders.stream()
                                .anyMatch(order -> customer.getId() == order.getCustomerId())));


    }

    @Test
    public void observation() {
        List<String> datas = Arrays.asList("a", "b", "c", "d");
        datas.stream()
                .filter("a"::equals)
                .collect(Collectors.toList())
                .stream()
                .map(a -> 12);
    }

}
