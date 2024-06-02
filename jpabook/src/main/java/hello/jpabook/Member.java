package hello.jpabook;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table
public class Member {

    @Id
    private String id;
    private String username;
    private Integer age;

    public Member() {}
}
