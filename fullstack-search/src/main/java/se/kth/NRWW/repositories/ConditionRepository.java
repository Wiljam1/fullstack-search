package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import se.kth.NRWW.model.patientjournal.Condition;
import se.kth.NRWW.model.patientjournal.Encounter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConditionRepository implements PanacheRepositoryBase<Condition, Long> {

    @Transactional
    public List<Long> findPatientIdsByDoctorId(Long doctorId) {
        return list("doctorId", doctorId)
                .stream()
                .map(Condition::getPatientId)
                .collect(Collectors.toList());
    }

    public List<Condition> searchConditions(String pattern, Optional<Integer> size) {
        return find("name like concat('%', :pattern, '%')",
                Parameters.with("pattern", pattern))
                .page(Page.ofSize(size.orElse(20)))
                .list();
    }

}

