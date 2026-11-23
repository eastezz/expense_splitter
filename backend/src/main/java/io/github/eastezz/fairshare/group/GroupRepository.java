package io.github.eastezz.fairshare.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for groups. Spring Data generates the implementation (save, findById, findAll, ...). */
public interface GroupRepository extends JpaRepository<Group, Long> {

	/** The groups of one user, oldest first. */
	List<Group> findByOwnerIdOrderByIdAsc(Long ownerId);

	/** One group, but only if it belongs to the given user. */
	Optional<Group> findByIdAndOwnerId(Long id, Long ownerId);

}