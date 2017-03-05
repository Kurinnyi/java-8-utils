package ua.kurinnyi.utils.stream;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
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
		boolean result = getEmptyBiStream().allMatch((s, i) -> false);

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
		boolean result = getEmptyBiStream().anyMatch((s, i) -> false);

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

	@Test
	public void shouldSwapLeftAndRightPart(){
		BiStream<Integer, String> biStream = initialBiStream.swap();

		assertThat(toListOfEntries(biStream)).containsExactly(entry(1, "a"), entry(2, "a"), entry(1, "b"), entry(2, "b"));
	}

	@Test
	public void shouldCollectValuesToMapOverridingExistingKeys(){
		Map<String, Integer> result = initialBiStream.toMap();

		assertThat(result).containsExactly(entry("a", 2), entry("b", 2));
	}

	@Test
	public void shouldReturnEmptyMapForEmptyStream(){
		Map<String, Integer> result = getEmptyBiStream().toMap();

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldUseFunctionToHandleDuplicationOfKeys(){
		Map<String, Integer> result = initialBiStream.toMap((left, right) -> left + right);

		assertThat(result).containsExactly(entry("a", 3), entry("b", 3));
	}


	@Test
	public void shouldUseValueFromFirstListAsLeftAndFromSecondAsRight(){
		List<String> list1 = Arrays.asList("a", "b");
		List<String> list2 = Arrays.asList("c", "d");

		BiStream<String, String> biStream = BiStream.combineLists(list1, list2);

		assertThat(toListOfEntries(biStream)).containsExactly(entry("a", "c"), entry("b", "d"));
	}


	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfListsHasDifferentSize(){
		List<String> list1 = Arrays.asList("a", "b");
		List<String> list2 = Arrays.asList("c");

		BiStream.combineLists(list1, list2);
	}

	@Test
	public void shouldReturnEmptyBiStreamIfListsAreEmpty(){
		List<String> list1 = Collections.emptyList();
		List<String> list2 = Collections.emptyList();

		BiStream<String, String> biStream = BiStream.combineLists(list1, list2);

		assertThat(toListOfEntries(biStream)).isEmpty();
	}

	@Test
	public void shouldReturnBiStreamContainingValueFromMap(){
		Map<String, String> map = new HashMap<String, String>(){{
			put("a", "b");
			put("c", "d");
		}};

		BiStream<String, String> biStream = BiStream.fromMap(map);

		assertThat(toListOfEntries(biStream)).containsExactly(entry("a", "b"), entry("c", "d"));
	}

	@Test
	public void shouldReturnEmptyBiStreamForEmptyMap(){
		Map<String, String> map = new HashMap();

		BiStream<String, String> biStream = BiStream.fromMap(map);

		assertThat(toListOfEntries(biStream)).isEmpty();
	}

	@Test
	public void shouldUseFunctionToProduceRightValuesFromInitialStream(){
		Stream<String> stream = Stream.of("a", "b");

		BiStream<String, String> biStream = BiStream.map(stream, s -> s.toUpperCase());

		assertThat(toListOfEntries(biStream)).containsExactly(entry("a", "A"), entry("b", "B"));
	}

	@Test
	public void shouldReturnEmptyBiStreamForEmptyStream(){
		Stream<String> stream = Stream.empty();

		BiStream<String, String> biStream = BiStream.map(stream, s -> s.toUpperCase());

		assertThat(toListOfEntries(biStream)).isEmpty();
	}

	@Test
	public void shouldUseStreamOfPairsToProduceBiStream(){
		Stream<BiStream.Pair<String, String>> stream = Stream.of(BiStream.Pair.of("a", "b"), BiStream.Pair.of("c", "d"));

		BiStream<String, String> biStream = BiStream.fromStream(stream);

		assertThat(toListOfEntries(biStream)).containsExactly(entry("a", "b"), entry("c", "d"));
	}



	private <T, R> List<Map.Entry<T, R>> toListOfEntries(BiStream<T, R> biStream){
		return biStream.toStream().map(pair -> entry(pair.getFirst(), pair.getSecond())).collect(toList());
	}


	private BiStream<String, Integer> getEmptyBiStream() {
		Stream<String> stream = Stream.empty();

		return BiStream.flatMap(stream, s -> Stream.empty());
	}

}