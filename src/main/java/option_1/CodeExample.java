package option_1;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodeExample {

    public static void fileExample() throws IOException {
        try (Stream<Path> pathStream = Files.walk(Paths.get("."))) {
            pathStream.filter(Files::isRegularFile)
                    .filter(FileSystems.getDefault()
                            .getPathMatcher("glob:**/*.java")::matches)
                    .flatMap(ThrowingFunction.unchecked(path ->
                            Files.readAllLines(path).stream().
                                    filter(line -> Pattern.compile("public class").matcher(line).find())
                                    .map(line -> path.getFileName() + ">> " + line)))
                    .forEach(System.out::println);
        }
    }

    public static void functionExample() {
        Predicate<Integer> positiveNumber = i -> i > 0;
        Predicate<Integer> evenNumber = i -> i % 2 == 0;
        assert (positiveNumber.and(evenNumber).test(2));
        //Consumer接口是消费一个数据。我们通过andThen方法组合调用两个Consumer，输出两行abcdefg
        Consumer<String> println = System.out::println;
        println.andThen(println).accept("abcdefg");
        //Function接口是输入一个数据，计算后输出一个数据。我们先把字符串转换为大写，然后通过andThen组合另一个Function实现字符串拼接
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> duplicate = s -> s.concat(s);
        assert (upperCase.andThen(duplicate).apply("test").equals("TESTTEST"));
        //Supplier是提供一个数据的接口。这里我们实现获取一个随机数
        Supplier<Integer> random = () -> ThreadLocalRandom.current().nextInt();
        System.out.println(random.get());

        //BinaryOperator是输入两个同类型参数，输出一个同类型参数的接口。这里我们通过方法引用获得一个整数加法操作，通过Lambda表达式定义一个减法操作，然后依次调用
        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<Integer> subtraction = (a, b) -> a - b;
        assert (subtraction.apply(add.apply(1, 2), 3).equals(0));
    }

    private static void increment(AtomicInteger integer) {
        integer.incrementAndGet();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int thread(int taskCount, int threadCount) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        IntStream.rangeClosed(1, threadCount).mapToObj(i -> new Thread(() -> {
            IntStream.rangeClosed(1, taskCount / threadCount).forEach(j -> increment(atomicInteger));
            countDownLatch.countDown();
        })).forEach(Thread::start);

        countDownLatch.await();
        return atomicInteger.get();
    }

    private static int threadpool(int taskCount, int threadCount) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        IntStream.rangeClosed(1, taskCount).forEach(i -> executorService.submit(() -> increment(atomicInteger)));
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);

        return atomicInteger.get();
    }

    public static int forkjoin(int taskCount, int threadCount) throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        forkJoinPool.execute(() -> {
            IntStream
                    .rangeClosed(1, taskCount)
                    .parallel()
                    .forEach(i -> {
                        increment(atomicInteger);
                        System.out.println(Thread.currentThread().getName());
                    });
            System.out.println(atomicInteger.get());
        });
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        return atomicInteger.get();
    }

    private int stream(int taskCount, int threadCount) {
        //设置公共ForkJoinPool的并行度
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(threadCount));
        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        //由于我们设置了公共ForkJoinPool的并行度，直接使用parallel提交任务即可
        IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger));
        //查询计数器当前值
        return atomicInteger.get();
    }

    private static int completableFuture(int taskCount, int threadCount) throws InterruptedException, ExecutionException {
        //总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        // 自定义一个并行度=threadCount的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        // 使用CompletableFuture.runAsync通过指定线程池异步执行任务
        CompletableFuture.runAsync(() ->
                IntStream.rangeClosed(1, taskCount).parallel()
                        .forEach(i -> increment(atomicInteger)), forkJoinPool)
                .get();
        // 查询计数器当前值
        return atomicInteger.get();
    }

    public static void main(String[] args) throws InterruptedException {
    }

}
