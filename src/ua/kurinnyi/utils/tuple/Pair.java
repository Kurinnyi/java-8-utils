package ua.kurinnyi.utils.tuple;


public class Pair <T, R> {
	private T first;
	private R second;

	public static <T, R> Pair<T, R> of(T leftValue, R rightValue) {
		return new Pair<>(leftValue, rightValue);
	}

	private Pair(T first, R second) {
		this.first = first;
		this.second = second;
	}

	public T getLeft() {
		return first;
	}

	public R getRight() {
		return second;
	}
}
