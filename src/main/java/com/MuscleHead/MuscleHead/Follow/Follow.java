package com.MuscleHead.MuscleHead.Follow;

import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "follower_sub_id", "followee_sub_id" })
})
@Data
@NoArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followee_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User followee;
}
