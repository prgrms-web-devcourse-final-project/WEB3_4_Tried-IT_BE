package com.dementor.domain.admin.entity;

import com.dementor.global.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import jakarta.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Table(name = "admin")
public class Admin extends BaseEntity {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	public Admin() {
	}
}
