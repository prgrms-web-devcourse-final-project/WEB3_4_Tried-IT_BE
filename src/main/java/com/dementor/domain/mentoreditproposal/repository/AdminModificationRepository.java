package com.dementor.domain.mentoreditproposal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dementor.domain.mentoreditproposal.entity.MentorEditProposal;

public interface AdminModificationRepository extends JpaRepository<MentorEditProposal, Long> {
}
