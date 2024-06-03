package hello.jpabook.relationalmapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * N : N -> 1:N , N : 1로 해결 연결 테이블에 추가적인 컬럼(orderAmount) 복합키 대신 새로운 기본 키 사용(member_id, product_id는
 * 외래 키로 활용) 대리 키 사용 비식별 관계 : 받아온 식별자는 외래 키로만 사용하고 새로운 식별자를 추가한다. 객체 입장에서 보면 비식별 관계를 사용하는 것이 복합 키를
 * 위한 식별자 클래스를 만들지 않아도 되므로 단순하고 편리하게 ORM 매핑을 할 수 있다.
 */
@SpringBootTest
@Slf4j
public class ManyToManyTest3 {

    @Autowired
    EntityManagerFactory emf;

    @Test
    void saveAndFind() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            // member save
            Member member1 = new Member();
            member1.setId("member1");
            member1.setUsername("회원1");
            em.persist(member1);

            // product save
            Product productA = new Product();
            productA.setId("productA");
            productA.setName("상품1");
            em.persist(productA);

            // order save
            Order order = new Order();
            order.setMember(member1);
            order.setProduct(productA);
            order.setOrderAmount(2);
            em.persist(order); // @GeneratedValue : SEQUENCE : seq_id를 조회해서 가져온다.

            em.flush();
            em.clear();
            // find
            Long orderId = 1L;
            Order findOrder = em.find(Order.class, orderId);
            Member findMember = findOrder.getMember();
            Product findProduct = findOrder.getProduct();
            log.info("member = {}", findMember.getUsername());
            log.info("product = {}", findProduct.getName());
            log.info("orderAmount = {}", findOrder.getOrderAmount());

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
    static class Order {

        @Id
        @GeneratedValue
        @Column(name = "order_id")
        private Long id;

        @ManyToOne
        @JoinColumn(name = "member_id")
        private Member member;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        private int orderAmount;
    }

    @Entity
    @Getter
    @Setter
    static class Member {

        @Id
        @Column(name = "member_id")
        private String id;
        private String username;

        @OneToMany(mappedBy = "member")
        private List<Order> orders = new ArrayList<Order>();
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
}
