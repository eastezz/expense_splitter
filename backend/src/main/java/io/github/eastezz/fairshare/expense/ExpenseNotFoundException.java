package io.github.eastezz.fairshare.expense;

/**
 * Thrown when an expense id does not exist in the given group (also when it exists,
 * but in another group). GlobalExceptionHandler turns it into a 404 Not Found response.
 */
public class ExpenseNotFoundException extends RuntimeException {

	public ExpenseNotFoundException(Long groupId, Long expenseId) {
		super("Expense " + expenseId + " not found in group " + groupId);
	}

}
