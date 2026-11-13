package io.github.eastezz.fairshare.member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for members. Spring Data builds the queries from the method names,
 * e.g. "findByGroupIdOrderByIdAsc" means: WHERE group_id = ? ORDER BY id ASC.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

	List<Member> findByGroupIdOrderByIdAsc(Long groupId);

	boolean existsByGroupIdAndName(Long groupId, String name);

}
