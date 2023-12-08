package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import se.kth.NRWW.model.patientjournal.Observation;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ObservationRepository implements PanacheRepositoryBase<Observation, Long> {

    @Transactional
    public List<Long> findPatientIdsByPerformerId(Long performerId) {
        return list("performerId", performerId)
                .stream()
                .map(Observation::getPatientId)
                .collect(Collectors.toList());
    }

}
