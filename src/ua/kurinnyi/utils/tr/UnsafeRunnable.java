package ua.kurinnyi.utils.tr;

@FunctionalInterface
public interface UnsafeRunnable<E extends Exception> {
	void run() throws E;
}