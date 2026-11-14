package io.github.eastezz.fairshare.expense;

/**
 * Thrown when an expense does not make sense, for example when the shares don't add up
 * to the total. GlobalExceptionHandler turns it into a 400 Bad Request response.
 */
public class InvalidExpenseException extends RuntimeException {

	public InvalidExpenseException(String message) {
		super(message);
	}

}
