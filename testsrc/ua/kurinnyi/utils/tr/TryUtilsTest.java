package ua.kurinnyi.utils.tr;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static ua.kurinnyi.utils.tr.TryUtils.tr;
import static ua.kurinnyi.utils.tr.TryUtils.uncheck;

public class TryUtilsTest {

	private static final String RESULT = "result";
	private static final String MESSAGE = "message";
	private static final String SECOND_RESULT = "second result";
	private IOException thrownException;

	@Test
	public void shouldReturnValueIfNoException(){
		assertThat(uncheck(() -> RESULT)).isEqualTo(RESULT);
	}

	@Test
	public void shouldRethrowException(){
		IOException exception = new IOException();

		RuntimeException result = null;
		try {
			uncheck(() ->  {throw exception;});
		} catch (RuntimeException e){
			result = e;
		}

		assertThat(result).isNotNull();
		assertThat(result).hasCause(exception);
	}

	@Test
	public void shouldRethrowExceptionWithRunnable(){
		IOException exception = new IOException();

		tr(() -> uncheck(() ->  {
				if (true){
					throw exception;
				}
			})
		).ok(r -> fail("Exception is not thrown"))
		.fail(e -> assertThat(e).hasCause(exception));
	}

	@Test
	public void shouldCallConsumerWithResult(){
		List<String> results = mock(List.class);

		tr(() -> RESULT).onSuccess(results::add);

		verify(results).add(RESULT);
	}

	@Test
	public void shouldNotCallConsumerWithResultWhenException(){
		List<String> results = mock(List.class);

		tr(this::unsafeMethod).onSuccess(results::add);

		verify(results, never()).add(any());
	}

	@Test
	public void shouldCallConsumerWithNullResult(){
		List<String> results = mock(List.class);

		tr(() -> (String)null).onSuccess(results::add);

		verify(results).add(null);
	}

	@Test
	public void shouldCallFailConsumerWhenException(){
		List<Exception> results = mock(List.class);

		tr(this::unsafeMethod).onFail(results::add);

		verify(results).add(any(IOException.class));
	}

	@Test
	public void shouldNotCallFailConsumerWhenNoException(){
		List<Exception> results = mock(List.class);

		tr(() -> RESULT).onFail(results::add);

		verify(results, never()).add(any());
	}

	@Test
	public void shouldCallOnlyFailConsumerWhenExceptionAndBothSpecified(){
		List<Exception> results = mock(List.class);
		List<String> successResults = mock(List.class);

		tr(this::unsafeMethod)
				.onSuccess(successResults::add)
				.onFail(results::add);

		verify(results).add(any(IOException.class));
		verify(successResults, never()).add(any());
	}

	@Test
	public void shouldCallOnlySuccessConsumerWhenNoExceptionAndBothSpecified(){
		List<Exception> results = mock(List.class);
		List<String> successResults = mock(List.class);

		tr(() -> RESULT)
				.onSuccess(successResults::add)
				.onFail(results::add);

		verify(successResults).add(RESULT);
		verify(results, never()).add(any());
	}

	@Test
	public void shouldChainSuccessConsumers(){
		List<Exception> results = mock(List.class);
		List<String> successResults = mock(List.class);

		tr(() -> RESULT)
				.onSuccess(successResults::add)
				.onSuccess(successResults::add)
				.onFail(results::add);

		verify(successResults, times(2)).add(RESULT);
		verify(results, never()).add(any());
	}


	@Test
	public void shouldCastExceptionWhenProvided(){
		List<IllegalArgumentException> results = mock(List.class);

		tr(this::unsafeMethodRuntime)
				.onFail(IllegalArgumentException.class, results::add);

		verify(results).add(any(IllegalArgumentException.class));
	}

	@Test
	public void shouldChainExceptionConsumers(){
		List<Exception> results = mock(List.class);

		tr(this::unsafeMethod)
				.onFail(IllegalArgumentException.class, results::add)
				.onFail(IOException.class, results::add);

		verify(results).add(any(IOException.class));
	}

	@Test
	public void shouldNotCallGeneralFailConsumerIfExceptionWasConsumedBefore(){
		List<Exception> results = mock(List.class);

		tr(this::unsafeMethod)
				.onFail(IllegalArgumentException.class, results::add)
				.onFail(IOException.class, results::add)
				.onFail(results::add);

		verify(results, times(1)).add(any(IOException.class));
	}


