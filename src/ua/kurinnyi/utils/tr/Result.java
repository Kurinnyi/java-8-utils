package ua.kurinnyi.utils.tr;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static ua.kurinnyi.utils.tr.TryUtils.tr;

public class Result <T> {

	T result;

	private Exception exception;
	private boolean success;
	private boolean exceptionConsumed;

	Result (T result){
		success = true;
		this.result = result;
	}

	Result(Exception exception) {
		success = false;
		this.exception = exception;
	}

	Result() {
		success = true;
	}

	public Result<T> onSuccess(Consumer<T> consumer) {
		if (success)
			consumer.accept(result);
		return this;
	}

	public Result<T> ok(Consumer<T> consumer) {
		return onSuccess(consumer);
	}


	public Result<T> onFail(Consumer<Exception> consumer) {
		if (shouldBeConsumed()){
			consumer.accept(exception);
		}
		return this;
	}

	public Result<T> fail(Consumer<Exception> consumer) {
		return onFail(consumer);
	}

	public <EX extends Exception> Result<T> onFail(Class<EX> exceptionClass, Consumer<EX> consumer) {
		if (shouldBeConsumed() && exceptionClass.isInstance(exception)){
			consumer.accept(exceptionClass.cast(exception));
			exceptionConsumed = true;
		}

		return this;
	}

	public <EX extends Exception> Result<T> fail(Class<EX> exceptionClass, Consumer<EX> consumer) {
		return onFail(exceptionClass, consumer);
	}

	public <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
		if (success)
			return mapper.apply(result);
		return new Result<>(exception);
	}

	public <E extends Exception> Result<T> or (UnsafeSupllier<T, E > supplier){
		if (!success)
			return tr(supplier);
		return this;
	}

	public <E extends Exception, D> BiResult<T, D> and (UnsafeSupllier<D, E > supplier){
		if (success)
			try {
				return new BiResult<>(result, supplier.get());
			} catch (Exception e){
				return new BiResult<>(e);
			}
		return new BiResult<>(exception);
	}

	public boolean isSuccessful(){
		return success;
	}

	public <EX extends RuntimeException> T wrap(Class<EX> exceptionClass) {
		if (success)
			return result;

		Set<Class> applicableConstructorParameters = new HashSet<>();
		applicableConstructorParameters.add(exceptionClass);
		applicableConstructorParameters.add(Exception.class);
		applicableConstructorParameters.add(Throwable.class);

		Stream.of(exceptionClass.getConstructors())
				.filter(constructor -> constructor.getParameterCount() == 1)
				.filter(constructor -> applicableConstructorParameters.contains(constructor.getParameterTypes()[0]))
				.findFirst().ifPresent(constructor ->
					tr(() -> (EX)constructor.newInstance(exception))
					.onSuccess(exception ->  {throw exception;})
					.wrap(IllegalArgumentException.class)
				);
		throw new IllegalArgumentException("No constructor for exception in passed class");

	}

	public T wrap() {
		return throwOnFail(() -> new RuntimeException(exception));
	}

	public T wrap(Function<Exception, ? extends RuntimeException> exceptionWrapper) {
		return throwOnFail(() -> exceptionWrapper.apply(exception));
	}

	public T wrap(String message) {
		return throwOnFail(() -> new RuntimeException(message,exception));
	}

	public <R> Result<R> map(Function<? super T, R> mapper)  {
		if (success)
			return new Result<>(mapper.apply(result));
		return new Result<>(exception);
	}

	public T get() {
		return throwOnFail(()
				-> new IllegalStateException("Can't return result cause the invocation resulted to exception."));
	}

	public T orElse(T alternativeResult) {
		if (success)
			return result;
		return alternativeResult;
	}

	public T orElseGet(Supplier<T> supplier) {
		if (success)
			return result;
		return supplier.get();
	}


	private boolean shouldBeConsumed() {
		return !(success || exceptionConsumed);
	}

	private T throwOnFail(Supplier<? extends RuntimeException> exception){
		if (success)
			return result;
		throw exception.get();
	}
}