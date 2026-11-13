package io.github.eastezz.fairshare.group;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Checks that groups can be saved to and loaded from the (H2) database. */
@DataJpaTest
class GroupRepositoryTest {

	@Autowired
	private GroupRepository groupRepository;

	@Test
	void savesAndFindsGroup() {
		Group saved = groupRepository.save(new Group("Trip to Berlin"));

		assertThat(saved.getId()).isNotNull();
		assertThat(groupRepository.findById(saved.getId()))
			.hasValueSatisfying(found -> {
				assertThat(found.getName()).isEqualTo("Trip to Berlin");
				assertThat(found.getCreatedAt()).isNotNull();
			});
	}

}
