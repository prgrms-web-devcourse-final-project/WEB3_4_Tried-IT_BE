package com.dementor.domain.job.entity;

import com.dementor.domain.mentor.entity.Mentor;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Job {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	// Mentor 엔티티와의 관계 (일대다)
	@OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
	private List<Mentor> mentors;

	public void updateName(String name) {
		this.name = name;
	}
}

