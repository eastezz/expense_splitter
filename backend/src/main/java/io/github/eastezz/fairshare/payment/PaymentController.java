package io.github.eastezz.fairshare.payment;

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

/** REST endpoints for the payments of a group under /api/groups/{groupId}/payments. */
@RestController
@RequestMapping("/api/groups/{groupId}/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	/** POST /api/groups/{groupId}/payments: records a payment, answers 201 Created. */
	@PostMapping
	public ResponseEntity<PaymentResponse> create(@PathVariable Long groupId,
			@Valid @RequestBody CreatePaymentRequest request) {
		PaymentResponse created = paymentService.create(groupId, request);
		URI location = URI.create("/api/groups/" + groupId + "/payments/" + created.id());
		return ResponseEntity.created(location).body(created);
	}

	/** GET /api/groups/{groupId}/payments: lists the payments of the group. */
	@GetMapping
	public List<PaymentResponse> list(@PathVariable Long groupId) {
		return paymentService.findByGroup(groupId);
	}

}
