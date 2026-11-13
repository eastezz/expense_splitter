package io.github.eastezz.fairshare.group;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for groups. Spring Data generates the implementation (save, findById, findAll, ...). */
public interface GroupRepository extends JpaRepository<Group, Long> {
}
