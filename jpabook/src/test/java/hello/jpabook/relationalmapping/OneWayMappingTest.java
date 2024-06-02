package hello.jpabook.relationalmapping;

import hello.jpabook.Member;
import hello.jpabook.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class OneWayMappingTest {

    @Autowired
    EntityManagerFactory emf;

    @AfterEach
    void close() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            String jpql = "delete from Member  m";
            Query query = em.createQuery(jpql);
            int result = query.executeUpdate();
            log.info("close() result ={}", result);

            jpql = "delete from Team t";
            query = em.createQuery(jpql);
            result = query.executeUpdate();
            log.info("close() result ={}", result);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }finally {
            em.close();
        }
    }

    @Test
    @DisplayName("다대일 연관관계")
    void mapping() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Team team1 = getTeam("team1", "팀1");
            em.persist(team1);
            Member member1 = getMember("member1", "회원1", 10);
            Member member2 = getMember("member2", "회원2", 20);
            member1.setTeam(team1);
            member2.setTeam(team1);
            em.persist(member1);
            em.persist(member2);
            
            Member findMember = em.find(Member.class, "member1"); // 1차 캐시 조회
            Team findMemberTeam = findMember.getTeam(); // 객체 그래프 탐색
            log.info("findMemberTeam.teamId={}", findMemberTeam.getId());
            tx.commit();
        } catch (Exception e) {
            log.info("error", e);
            tx.rollback();
        }finally {
            em.close();
        }
    }

    @Test
    @DisplayName("jpql - join()")
    void queryLogicJoin() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Team team1 = getTeam("team1", "팀1");
            em.persist(team1);
            Member member1 = getMember("member1", "회원1", 10);
            Member member2 = getMember("member2", "회원2", 20);
            member1.setTeam(team1);
            member2.setTeam(team1);
            em.persist(member1);
            em.persist(member2);
            String jpql = "select m from Member m join m.team t where t.name =:teamName";

            List<Member> resultList = em.createQuery(jpql, Member.class)
                .setParameter("teamName", "팀1")
                .getResultList();

            for (Member member : resultList) {
                log.info("[query] member.username={}", member.getUsername());
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }finally {
            em.close();
        }
    }

    @Test
    void update() {
        Team team1 = getTeam("team1", "팀1");
        Team team2 = getTeam("team2", "팀2");

        Member member = getMember("member1", "회원1", 10);
        member.setTeam(team1);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            em.persist(team1);
            em.persist(team2);
            em.persist(member);

            member = em.find(Member.class, "member1");
            member.setTeam(team2);

            tx.commit();
        } catch (Exception e) {
            log.info("error", e);
            tx.rollback();
        }finally {
            em.close();
        }
    }

    @Test
    void delete() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Team team = getTeam("team1", "팀1");
            Member member = getMember("member1", "회원1", 10);
            em.persist(team);
            member.setTeam(team);
            em.persist(member);
            member.setTeam(null);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }finally {
            em.close();
        }
    }
    private Team getTeam(String id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setName(name);
        return team;
    }
    private Member getMember(String id, String username, Integer age) {
        Member member = new Member();
        member.setId(id);
        member.setUsername(username);
        member.setAge(age);
        return member;
    }
}
