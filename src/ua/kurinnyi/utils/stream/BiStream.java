package ua.kurinnyi.utils.stream;

import java.util.function.*;
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

	//TODO initial map
	//TODO initial from map
	//TODO initial as combine of collections
	//TODO toMap
	//TODO toMap with collision handling
	//TODO reduce
	//TODO advance tuple
	//TODO swap

	public static class Pair <T, R> {
		private T first;
		private R second;

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
}
