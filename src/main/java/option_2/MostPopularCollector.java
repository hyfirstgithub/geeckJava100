package option_2;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

class MostPopularCollecto<T> implements Collector<T, Popular<T>, T> {


    @Override
    public Supplier<Popular<T>> supplier() {
        return Popular::new;
    }

    @Override
    public BiConsumer<Popular<T>, T> accumulator() {
        return (t, a) -> t.append(a);
    }

    @Override
    public BinaryOperator<Popular<T>> combiner() {
        return (left, right) -> {
            left.combine(right);
            return left;
        };
    }

    @Override
    public Function<Popular<T>, T> finisher() {
        return tPopular -> tPopular.getPopular();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
    }

    public static void main(String[] args) {
        Integer result1 = Stream.of(1, 1, 2, 2, 2, 3, 4, 5, 5).collect(new MostPopularCollecto<>());
        if ((result1.equals(2))) {
            System.out.println("num success");
        }
        if (Stream.of('a', 'b', 'c', 'c', 'c', 'd')

                .collect(new MostPopularCollecto<>()).equals('c')) {
            System.out.println("char success");
        }
    }
}
