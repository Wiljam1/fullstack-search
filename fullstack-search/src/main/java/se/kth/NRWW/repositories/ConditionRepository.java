package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import se.kth.NRWW.model.patientjournal.Condition;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConditionRepository implements PanacheRepositoryBase<Condition, Long> {

    @Inject
    EntityManager entityManager;

    @Transactional
    public List<Condition> searchConditions(String pattern, Optional<Integer> size) {
        System.out.println("Searching for conditions...");

        SearchSession searchSession = Search.session(entityManager);

        return searchSession.search(Condition.class)
                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                        : f.simpleQueryString()
                        .fields("name").matching(pattern))
                .fetchHits(size.orElse(20));
    }
}

