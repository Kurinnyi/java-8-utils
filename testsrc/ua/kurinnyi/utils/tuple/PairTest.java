package ua.kurinnyi.utils.tuple;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {


	private String result;

	@Test
	public void shouldSwapLeftAndRightValues() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		checkPair(pair.swap(), 1, "A");
	}


	@Test
	public void shouldProvideValuesToConsumer() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		pair.use((left, right) -> result = left + right);

		assertThat(result).isEqualTo("A1");
	}


	@Test
	public void shouldReturnResultOfFunctionCall() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		String result = pair.transform((left, right) -> left + right);

		assertThat(result).isEqualTo("A1");
	}

	@Test
	public void shouldReturnTrueWhenPredicateReturnTrue() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		boolean result = pair.test((left, right) -> left == "A");

		assertThat(result).isTrue();
	}

	@Test
	public void shouldReturnFalseWhenPredicateReturnFalse() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		boolean result = pair.test((left, right) -> left != "A");

		assertThat(result).isFalse();
	}

	@Test
	public void shouldReturnEmptyOptionalWhenPredicateReturnFalse() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		Optional<Pair<String, Integer>> result = pair.filter((left, right) -> left != "A");

		assertThat(result).isEmpty();
	}

	@Test
	public void shouldReturnOptionalWithPairWhenPredicateReturnTrue() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		Optional<Pair<String, Integer>> result = pair.filter((left, right) -> left == "A");

		assertThat(result).isNotEmpty();
		assertThat(result).contains(pair);
	}

	@Test
	public void shouldReturnCreatedPair() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		Pair<Integer, String> result = pair.flatMap((left, right) -> Pair.of(right, left));

		checkPair(result, 1, "A");
	}

	@Test
	public void shouldChangeLeftValueInAccordanceToFunction() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		Pair<Integer, Integer> result = pair.mapLeft((left, right) -> right);

		checkPair(result, 1, 1);
	}

	@Test
	public void shouldChangeRightValueInAccordanceToFunction() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		Pair<String, String> result = pair.mapRight((left, right) -> left);

		checkPair(result, "A", "A");
	}


	private <L,R> void checkPair(Pair<L,R> pair, L leftValue, R rightValue){
		assertThat(pair.getLeft()).isEqualTo(leftValue);
		assertThat(pair.getRight()).isEqualTo(rightValue);
	}
}