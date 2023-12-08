package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import se.kth.NRWW.model.patientjournal.Encounter;
import se.kth.NRWW.model.patientjournal.Observation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class EncounterRepository implements PanacheRepositoryBase<Encounter, Long> {

    @Transactional
    public Set<Long> findPatientIdsByPractitionerId(Long practitionerId) {
        List<Long> patients = list("practitionerId", practitionerId)
                .stream()
                .map(Encounter::getPatientId)
                .toList();
        return new HashSet<>(patients);
    }

    @Transactional
    public Set<Encounter> findByPractitionerIdAndDate(Long practitionerId, Date date) {
        try {
            List<Encounter> encounters = list("practitionerId = :practitionerId and date = :date",
                    Parameters.with("practitionerId", practitionerId)
                            .and("date", date));

            System.out.println("Encounters found: " + encounters.get(0));

            // Format date back to YYYY-MM-DD
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            encounters.forEach(encounter -> {
                LocalDate localDate = encounter.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String formattedDate = localDate.format(formatter);
                encounter.setDate(java.sql.Date.valueOf(formattedDate));
            });

            return new HashSet<>(encounters);
        } catch (Exception e) {
            //throw new RuntimeException("Error while fetching encounters", e);
            return new HashSet<>();
        }
    }

    @Transactional
    public List<Encounter> findByDate(Date date) {
        try {
            return list("date = ?1", date);
        } catch (Exception e) {
            // Log the exception or handle it accordingly
            throw new RuntimeException("Error while fetching encounters by date", e);
        }
    }

}
