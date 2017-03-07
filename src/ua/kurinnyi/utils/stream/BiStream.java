package ua.kurinnyi.utils.stream;

import ua.kurinnyi.utils.tuple.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiStream<L,R>{


	private Stream<Pair<L,R>> stream;


	protected BiStream(Stream<Pair<L,R>> stream) {
		this.stream = stream;
	}

	public static <L,R> BiStream<L,R> flatMap(Stream<? extends L> stream,
	                                          Function<? super L, ? extends Stream<? extends R>> mapper) {
		return new BiStream<>(stream.flatMap(initialValue ->
				mapper.apply(initialValue).map(mappedValue -> Pair.of(initialValue, mappedValue))
		));
	}

	public static <L,R> BiStream<L,R> combineLists(List<? extends L> list1, List<? extends R> list2) {
		if (list1.size() != list2.size())
			throw new IllegalArgumentException("Lists should have same size.");
		Iterator<? extends R> list2Iterator = list2.iterator();
		return new BiStream<>(list1.stream().map(value1 -> Pair.of(value1, list2Iterator.next())));
	}

	public static <L,R> BiStream<L,R> map(Stream<? extends L> stream, Function<? super L, ? extends R> mapper) {
		return new BiStream<>(stream.map(initialValue -> Pair.of(initialValue, mapper.apply(initialValue))));
	}

	public static <L, R>BiStream<L, R> fromMap(Map<? extends L, ? extends R> map) {
		return new BiStream<>(map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
	}

	public static <L, R> BiStream<L, R> fromStream(Stream<Pair<L, R>> stream) {
		return new BiStream<>(stream);
	}

	public static <L, R> BiStream<L,R> of(Pair<L, R>... pairs ) {
		return new BiStream<>(Stream.of(pairs));
	}


	public BiStream<L,R> filter(BiPredicate<? super L, ? super R> predicate){
		return new BiStream<>(stream.filter(pair -> pair.test(predicate)));
	}

	public void forEach(BiConsumer<? super L, ? super R> action){
		stream.forEach(pair -> pair.use(action));
	}

	public BiStream<L, R> peek(BiConsumer<? super L, ? super R> action){
		return new BiStream<>(stream.peek(pair -> action.accept(pair.getLeft(), pair.getRight())));
	}

	public Stream<Pair<L,R>> toStream(){
		return stream;
	}

	public boolean allMatch(BiPredicate<? super L, ? super R> predicate) {
		return stream.allMatch(pair -> pair.test(predicate));
	}

	public boolean anyMatch(BiPredicate<? super L, ? super R> predicate) {
		return stream.anyMatch(pair -> pair.test(predicate));
	}

	public <M> BiStream<M, R> mapLeft(BiFunction<? super L, ? super R , ? extends M> mapper) {
		return new BiStream<>(stream.map(pair -> pair.mapLeft(mapper)));
	}

	public <M> BiStream<L, M> mapRight(BiFunction<? super L, ? super R, ? extends M> mapper) {
		return new BiStream<>(stream.map(pair -> pair.mapRight(mapper)));
	}

	public <M> BiStream<L, M> flatMapRight(BiFunction<? super L, ? super R, ? extends Stream<? extends M>> mapper) {

		Stream<Pair<L, M>> resultStream = stream
				.flatMap(pair ->  mapper.apply(pair.getLeft(), pair.getRight())
				.map(mappedRight -> Pair.of(pair.getLeft(), mappedRight)));

		return new BiStream<>(resultStream);
	}

	public <M> BiStream<M, R> flatMapLeft(BiFunction<? super L, ? super R, ? extends Stream<? extends M>> mapper) {

		Stream<Pair<M, R>> resultStream = stream
				.flatMap(pair -> mapper.apply(pair.getLeft(), pair.getRight())
				.map(mappedLeft-> Pair.of(mappedLeft, pair.getRight())));

		return new BiStream<>(resultStream);
	}

	public BiStream<R, L> swap() {
		return new BiStream<>(stream.map(Pair::swap));
	}

	public Map<L, R> toMap() {
		return toMap ((value, newValue) ->  newValue);
	}

	public Map<L, R> toMap(BinaryOperator<R> mergeFunction) {
		return stream.collect(Collectors.toMap(Pair::getLeft, Pair::getRight, mergeFunction));
	}
}
