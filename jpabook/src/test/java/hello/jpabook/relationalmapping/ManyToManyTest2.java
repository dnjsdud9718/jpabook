package hello.jpabook.relationalmapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * N:N -> 1 : N , N :1 로 풀어서 해결
 * 연결 테이블에 추가적인 컬럼(orderAmount)
 * 복합 키 사용(외래키를 기본키로 사용)
 * 복합 키 -> 식별자 클래스 필요
 */
@SpringBootTest
@Slf4j
public class ManyToManyTest2 {

    @Autowired
    EntityManagerFactory emf;


    @Test
    void save() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            // save member
            Member member1 = new Member();
            member1.setId("member1");
            member1.setUsername("회원1");
            em.persist(member1);

            // save product
            Product productA = new Product();
            productA.setId("productA");
            productA.setName("상품1");
            em.persist(productA);

            // save MemberProduct
            MemberProduct memberProduct = new MemberProduct();
            memberProduct.setMember(member1);
            memberProduct.setProduct(productA);
            memberProduct.setOrderAmount(2);
            em.persist(memberProduct);

            tx.commit();
        } catch (Exception e) {
            log.info("error", e);
            tx.rollback();
        }finally {
            em.close();
        }
    }

    @Test
    void find() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            // save member
            Member member1 = new Member();
            member1.setId("member1");
            member1.setUsername("회원1");
            em.persist(member1);

            // save product
            Product productA = new Product();
            productA.setId("productA");
            productA.setName("상품1");
            em.persist(productA);

            // save MemberProduct
            MemberProduct memberProduct = new MemberProduct();
            memberProduct.setMember(member1);
            memberProduct.setProduct(productA);
            memberProduct.setOrderAmount(2);
            em.persist(memberProduct);


            // flush & clean
            em.flush();
            em.clear();

            // pk 생성
            MemberProductId memberProductId = new MemberProductId();
            memberProductId.setMember("member1");
            memberProductId.setProduct("productA");

            memberProduct = em.find(MemberProduct.class, memberProductId);

            Member findMember = memberProduct.getMember();
            Product findProduct = memberProduct.getProduct();
            log.info("member = {}", findMember.getUsername());
            log.info("product = {}", findProduct.getName());
            log.info("orderAmount = {}", memberProduct.getOrderAmount());
            tx.commit();
        } catch (Exception e) {
            log.info("error", e);
            tx.rollback();
        }finally {
            em.close();
        }
    }

    @Entity
    @Getter
    @Setter
    static class Member {

        @Id
        @Column(name = "member_id")
        private String id; // id 직접 할당
        private String username;
        //역방향
        @OneToMany(mappedBy = "member")
        private List<MemberProduct> memberProducts;
    }

    @Entity
    @Getter
    @Setter
    static class Product {

        @Id
        @Column(name = "product_id")
        private String id;
        private String name;
    }

    @Entity
    @Getter
    @Setter
    @IdClass(MemberProductId.class)
    static class MemberProduct {

        @Id
        @ManyToOne
        @JoinColumn(name = "member_id")
        private Member member;

        @Id
        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        private int orderAmount;
    }

    /*
    복합 키를 위한 식별자 클래스
    1. 복합 키는 별도의 식별자 클래스로 만들어야 한다.
    2. Serializable을 구현해야 한다.
    3. equals & hashCode 메서드를 구현해야 한다.
    4. 기본 생성자 필수
    5. 식별자 클래스는 public
    6. @IdClass 사용하는 방법 외에 @EmbeddedId를 사용하는 방법도 있다.
     */
    @EqualsAndHashCode
    @Getter @Setter
    static class MemberProductId implements Serializable {

        private String member;
        private String product;

    }
}
