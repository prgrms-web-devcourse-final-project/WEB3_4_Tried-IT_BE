package com.dementor.domain.mentoringclass.entity;

import com.dementor.domain.mentor.entity.Mentor;
import com.dementor.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mentoring_class")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentoringClass extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String title;

    private String stack;

    @Getter
    private String content;

    @Getter
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    @Getter
    private Mentor mentor;

    public void updateTitle(String title) {
        this.title = title;
    }

    public String[] getStack() { // getter 쓰면 String 배열로 지정 못함
        return this.stack.split(",");
    }

    public void updateContent(String description) {
        this.content = description;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    public void updateStack(String[] stack) {
        this.stack = String.join(",", stack);
    }

}
