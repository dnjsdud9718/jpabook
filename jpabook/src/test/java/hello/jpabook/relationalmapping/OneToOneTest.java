package hello.jpabook.relationalmapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OneToOneTest {
    @Entity
    @Getter
    @Setter
    static class Member {

        @Id
        @GeneratedValue
        @Column(name = "member_id")
        private Long id;

        private String username;

        @OneToOne
        @JoinColumn(name = "locker_id") // 주테이블 외래키
        private Locker locker;
    }

    @Entity
    @Getter
    @Setter
    static class Locker {

        @Id
        @GeneratedValue
        @Column(name = "locker_id")
        private Long id;
        private String name;
        @OneToOne(mappedBy = "locker")
        private Member member;
    }
}
