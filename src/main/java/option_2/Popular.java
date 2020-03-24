package option_2;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Popular<T> {

    private Map<T, Integer> insizeData = new ConcurrentHashMap<>();

    public void append(T data) {
        if( insizeData.containsKey(data)) {
            insizeData.computeIfPresent(data, (k, v) -> ++v);
        }else {
            insizeData.putIfAbsent(data, 1);
        }
    }

    public T getPopular() {
        Map.Entry<T, Integer> result = insizeData.entrySet()
                .stream()
                .sorted(Map.Entry.<T,Integer>comparingByValue().reversed()).limit(1)
                .collect(Collectors.toList()).get(0);
        return result.getKey();
    }

    public Map<T, Integer> inerMap() {
        Map<T, Integer> result = new ConcurrentHashMap<>(insizeData.size());
        insizeData.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    public void clear() {
        insizeData.clear();
    }

    public void combine(Popular<T> other) {
        Map<T, Integer> otherData = other.inerMap();
        otherData.entrySet().forEach(entry -> {
            if( insizeData.containsKey(entry.getKey())) {
                insizeData.computeIfPresent(entry.getKey(), (k, v) -> entry.getValue() + v);
            }else {
                insizeData.computeIfAbsent(entry.getKey(), k -> entry.getValue());
            }
        });
        other.clear();
    }
}
