package io.github.eastezz.fairshare.payment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupNotFoundException;
import io.github.eastezz.fairshare.group.GroupRepository;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;

/**
 * Business logic for payments: both members must belong to the group and must be two
 * different people. Responses are built inside the transaction (lazy relations).
 */
@Service
@Transactional
public class PaymentService {

	private final PaymentRepository paymentRepository;

	private final GroupRepository groupRepository;

	private final MemberRepository memberRepository;

	public PaymentService(PaymentRepository paymentRepository, GroupRepository groupRepository,
			MemberRepository memberRepository) {
		this.paymentRepository = paymentRepository;
		this.groupRepository = groupRepository;
		this.memberRepository = memberRepository;
	}

	/** Records a payment in a group. 404 if the group is unknown, 400 if the payment is invalid. */
	public PaymentResponse create(Long groupId, CreatePaymentRequest request) {
		Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
		if (request.fromMemberId().equals(request.toMemberId())) {
			throw new InvalidPaymentException("A member cannot pay themselves");
		}
		Member from = findMemberOfGroup(groupId, request.fromMemberId());
		Member to = findMemberOfGroup(groupId, request.toMemberId());
		return PaymentResponse.from(paymentRepository.save(new Payment(group, from, to, request.amountCents())));
	}

	/** Lists the payments of a group in the order they were made. 404 if the group is unknown. */
	@Transactional(readOnly = true)
	public List<PaymentResponse> findByGroup(Long groupId) {
		if (!groupRepository.existsById(groupId)) {
			throw new GroupNotFoundException(groupId);
		}
		return paymentRepository.findByGroupIdOrderByIdAsc(groupId).stream().map(PaymentResponse::from).toList();
	}

	/** Loads a member and checks that it belongs to the given group (otherwise 400). */
	private Member findMemberOfGroup(Long groupId, Long memberId) {
		return memberRepository.findById(memberId)
			.filter(member -> member.getGroup().getId().equals(groupId))
			.orElseThrow(() -> new InvalidPaymentException("Member " + memberId + " is not in group " + groupId));
	}

}
