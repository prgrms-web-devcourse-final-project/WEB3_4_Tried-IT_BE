package com.dementor.domain.mentoringclass.entity;

import com.dementor.domain.mentor.entity.Mentor;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mentoring_class")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentoringClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;

    private String stack;

    private String content;

    private int price;

    @OneToMany(mappedBy = "mentoringClass", cascade = CascadeType.ALL)
    private List<Schedule> schedules = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;
}
