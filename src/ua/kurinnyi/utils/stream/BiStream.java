package ua.kurinnyi.utils.stream;

import ua.kurinnyi.utils.tuple.Pair;

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
				mapper.apply(initialValue).map(mappedValue -> Pair.of(initialValue, mappedValue))
		));
	}

	public static <T,R> BiStream<T,R> combineLists(List<? extends T> list1, List<? extends R> list2) {
		if (list1.size() != list2.size())
			throw new IllegalArgumentException("Lists should have same size.");
		Iterator<? extends R> list2Iterator = list2.iterator();
		return new BiStream<>(list1.stream().map(value1 -> Pair.of(value1, list2Iterator.next())));
	}

	public static <T,R> BiStream<T,R> map(Stream<? extends T> stream,
	                                          Function<? super T, ? extends R> mapper) {
		return new BiStream<>(stream.map(initialValue -> Pair.of(initialValue, mapper.apply(initialValue))));
	}

	public static <T, R>BiStream<T, R> fromMap(Map<? extends T, ? extends R> map) {
		return new BiStream<>(map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
	}

	public static <T, R> BiStream<T, R> fromStream(Stream<Pair<T, R>> stream) {
		return new BiStream<>(stream);
	}

	public static <T, R> BiStream<T,R> of(Pair<T, R>... pairs ) {
		return new BiStream<>(Stream.of(pairs));
	}


	public BiStream<T,R> filter(BiPredicate<? super T, ? super R> predicate){
		return new BiStream<>(stream.filter(convertToPairPredicate(predicate)));
	}

	public void forEach(BiConsumer<? super T, ? super R> action){
		stream.forEach(pair -> action.accept(pair.getLeft(), pair.getRight()));
	}

	public BiStream<T, R> peek(BiConsumer<? super T, ? super R> action){
		return new BiStream<>(stream.peek(pair -> action.accept(pair.getLeft(), pair.getRight())));
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

	public <M> BiStream<M, R> mapLeft(BiFunction<? super T, ? super R , ? extends M> mapper) {
		return new BiStream<>(stream.map(pair -> Pair.of(mapper.apply(pair.getLeft(), pair.getRight()), pair.getRight())));
	}

	public <M> BiStream<T, M> mapRight(BiFunction<? super T, ? super R, ? extends M> mapper) {
		return new BiStream<>(stream.map(pair -> Pair.of(pair.getLeft(), mapper.apply(pair.getLeft(), pair.getRight()))));
	}

	public <M> BiStream<T, M> flatMapRight(BiFunction<? super T, ? super R, ? extends Stream<? extends M>> mapper) {

		Stream<Pair<T, M>> resultStream = stream
				.flatMap(pair ->  mapper.apply(pair.getLeft(), pair.getRight())
				.map(mappedRight -> Pair.of(pair.getLeft(), mappedRight)));

		return new BiStream<>(resultStream);
	}

	public <M> BiStream<M, R> flatMapLeft(BiFunction<? super T, ? super R, ? extends Stream<? extends M>> mapper) {

		Stream<Pair<M, R>> resultStream = stream
				.flatMap(pair ->  mapper.apply(pair.getLeft(), pair.getRight())
				.map(mappedLeft->Pair.of(mappedLeft, pair.getRight())));

		return new BiStream<>(resultStream);
	}

	private Predicate<Pair<T, R>> convertToPairPredicate(BiPredicate<? super T, ? super R> predicate) {
		return pair -> predicate.test(pair.getLeft(), pair.getRight());
	}

	public BiStream<R, T> swap() {
		return new BiStream<>(stream.map(pair -> Pair.of(pair.getRight(), pair.getLeft())));
	}

	public Map<T, R> toMap() {
		return toMap ((r, r2) ->  r2);
	}

	public Map<T, R> toMap(BinaryOperator<R> mergeFunction) {
		return stream.collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight(), mergeFunction));
	}
}
