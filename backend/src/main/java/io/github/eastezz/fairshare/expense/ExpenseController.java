package io.github.eastezz.fairshare.expense;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for the expenses of a group under /api/groups/{groupId}/expenses. */
@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

	private final ExpenseService expenseService;

	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}

	/** POST /api/groups/{groupId}/expenses: adds an expense, answers 201 Created. */
	@PostMapping
	public ResponseEntity<ExpenseResponse> create(@PathVariable Long groupId,
			@Valid @RequestBody CreateExpenseRequest request) {
		ExpenseResponse created = expenseService.create(groupId, request);
		URI location = URI.create("/api/groups/" + groupId + "/expenses/" + created.id());
		return ResponseEntity.created(location).body(created);
	}

	/** GET /api/groups/{groupId}/expenses: lists the expenses of the group. */
	@GetMapping
	public List<ExpenseResponse> list(@PathVariable Long groupId) {
		return expenseService.findByGroup(groupId);
	}

}
