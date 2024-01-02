package se.kth.NRWW;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.jboss.resteasy.reactive.RestQuery;
import se.kth.NRWW.model.patientjournal.Condition;
import se.kth.NRWW.model.patientjournal.Encounter;
import se.kth.NRWW.model.users.User;
import se.kth.NRWW.repositories.ConditionRepository;
import se.kth.NRWW.repositories.EncounterRepository;
import se.kth.NRWW.repositories.ObservationRepository;
import se.kth.NRWW.repositories.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Path("/")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class SearchResource {

    @Inject
    ConditionRepository conditionRepository;

    @Inject
    EncounterRepository encounterRepository;

    @Inject
    ObservationRepository observationRepository;

    @Inject
    UserRepository userRepository;

    // Reactive search
    // Use all the Uni searches and provide collected results
    @GET
    @Path("searchPatients")
    @RolesAllowed("doctor")
    @Transactional
    public Multi<User> search(@RestQuery String pattern,
                              @RestQuery Optional<Integer> size) {
        Uni<List<User>> usersUni1 = searchUsers(pattern, size);
        Uni<List<User>> usersUni2 = searchPatientsWithCondition(pattern, size);

        return Multi.createBy().merging()
                .streams(usersUni1.onItem().transformToMulti(Multi.createFrom()::iterable),
                        usersUni2.onItem().transformToMulti(Multi.createFrom()::iterable));
    }

    @GET
    @Path("users/search")
    @RolesAllowed("doctor")
    @Transactional
    public Uni<List<User>> searchUsers(@RestQuery String pattern,
                                       @RestQuery Optional<Integer> size) {
        return Uni.createFrom().item(() ->
                userRepository.searchUsers(pattern, size)
        );
    }

    @GET
    @Path("users/searchWithCondition")
    @RolesAllowed("doctor")
    @Transactional
    public Uni<List<User>> searchPatientsWithCondition(@RestQuery String pattern,
                                                       @RestQuery Optional<Integer> size) {
        return searchConditionsReactive(pattern, size)
                .onItem().transformToUni(conditions -> {
                    List<Long> patientIds = conditions.stream()
                            .map(Condition::getPatientId)
                            .toList();

                    List<User> usersToReturn = new ArrayList<>();
                    for (Long patientId : patientIds) {
                        User user = userRepository.findByPatientId(patientId);
                        usersToReturn.add(user);
                    }

                    return Uni.createFrom().item(usersToReturn);
                });
    }

    @GET
    @Path("conditions/searchReactive")
    @RolesAllowed("doctor")
    @Transactional
    public Uni<List<Condition>> searchConditionsReactive(@RestQuery String pattern,
                                                 @RestQuery Optional<Integer> size) {
        return Uni.createFrom().item(() ->
                conditionRepository.searchConditions(pattern, Optional.of(size.orElse(10)))
        );
    }

    @GET
    @Path("searchDoctorPatients")
    @RolesAllowed("doctor")
    @Transactional
    public Uni<Set<User>> searchDoctorPatients(@RestQuery Long doctorId) {
        Set<Long> userIds = new HashSet<>();

        // TODO: Fix so this is non-blocking (Return Uni everywhere)
        userIds.addAll(conditionRepository.findPatientIdsByDoctorId(doctorId));
        userIds.addAll(observationRepository.findPatientIdsByPerformerId(doctorId));
        userIds.addAll(encounterRepository.findPatientIdsByPractitionerId(doctorId));

        Set<User> userResults = new HashSet<>();
        for(Long id : userIds) {
            userResults.add(userRepository.findByPatientId(id));
        }

        return Uni.createFrom().item(() ->
                userResults
        );
    }

    @GET
    @Path("searchDoctorEncounters")
    @RolesAllowed("doctor")
    @Transactional
    public Uni<Set<Encounter>> searchDoctorEncounters(@RestQuery Long doctorId, @RestQuery String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date realDate = sdf.parse(date);

            System.out.println("Date sent to repository: " + realDate);

            return Uni.createFrom().item(() ->
                            encounterRepository.findByPractitionerIdAndDate(doctorId, realDate)
            );
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing date parameter", e);
        }
    }
}
