package hello.jpabook.relationalmapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ManyToManyTest {

    @Autowired
    EntityManagerFactory emf;

    @Test
    void save() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Product productA = new Product();
            productA.setId("productA");
            productA.setName("상품A");
            em.persist(productA);

            Member member1 = new Member();
            member1.setId("member1");
            member1.setUsername("회원1");
//            member1.getProducts().add(productA);
            member1.addProduct(productA);
            em.persist(member1);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
    }

    @Test
    void find() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Product productA = new Product();
            productA.setId("productA");
            productA.setName("상품A");
            em.persist(productA);
            Product productB = new Product();
            productB.setId("productB");
            productB.setName("상품B");
            em.persist(productB);

            Member member1 = new Member();
            member1.setId("member1");
            member1.setUsername("회원1");
//            member1.getProducts().add(productA);
            member1.addProduct(productA);
            member1.addProduct(productB);
            em.persist(member1);

            em.flush();
            em.clear(); // select query 확인하기 위해서

            Member findMember = em.find(Member.class, "member1");
            List<Product> products = findMember.getProducts();
            // 지연 로딩이 적용된다.
            products.forEach(product -> System.out.println("product = " + product.getName()));

            // inverse
//            Product product = em.find(Product.class, "productA");
//            List<Member> members = product.getMembers();
//            members.forEach(
//                member -> System.out.println("member.getUsername() = " + member.getUsername()));

            tx.commit();
        } catch (Exception e) {
            System.out.println("e = " + e);
            tx.rollback();
        } finally {
            em.close();
        }
    }


    @Entity
    @Getter
    @Setter
    static
    class Member {

        @Id
        @Column(name = "member_id")
        private String id;

        private String username;

        @ManyToMany
        @JoinTable(name = "member_product",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
        private List<Product> products = new ArrayList<>();

        public Member() {}

        // 연관관계 편의 메서드
        public void addProduct(Product product) {
            products.add(product);
            product.getMembers().add(this);
        }
    }

    @Entity
    @Getter
    @Setter
    static class Product {

        @Id
        @Column(name = "product_id")
        private String id;

        private String name;

        @ManyToMany(mappedBy = "products") // 양방향
        private List<Member> members = new ArrayList<>();

        public Product() {}
    }

}
