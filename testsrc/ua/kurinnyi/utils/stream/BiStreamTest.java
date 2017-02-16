package ua.kurinnyi.utils.stream;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class BiStreamTest {

	BiStream<String, Integer> initialBiStream;

	@Before
	public void setUp(){
		Stream<String> stream = Stream.of("a", "b");
		initialBiStream = BiStream.flatMap(stream, s -> Stream.of(1, 2));
	}

	@Test
	public void shouldCreateBiStreamFromStream(){
		BiStream biStream = initialBiStream;

		assertThat(biStream.toStream().collect(toList()))
				.isEqualToComparingFieldByFieldRecursively(
						asList(new BiStream.Pair<>("a", 1),
								new BiStream.Pair<>("a", 2),
								new BiStream.Pair<>("b", 1),
								new BiStream.Pair<>("b", 2)));
	}

	@Test
	public void shouldFilterValuesByPredicate(){

		BiStream biStream = initialBiStream.filter((s, i) -> "a".equals(s) && 1 == i);

		assertThat(biStream.toStream().collect(toList()))
				.isEqualToComparingFieldByFieldRecursively(singletonList(new BiStream.Pair<>("a", 1)));
	}

	@Test
	public void shouldInvokeActionOnEachPair(){
		List<Map.Entry<String, Integer>> entries = new ArrayList<>();

		initialBiStream.forEach((s, i) -> entries.add(entry(s, i)));

		assertThat(entries).containsExactly(entry("a", 1), entry("a", 2), entry("b", 1), entry("b", 2));
	}

	@Test
	public void shouldReturnTrueWhenAllEntitiesMatchPredicate(){
		boolean result = initialBiStream.allMatch((s, i) -> true);

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnTrueWhenEmptyStream(){
		Stream<String> stream = Stream.empty();

		boolean result = BiStream.<String, Integer>flatMap(stream, s -> Stream.empty()).allMatch((s, i) -> false);

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenAtLeastOneEntityNotMatchPredicate(){
		boolean result = initialBiStream.allMatch((s, i) -> i == 1);

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenAllNotMatchPredicate(){
		boolean result = initialBiStream.anyMatch((s, i) -> false);

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenAnyMatchPredicate(){
		boolean result = initialBiStream.anyMatch((s, i) -> i == 1);

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenEmptyStream(){
		Stream<String> stream = Stream.empty();

		boolean result = BiStream.flatMap(stream, s -> Stream.empty()).anyMatch((s, i) -> false);

		assertThat(result).isFalse();
	}

	@Test
	public void shouldInvokeActionOnEachPairAndNotTerminateStream(){

		List<Map.Entry<String, Integer>> result = new ArrayList<>();

		initialBiStream.peek((s, i) -> result.add(entry(s, i))).anyMatch((s, i) -> false);

		assertThat(result).containsExactly(entry("a", 1), entry("a", 2), entry("b", 1), entry("b", 2));
	}

	@Test
	public void shouldChangeLeftPartInAccordanceToFunction(){
		BiStream<String, Integer> biStream = initialBiStream.mapLeft(s -> s.toUpperCase());

		assertThat(toListOfEntries(biStream)).containsExactly(entry("A", 1), entry("A", 2), entry("B", 1), entry("B", 2));
	}

	@Test
	public void shouldChangeRightPartInAccordanceToFunction(){
		BiStream<String, Integer> biStream = initialBiStream.mapRight(i -> -i);

		assertThat(toListOfEntries(biStream)).containsExactly(entry("a", -1), entry("a", -2), entry("b", -1), entry("b", -2));
	}

	private List<Map.Entry<String, Integer>> toListOfEntries(BiStream<String, Integer> biStream){
		return biStream.toStream().map(pair -> entry(pair.getFirst(), pair.getSecond())).collect(toList());
	}

}