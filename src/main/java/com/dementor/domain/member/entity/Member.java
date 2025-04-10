package com.dementor.domain.member.entity;

import com.dementor.global.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Email
	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String nickname;

	@Column(nullable = false)
	private UserRole userRole;

	public Member() {

	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

}


