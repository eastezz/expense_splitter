package io.github.eastezz.fairshare.settlement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.expense.Expense;
import io.github.eastezz.fairshare.expense.ExpenseRepository;
import io.github.eastezz.fairshare.expense.ExpenseShare;
import io.github.eastezz.fairshare.group.GroupAccess;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;
import io.github.eastezz.fairshare.payment.PaymentRepository;

/**
 * Loads a group's members, expenses and payments from the database, turns them into plain
 * ExpenseEntry and PaymentEntry records and lets BalanceCalculator do the maths.
 * Read-only: it never changes anything in the database.
 */
@Service
@Transactional(readOnly = true)
public class BalanceService {

	private final GroupAccess groupAccess;

	private final MemberRepository memberRepository;

	private final ExpenseRepository expenseRepository;

	private final PaymentRepository paymentRepository;

	private final BalanceCalculator balanceCalculator;

	public BalanceService(GroupAccess groupAccess, MemberRepository memberRepository,
			ExpenseRepository expenseRepository, PaymentRepository paymentRepository,
			BalanceCalculator balanceCalculator) {
		this.groupAccess = groupAccess;
		this.memberRepository = memberRepository;
		this.expenseRepository = expenseRepository;
		this.paymentRepository = paymentRepository;
		this.balanceCalculator = balanceCalculator;
	}

	/** The balance of every member of the group, ordered by member id. 404 if the group is unknown. */
	public List<BalanceResponse> getBalances(Long groupId) {
		groupAccess.requireOwnGroup(groupId);
		List<Member> members = memberRepository.findByGroupIdOrderByIdAsc(groupId);
		// One query that also loads the shares and their members (@EntityGraph).
		List<ExpenseEntry> expenses = expenseRepository.findByGroupIdOrderByIdAsc(groupId)
			.stream()
			.map(BalanceService::toEntry)
			.toList();

		List<PaymentEntry> payments = paymentRepository.findByGroupIdOrderByIdAsc(groupId)
			.stream()
			.map(payment -> new PaymentEntry(payment.getFromMember().getId(), payment.getToMember().getId(),
					payment.getAmountCents()))
			.toList();

		Map<Long, Long> balances = balanceCalculator.calculate(members.stream().map(Member::getId).toList(), expenses,
				payments);
		return members.stream()
			.map(member -> new BalanceResponse(member.getId(), member.getName(), balances.get(member.getId())))
			.toList();
	}

	/** Copies what the calculator needs out of a JPA entity. */
	private static ExpenseEntry toEntry(Expense expense) {
		Map<Long, Long> shares = new HashMap<>();
		for (ExpenseShare share : expense.getShares()) {
			shares.put(share.getMember().getId(), share.getShareCents());
		}
		return new ExpenseEntry(expense.getPaidBy().getId(), expense.getAmountCents(), shares);
	}

}
