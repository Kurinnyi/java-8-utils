package ua.kurinnyi.utils.tuple;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PairTest {


	@Test
	public void shouldSwapLeftAndRightValues() {
		Pair<String, Integer> pair = Pair.of("A", 1);

		checkPair(pair.swap(), 1, "A");
	}


	private <L,R> void checkPair(Pair<L,R> pair, L leftValue, R rightValue){
		assertThat(pair.getLeft()).isEqualTo(leftValue);
		assertThat(pair.getRight()).isEqualTo(rightValue);

	}
}