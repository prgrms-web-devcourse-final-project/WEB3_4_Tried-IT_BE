package com.dementor.domain.mentoringclass.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayOfWeek;
    private int time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_class_id")
    private MentoringClass mentoringClass;
} 