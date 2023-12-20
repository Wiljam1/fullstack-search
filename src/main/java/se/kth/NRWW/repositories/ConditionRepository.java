package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import se.kth.NRWW.model.patientjournal.Condition;

import java.util.List;
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

}

