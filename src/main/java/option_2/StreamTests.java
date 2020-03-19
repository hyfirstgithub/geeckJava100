package option_2;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamTests {

    @Test
    public void stream() {
        Arrays.asList("a1", "a2", "a3").stream().forEach(System.out::println);
        Arrays.stream(new int[]{1, 2, 3}).forEach(System.out::println);
    }

    @Test
    public void of() {
        String[] arr = {"a", "b", "c"};
        Stream.of(arr).forEach(System.out::println);
        Stream.of("a", "b", "c").forEach(System.out::println);
        Stream.of(1, 2, "a").map(item -> item.getClass().getName()).forEach(System.out::println);

    }

    @Test
    public void iterate() {
        Stream.iterate(2, item -> item*2).limit(10).forEach(System.out::println);
        Stream.iterate(BigInteger.ZERO, n->n.add(BigInteger.TEN)).limit(10).forEach(System.out::println);
    }

    @Test
    public void generate() {
        Stream.generate(()->"test").limit(3).forEach(System.out::println);
        Stream.generate(Math::random).limit(10).forEach(System.out::println);
    }

    @Test
    public void primitive() {
        IntStream.range(1,3).forEach(System.out::println);
        IntStream.range(0,3).mapToObj(i -> "x").forEach(System.out::println);

        IntStream.rangeClosed(1,3).forEach(System.out::println);
        DoubleStream.of(1.1,2.2,3.3).forEach(System.out::println);

        System.out.println(IntStream.of(1,2).toArray().getClass());
        System.out.println(Stream.of(1,2).mapToInt(Integer::intValue).toArray().getClass());
        System.out.println(IntStream.of(1,2).boxed().toArray().getClass());
        System.out.println(IntStream.of(1,2).asDoubleStream().toArray().getClass());
        System.out.println(IntStream.of(1,2).asLongStream().toArray().getClass());

        Arrays.asList("a","b","c").stream()
                .mapToInt(String::length)
                .asLongStream()
                .mapToDouble(x->x/1.0)
                .boxed()
                .mapToLong(x -> 1L)
                .mapToObj(x -> "")
                .collect(Collectors.toList());
    }
}
