package ua.kurinnyi.utils.tr;

public class TryUtils {

	public static <T, E extends Exception> T uncheck(UnsafeSupllier<T, E> supplier){
		return tr(supplier).wrap();
	}

	public static <E extends Exception> void uncheck(UnsafeRunnable<E> supplier){
		tr(supplier).wrap();
	}

	public static <T, E extends Exception> Result<T> tr(UnsafeSupllier<T, E > supplier) {
		try {
			return new Result<>(supplier.get());
		} catch (Exception e){
			return new Result<>(e);
		}
	}

	public static <E extends Exception> Result<Void> tr(UnsafeRunnable<E> action) {
		try {
			action.run();
			return new Result<>();
		} catch (Exception e){
			return new Result<>(e);
		}
	}

}
