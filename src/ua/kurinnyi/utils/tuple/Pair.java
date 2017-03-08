package ua.kurinnyi.utils.tuple;


import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class Pair <L, R> {
	private L left;
	private R right;

	public static <L, R> Pair<L, R> of(L leftValue, R rightValue) {
		return new Pair<>(leftValue, rightValue);
	}

	private Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public Pair<R, L> swap() {
		return Pair.of(right, left);
	}

	public void use(BiConsumer<? super L, ? super R> consumer){
		consumer.accept(left, right);
	}

	public <T> T transform(BiFunction<? super L, ? super R, ? extends T> mapper){
		return mapper.apply(left, right);
	}

	public boolean test(BiPredicate<? super L, ? super R> predicate){
		return predicate.test(left, right);
	}

	public Optional<Pair<L,R>> filter(BiPredicate<? super L, ? super R> predicate){
		return Optional.of(this).filter(pair -> pair.test(predicate));
	}

	public <L1, R1> Pair<L1, R1> flatMap(
			BiFunction<? super L, ? super R, ? extends Pair<L1, R1>> mapper){
		return mapper.apply(left, right);
	}

	public <L1> Pair<L1,R> mapLeft(BiFunction<? super L, ? super R, ? extends L1> mapper) {
		return Pair.of(mapper.apply(left,right), right);
	}

	public <R1> Pair<L,R1> mapRight(BiFunction<? super L, ? super R, ? extends R1> mapper) {
		return Pair.of(left, mapper.apply(left,right));
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Pair)) return false;

		Pair<?, ?> otherPair = (Pair<?, ?>) other;

		return Objects.equals(left, otherPair.left) && Objects.equals(right, otherPair.right);

	}

	@Override
	public int hashCode() {
		int result = left != null ? left.hashCode() : 0;
		result = 31 * result + (right != null ? right.hashCode() : 0);
		return result;
	}
}
