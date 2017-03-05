package ua.kurinnyi.utils.tuple;


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
}
