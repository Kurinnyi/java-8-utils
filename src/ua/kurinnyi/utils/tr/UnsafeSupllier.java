package ua.kurinnyi.utils.tr;

@FunctionalInterface
public interface UnsafeSupllier<T, E extends Exception> {
	T get() throws E;
}