	@Test
	public void shouldRethrowExceptionWrapped(){
		tr(() -> tr(this::unsafeMethod).wrap(IllegalStateException.class))
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception.getClass()).isEqualTo(IllegalStateException.class))
				.fail(exception -> assertThat(exception).hasCause(thrownException));
	}

	@Test
	public void shouldThrowIllegalArgumentExceptionWhenPassedClassWithNoApplicableConstructor(){
		tr(() -> tr(this::unsafeMethod).wrap(ExceptionWithNoConstructor.class))
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception.getClass()).isEqualTo(IllegalArgumentException.class));
	}

	@Test
	public void shouldRethrowExceptionWrappedToRuntimeException(){
		tr(() -> tr(this::unsafeMethod).wrap())
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception.getClass()).isEqualTo(RuntimeException.class))
				.fail(exception -> assertThat(exception).hasCause(thrownException));
	}

	@Test
	public void shouldRethrowExceptionWrappedToRuntimeExceptionWithMessage(){
		tr(() -> tr(this::unsafeMethod).wrap(MESSAGE))
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception.getMessage()).isEqualTo(MESSAGE))
				.fail(exception -> assertThat(exception).hasCause(thrownException));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRethrowExceptionWrappedBySupplier(){
		tr(this::unsafeMethod).wrap(e -> new IllegalArgumentException("message", e));
	}

	@Test
	public void shouldReturnResultForTheSecondCallIfFirstIsSuccess(){
		tr(() -> RESULT).flatMap(result -> tr(() -> SECOND_RESULT))
				.ok(result -> assertThat(result).isEqualTo(SECOND_RESULT))
				.wrap();
	}

	@Test
	public void shouldContainFirstExceptionIfFirstMethodIsFailed(){
		tr(this::unsafeMethod).flatMap(result -> tr(this::unsafeMethodRuntime))
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception).isEqualTo(thrownException));
	}

	@Test
	public void shouldContainSecondExceptionIfSecondMethodIsFailed(){
		tr(() -> RESULT).flatMap(result -> tr(this::unsafeMethod))
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception).isEqualTo(thrownException));
	}

	@Test
	public void shouldContainFirstResultIfItSuccess(){
		tr(() -> RESULT).or(() -> SECOND_RESULT)
				.ok(result -> assertThat(result).isEqualTo(RESULT))
				.wrap();
	}

	@Test
	public void shouldContainFirstResultIfSecondFail(){
		tr(() -> RESULT).or(this::unsafeMethod)
				.ok(result -> assertThat(result).isEqualTo(RESULT))
				.wrap();
	}

	@Test
	public void shouldContainSecondResultIfFirstFail(){
		tr(this::unsafeMethod).or(() -> RESULT)
				.ok(result -> assertThat(result).isEqualTo(RESULT))
				.wrap();
	}

	@Test
	public void shouldContainSecondExceptionIfBothFail(){
		tr(this::unsafeMethodRuntime).or(this::unsafeMethod)
				.ok(r -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception).isEqualTo(thrownException));
	}

	@Test
	public void shouldConsumeBothResultIfBothSuccess(){
		tr(() -> RESULT).and(() -> SECOND_RESULT)
				.ok((result, result2) -> {
					assertThat(result).isEqualTo(RESULT);
					assertThat(result2).isEqualTo(SECOND_RESULT);
				})
				.wrap();
	}

	@Test
	public void shouldContainFirstExceptionIfBothFailed(){
		tr(this::unsafeMethod).and(this::unsafeMethodRuntime)
				.ok((result, result2) -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception).isEqualTo(thrownException));
	}

	@Test
	public void shouldContainSecondExceptionIfSecondFailed(){
		tr(() -> RESULT).and(this::unsafeMethod)
				.ok((result, result2) -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception).isEqualTo(thrownException));
	}

	@Test
	public void shouldApplyMapFunctionToResult(){
		tr(() -> RESULT).map(String::toUpperCase)
				.ok(result -> assertThat(result).isEqualTo(RESULT.toUpperCase()))
				.wrap();
	}

	@Test
	public void shouldPassExceptionToNewResultIfFail(){
		tr(this::unsafeMethod).map(String::toUpperCase)
				.ok(result -> fail("Exception is not thrown"))
				.fail(exception -> assertThat(exception).isEqualTo(thrownException));
	}

	@Test
	public void shouldReturnTrueIfSuccessful(){
		assertThat(tr(() -> RESULT).isSuccessful()).isTrue();
	}

	@Test
	public void shouldReturnFalseIfFail(){
		assertThat(tr(this::unsafeMethod).isSuccessful()).isFalse();
	}

	@Test
	public void shouldReturnResultIfSuccessful(){
		assertThat(tr(() -> RESULT).get()).isEqualTo(RESULT);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowExceptionIfFailed(){
		tr(this::unsafeMethod).get();
	}

	@Test
	public void shouldReturnResultOfInvocationIfSuccessful(){
		assertThat(tr(() -> RESULT).orElse(SECOND_RESULT)).isEqualTo(RESULT);
	}

	@Test
	public void shouldReturnAlternativeResultIfFail(){
		assertThat(tr(this::unsafeMethod).orElse(SECOND_RESULT)).isEqualTo(SECOND_RESULT);
	}

	@Test
	public void shouldReturnAlternativeResultFromSupplierIfFail(){
		assertThat(tr(this::unsafeMethod).orElseGet(() -> SECOND_RESULT)).isEqualTo(SECOND_RESULT);
	}

	@Test(expected = RuntimeException.class)
	public void example(){

		//this is almost equals to ...
		tr(this::unsafeMethod).ok(System.out::println)
				.fail(IOException.class, e -> System.err.print("First method fail"))
				.or(this::unsafeMethod).ok(System.out::println)
				.fail(IOException.class, e -> System.err.print("Both method failed"))
				.fail(IllegalArgumentException.class, e -> System.err.print("Some of two failed"))
				.wrap();

		//...this. Code bellow as well as above is a mess and shouldn't be used. It's only for example.
		try {
			try {
				System.out.println(unsafeMethod());
			} catch (IOException e) {
				System.err.print("First method fail");
				try {
					System.out.println(unsafeMethod());
				} catch (IOException e1) {
					System.err.print("Both method fail");
				}
			}
		} catch (IllegalArgumentException e){
			System.err.print("Some of two failed");
		}  catch (Exception e){
			throw new RuntimeException(e);
		}

	}

	@Test
	public void secondExample(){

		//this is almost equals to ...
		String result = tr(this::unsafeMethod).fail(Exception::printStackTrace).orElse(RESULT);

		//...this.
		result = RESULT;
		try {
			result = unsafeMethod();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private String unsafeMethod() throws IOException {
		thrownException = new IOException();
		throw thrownException;
	}

	private String unsafeMethodRuntime() {
		throw  new IllegalArgumentException();
	}

	public static class ExceptionWithNoConstructor extends RuntimeException{

	}
}