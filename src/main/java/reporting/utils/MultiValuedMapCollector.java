package reporting.utils;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class MultiValuedMapCollector<T, K, V> implements Collector<T, MultiValueMap<K, V>, MultiValueMap<K, V>> {
	private Function<T, K> keyMapper;
	private Function<T, V> valueMapper;

	private MultiValuedMapCollector(Function<T, K> keyMapper, Function<T, V> valueMapper) {
		this.keyMapper = keyMapper;
		this.valueMapper = valueMapper;
	}

	public static <T, K, V> MultiValuedMapCollector<T, K, V> toMultivaluedMap(Function<T, K> keyMapper,
	                                                                          Function<T, V> valueMapper) {
		return new MultiValuedMapCollector<>(keyMapper, valueMapper);
	}

	private static <K, V> MultiValueMap<K, V> combine(MultiValueMap<K, V> left, MultiValueMap<K, V> right) {
		MultiValueMap<K, V> combined = MultiValuedMapCollector.<K, V>mapSupplier().get();
		left.forEach((k, v) -> v.forEach(e -> combined.add(k, e)));
		right.forEach((k, v) -> v.forEach(e -> combined.add(k, e)));
		return combined;
	}

	private static <K, V> Supplier<MultiValueMap<K, V>> mapSupplier() {
		return LinkedMultiValueMap<K, V>::new;
	}

	public Supplier<MultiValueMap<K, V>> supplier() {
		return mapSupplier();
	}

	public BiConsumer<MultiValueMap<K, V>, T> accumulator() {
		return (acc, elem) -> acc.add(keyMapper.apply(elem), valueMapper.apply(elem));
	}

	public BinaryOperator<MultiValueMap<K, V>> combiner() {
		return (left, right) -> combine(left, right);
	}

	public Function<MultiValueMap<K, V>, MultiValueMap<K, V>> finisher() {
		return m -> m;
	}

	public Set<Characteristics> characteristics() {
		return new HashSet<>();
	}


}
