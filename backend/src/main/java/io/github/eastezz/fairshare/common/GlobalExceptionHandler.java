package io.github.eastezz.fairshare.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.eastezz.fairshare.expense.InvalidExpenseException;
import io.github.eastezz.fairshare.group.GroupNotFoundException;
import io.github.eastezz.fairshare.member.DuplicateMemberNameException;

/**
 * Turns exceptions from any controller into clean JSON error responses.
 * ProblemDetail is the standard error format from RFC 9457
 * (fields: type, title, status, detail, instance).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** Unknown group id: 404 Not Found. */
	@ExceptionHandler(GroupNotFoundException.class)
	public ProblemDetail handleGroupNotFound(GroupNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Not Found");
		return problem;
	}

	/** Member name already used in this group: 409 Conflict. */
	@ExceptionHandler(DuplicateMemberNameException.class)
	public ProblemDetail handleDuplicateMemberName(DuplicateMemberNameException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Conflict");
		return problem;
	}

	/** Expense that breaks a business rule (for example shares that don't add up): 400 Bad Request. */
	@ExceptionHandler(InvalidExpenseException.class)
	public ProblemDetail handleInvalidExpense(InvalidExpenseException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Invalid expense");
		return problem;
	}

	/** Invalid request body (for example a blank name): 400 Bad Request with field -> message. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
		problem.setTitle("Bad Request");
		problem.setProperty("errors", errors);
		return problem;
	}

}
