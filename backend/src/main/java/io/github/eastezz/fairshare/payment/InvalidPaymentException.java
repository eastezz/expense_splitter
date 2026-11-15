package io.github.eastezz.fairshare.payment;

/**
 * Thrown when a payment does not make sense, for example when a member would pay
 * themselves. GlobalExceptionHandler turns it into a 400 Bad Request response.
 */
public class InvalidPaymentException extends RuntimeException {

	public InvalidPaymentException(String message) {
		super(message);
	}

}
