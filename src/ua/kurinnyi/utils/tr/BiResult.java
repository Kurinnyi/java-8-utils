package ua.kurinnyi.utils.tr;

import java.util.function.BiConsumer;

public class BiResult  <T, D> extends Result<T> {
	private D secondResult;

	BiResult (T result, D secondResult){
		super(result);
		this.secondResult = secondResult;
	}

	BiResult(Exception exception) {
		super(exception);
	}

	public BiResult<T, D> onSuccess(BiConsumer<T, D> consumer) {
		if (isSuccessful())
			consumer.accept(result, secondResult);
		return this;
	}

	public BiResult<T, D> ok(BiConsumer<T, D> consumer) {
		return onSuccess(consumer);
	}

}