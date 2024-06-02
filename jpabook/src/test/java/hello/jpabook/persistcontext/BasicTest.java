package hello.jpabook.persistcontext;

import hello.jpabook.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
//@Transactional
public class BasicTest {

    @Autowired
    EntityManagerFactory emf;

    @AfterEach
    void close() {
        String jpql = "delete from Member m";
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Query query = em.createQuery(jpql);
            int result = query.executeUpdate();
            log.info("close() = {}", result);
            tx.commit();
        } catch (Exception e) {
            log.info("error", e);
            tx.rollback();
        } finally {
            em.close();
        }

    }

    @Test
    void setup() {
        log.info("emf={}", emf.getClass());
    }

    @DisplayName("1차 캐시에서 조회")
    @Test
    void find() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            log.info("em={}", em);
            Member member = getMember("member1", "회원1", 12);
            // 1차 캐시에 저장됨
            em.persist(member);
            // 1차 캐시에서 조회 -> 쿼리가 안날라가네
            Member findMember = em.find(Member.class, "member1");
            // 동일성 만족
            Assertions.assertThat(findMember).isSameAs(member);
//            findMember.setAge(100); 영속석 컨텍스트에서 변경 사황을 체크 후 update 쿼리 날라간다.
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }

    @DisplayName("데이터베이스에서 조회")
    @Test
    void find2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Member member = getMember("member1", "회원1", 12);
        // 1차 캐시에 저장
        em.persist(member);
        // DB에 flush -> 쓰기 지연 sql 저장소에서 insert 나간다.
        em.flush();
        // 준영속 : member 객체를 더 이상 영속성 컨텍스트에서 관리하지 않는다.
        em.detach(member);
        // 1차 캐시에서 조회 불가능 -> 데이터베이스에서 조회 : select 쿼리 확인
        Member findMember = em.find(Member.class, "member1");
//        findMember.setAge(100); 영속성 컨텍스트에서 관리하지 않기 때문에 update 쿼리 날라가지 않는다.
        tx.commit();
    }

    /**
     * 쓰기 지연이 가능한 이유 1. 트랜잭션 범위 안에서 실행되므로 커밋 전에만 데이터베이스에 sql에 전달하면 된다.
     */
    @DisplayName("엔티티 등록 - 쓰기 지연")
    @Test
    void write() {
        EntityManager em = emf.createEntityManager();
        log.info("em={}", em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        // == 비영속 ==
        Member member1 = getMember("member1", "회원1", 10);
        Member member2 = getMember("member2", "회원2", 20);

        // == 영속, 1차 캐시 저장, 스냅샷 ==
        em.persist(member1);
        em.persist(member2);
        // == 쓰기 지연 sql 저장소에서 쿼리 찾아서 데이터베이스 나간다. ==
        tx.commit();

    }

    /**
     * 엔티티의 모든 필드를 업데이트 한다. 단점 : 데이터 전송량 증가 장점: 모든 필드를 사용하면 해당 엔티티의 수정 쿼리가 모두 동일. 따라서 애플리케이션 로딩 시점에서
     * 수정 쿼리를 미리 생성해두고 재사용 가능. 데이터베이스에 동일한 쿼리를 보내면 데이터베이스는 이전에 한 번 파싱된 쿼리를 재사용
     * <p>
     * 참고: @DynamicUpdate : 동적 update sql 생성
     */
    @DisplayName("엔티티 수정")
    @Test
    void update() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Member member = getMember("member1", "회원1", 12);

        // 1차 캐시 스냅샷이 생성된다.(최초 상태 저장)
        em.persist(member);
        member.setAge(20);
        // flush 시점에 스냇샵과 entitiy(member)를 비교해 변경이 있다면 update 쿼리를 쓰기 지연 sql 저장소에 보낸다.
        // 쓰기 지연 sql 저장소의 sql을 데이터베이스로 보낸다.
        tx.commit();
    }


    @DisplayName("엔티티 삭제")
    @Test
    void delete() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Member member = getMember("member1", "회원1", 10);

        em.persist(member);
        em.remove(member);
        // 쓰기 지연 sql 저장소에서 데이터베이스에 쿼리 보낸다.
        tx.commit();
    }

    /**
     * JPQL 쿼리가 날라갈 때  flush()가 되야하는 이유
     * <p>
     * flush() -> 영속성 컨텍스트를 지우는 것이 아니다.
     */
    @DisplayName("flush() - JPQL")
    @Test
    void flush() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Member member1 = getMember("member1", "회원1", 1);
        Member member2 = getMember("member2", "회원2", 1);

        // 1차 캐시에 저장된다.
        em.persist(member1);
        em.persist(member2);

        // JPQL -> SQL -> 데이터베이스에 보낸다. (1차 캐시에 들어 있는 애들을 알 수 없다.)
        // 따라서 flush() 해줘야 하기 때문에 해준다.
        Query query = em.createQuery("select m from Member m");
        query.getResultList().stream().forEach(System.out::println);

        // 1차 캐쉬에서 조회한다.(예상) -> 쿼리 안 날린다.
        em.find(Member.class, "member1");
        tx.commit();
    }

    @DisplayName("detach() - 준영속")
    @Test
    void detach() {

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        Member member1 = getMember("member1", "회원1", 10);
        Member member2 = getMember("member2", "회원2", 20);

        tx.begin();
        em.persist(member1);
        em.persist(member2);
        em.flush(); // flush() 있어야 한다. 일단은 -> persist() -> detach()하면 하이버네이트 버그가 발생하는 듯,,, 밑에 내용 참고
        member1.setAge(100);
        em.detach(member1);
        /*
        I don’t know why you’re detaching an entity right after you pass it to persist as that will not make the entity persistent.
        You have to flush first.
        Either way, I’d say this is a bug. The detach operation should remove entity actions involving the entity.
         */
        tx.commit();
        em.close();

    }

    private Member getMember(String id, String username, Integer age) {
        Member member = new Member();
        member.setId(id);
        member.setUsername(username);
        member.setAge(age);
        return member;
    }
}
