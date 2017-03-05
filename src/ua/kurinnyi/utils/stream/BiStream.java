package ua.kurinnyi.utils.stream;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiStream<T,R>{


	private Stream<Pair<T,R>> stream;


	private BiStream(Stream<Pair<T,R>> stream) {
		this.stream = stream;
	}

	public static <T,R> BiStream<T,R> flatMap(Stream<? extends T> stream,
	                                          Function<? super T, ? extends Stream<? extends R>> mapper) {
		return new BiStream<>(stream.flatMap(initialValue ->
				mapper.apply(initialValue).map(mappedValue -> new Pair<>(initialValue, mappedValue))
		));
	}

	public static <T,R> BiStream<T,R> combineLists(List<? extends T> list1, List<? extends R> list2) {
		if (list1.size() != list2.size())
			throw new IllegalArgumentException("Lists should have same size.");
		Iterator<? extends R> list2Iterator = list2.iterator();
		return new BiStream<>(list1.stream().map(value1 -> new Pair<>(value1, list2Iterator.next())));
	}

	public static <T,R> BiStream<T,R> map(Stream<? extends T> stream,
	                                          Function<? super T, ? extends R> mapper) {
		return new BiStream<>(stream.map(initialValue -> new Pair<>(initialValue, mapper.apply(initialValue))));
	}

	public static <T, R>BiStream<T, R> fromMap(Map<? extends T, ? extends R> map) {
		return new BiStream<>(map.entrySet().stream().map(entry -> new Pair(entry.getKey(), entry.getValue())));
	}

	public static <T, R> BiStream<T, R> fromStream(Stream<Pair<T, R>> stream) {
		return new BiStream<>(stream);
	}

	public BiStream<T,R> filter(BiPredicate<? super T, ? super R> predicate){
		return new BiStream<>(stream.filter(convertToPairPredicate(predicate)));
	}

	public void forEach(BiConsumer<? super T, ? super R> action){
		stream.forEach(pair -> action.accept(pair.first, pair.second));
	}

	public BiStream<T, R> peek(BiConsumer<? super T, ? super R> action){
		return new BiStream<>(stream.peek(pair -> action.accept(pair.first, pair.second)));
	}

	public Stream<Pair<T,R>> toStream(){
		return stream;
	}

	public boolean allMatch(BiPredicate<? super T, ? super R> predicate) {
		return stream.allMatch(convertToPairPredicate(predicate));
	}

	public boolean anyMatch(BiPredicate<? super T, ? super R> predicate) {
		return stream.anyMatch(convertToPairPredicate(predicate));
	}

	public <M> BiStream<M, R> mapLeft(Function<? super T, ? extends M> mapper) {
		return new BiStream<>(stream.map(pair -> new Pair(mapper.apply(pair.first), pair.second)));
	}

	public <M> BiStream<T, M> mapRight(Function<? super R, ? extends M> mapper) {
		return new BiStream<>(stream.map(pair -> new Pair(pair.first, mapper.apply(pair.second))));
	}

	private Predicate<Pair<T, R>> convertToPairPredicate(BiPredicate<? super T, ? super R> predicate) {
		return pair -> predicate.test(pair.first, pair.second);
	}

	public BiStream<R, T> swap() {
		return new BiStream<>(stream.map(pair -> new Pair(pair.second, pair.first)));
	}

	public Map<T, R> toMap() {
		return toMap ((r, r2) ->  r2);
	}

	public Map<T, R> toMap(BinaryOperator<R> mergeFunction) {
		return stream.collect(Collectors.toMap(pair -> pair.first, pair -> pair.second, mergeFunction));
	}


	//TODO advance tuple

	public static class Pair <T, R> {
		private T first;
		private R second;

		public static <T, R> Pair<T, R> of(T leftValue, R rightValue) {
			return new Pair<>(leftValue, rightValue);
		}

		Pair(T first, R second) {
			this.first = first;
			this.second = second;
		}

		public T getFirst() {
			return first;
		}

		public R getSecond() {
			return second;
		}
	}

	@FunctionalInterface
	public interface TriFunction <M, T, R>{

		 T apply(M m, T t, R r);
	}
}
