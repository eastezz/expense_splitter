package io.github.eastezz.fairshare.expense;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupAccess;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;

/**
 * Business logic for expenses: checks that the payer and everyone in the shares belong
 * to the group, lets SplitCalculator work out the cents, and saves the result.
 * Responses are built inside the transaction, so lazy relations can still be loaded.
 */
@Service
@Transactional
public class ExpenseService {

	private final ExpenseRepository expenseRepository;

	private final GroupAccess groupAccess;

	private final MemberRepository memberRepository;

	private final SplitCalculator splitCalculator;

	public ExpenseService(ExpenseRepository expenseRepository, GroupAccess groupAccess,
			MemberRepository memberRepository, SplitCalculator splitCalculator) {
		this.expenseRepository = expenseRepository;
		this.groupAccess = groupAccess;
		this.memberRepository = memberRepository;
		this.splitCalculator = splitCalculator;
	}

	/** Creates an expense in a group. 404 if the group is unknown, 400 if the expense is invalid. */
	public ExpenseResponse create(Long groupId, CreateExpenseRequest request) {
		Group group = groupAccess.requireOwnGroup(groupId);
		Member payer = findMemberOfGroup(groupId, request.paidByMemberId());

		Map<Long, Member> members = new HashMap<>();
		Set<Long> seen = new HashSet<>();
		for (ShareRequest share : request.shares()) {
			if (!seen.add(share.memberId())) {
				throw new InvalidExpenseException("Member " + share.memberId() + " appears more than once in the shares");
			}
			members.put(share.memberId(), findMemberOfGroup(groupId, share.memberId()));
		}

		Map<Long, Long> cents = calculateShares(request);

		Expense expense = new Expense(group, request.description().trim(), request.amountCents(), payer,
				request.splitType());
		cents.forEach((memberId, shareCents) -> expense.addShare(members.get(memberId), shareCents));
		return ExpenseResponse.from(expenseRepository.save(expense));
	}

	/** Lists the expenses of a group in the order they were added. 404 if the group is unknown. */
	@Transactional(readOnly = true)
	public List<ExpenseResponse> findByGroup(Long groupId) {
		groupAccess.requireOwnGroup(groupId);
		return expenseRepository.findByGroupIdOrderByIdAsc(groupId).stream().map(ExpenseResponse::from).toList();
	}

	/** Returns one expense of a group. 404 if it doesn't exist or belongs to another group. */
	@Transactional(readOnly = true)
	public ExpenseResponse findById(Long groupId, Long expenseId) {
		return ExpenseResponse.from(findExpenseOfGroup(groupId, expenseId));
	}

	/** Deletes one expense; its shares are deleted with it (cascade). 404 like findById. */
	public void delete(Long groupId, Long expenseId) {
		expenseRepository.delete(findExpenseOfGroup(groupId, expenseId));
	}

	private Expense findExpenseOfGroup(Long groupId, Long expenseId) {
		// First the group: someone else's group gives 404 before we even look for the expense.
		groupAccess.requireOwnGroup(groupId);
		return expenseRepository.findByIdAndGroupId(expenseId, groupId)
			.orElseThrow(() -> new ExpenseNotFoundException(groupId, expenseId));
	}

	/** Uses the split type to turn the requested shares into cents per member id. */
	private Map<Long, Long> calculateShares(CreateExpenseRequest request) {
		if (request.splitType() == SplitType.EQUAL) {
			if (request.shares().stream().anyMatch(share -> share.amountCents() != null)) {
				throw new InvalidExpenseException("Leave out amountCents in the shares for an EQUAL split");
			}
			List<Long> memberIds = request.shares().stream().map(ShareRequest::memberId).toList();
			return splitCalculator.splitEqually(request.amountCents(), memberIds);
		}
		Map<Long, Long> amounts = new LinkedHashMap<>();
		for (ShareRequest share : request.shares()) {
			if (share.amountCents() == null) {
				throw new InvalidExpenseException(
						"Member " + share.memberId() + " needs an amountCents for an EXACT split");
			}
			amounts.put(share.memberId(), share.amountCents());
		}
		return splitCalculator.validateExact(request.amountCents(), amounts);
	}

	/** Loads a member and checks that it belongs to the given group (otherwise 400). */
	private Member findMemberOfGroup(Long groupId, Long memberId) {
		return memberRepository.findById(memberId)
			.filter(member -> member.getGroup().getId().equals(groupId))
			.orElseThrow(() -> new InvalidExpenseException("Member " + memberId + " is not in group " + groupId));
	}

}
