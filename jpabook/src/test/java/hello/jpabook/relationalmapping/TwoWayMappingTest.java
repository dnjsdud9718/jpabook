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
public class TwoWayMappingTest {

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
        } finally {
            em.close();
        }
    }

    @DisplayName("양방향 연관관계 - 조회(편의 메서드 적용)")
    @Test
    void search() {
        Team team = getTeam("team1", "팀1");
        Member member1 = getMember("member1", "회원1", 10);
        Member member2 = getMember("member2", "회원2", 20);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            em.persist(team);
            member1.setTeam(team);
            member2.setTeam(team);
            em.persist(member1);
            em.persist(member2);
//            em.flush();
            Team findTeam = em.find(Team.class, "team1");
            List<Member> members = findTeam.getMembers();
            for (Member member : members) {
                log.info("member.getUsername = {} ", member.getUsername());
            }
            tx.commit();
        } catch (Exception e) {
            log.info("error", e);
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
