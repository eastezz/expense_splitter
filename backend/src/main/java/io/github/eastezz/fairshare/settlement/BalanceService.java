package io.github.eastezz.fairshare.settlement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.expense.Expense;
import io.github.eastezz.fairshare.expense.ExpenseRepository;
import io.github.eastezz.fairshare.expense.ExpenseShare;
import io.github.eastezz.fairshare.group.GroupNotFoundException;
import io.github.eastezz.fairshare.group.GroupRepository;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;

/**
 * Loads a group's members and expenses from the database, turns the expenses into plain
 * ExpenseEntry records and lets BalanceCalculator do the maths.
 * Read-only: it never changes anything in the database.
 */
@Service
@Transactional(readOnly = true)
public class BalanceService {

	private final GroupRepository groupRepository;

	private final MemberRepository memberRepository;

	private final ExpenseRepository expenseRepository;

	private final BalanceCalculator balanceCalculator;

	public BalanceService(GroupRepository groupRepository, MemberRepository memberRepository,
			ExpenseRepository expenseRepository, BalanceCalculator balanceCalculator) {
		this.groupRepository = groupRepository;
		this.memberRepository = memberRepository;
		this.expenseRepository = expenseRepository;
		this.balanceCalculator = balanceCalculator;
	}

	/** The balance of every member of the group, ordered by member id. 404 if the group is unknown. */
	public List<BalanceResponse> getBalances(Long groupId) {
		if (!groupRepository.existsById(groupId)) {
			throw new GroupNotFoundException(groupId);
		}
		List<Member> members = memberRepository.findByGroupIdOrderByIdAsc(groupId);
		// One query that also loads the shares and their members (@EntityGraph).
		List<ExpenseEntry> expenses = expenseRepository.findByGroupIdOrderByIdAsc(groupId)
			.stream()
			.map(BalanceService::toEntry)
			.toList();

		Map<Long, Long> balances = balanceCalculator.calculate(members.stream().map(Member::getId).toList(), expenses);
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
