package se.kth.NRWW;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.hibernate.search.mapper.orm.session.SearchSession;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Path("/")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class SearchResource {

    @Inject
    SearchSession searchSession;

    @Inject
    ConditionRepository conditionRepository;

    @Inject
    EncounterRepository encounterRepository;

    @Inject
    ObservationRepository observationRepository;

    @Inject
    UserRepository userRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        //if (Book.count() > 0) {
        searchSession.massIndexer()
                .startAndWait();
        //}
    }

    // Endpoint to trigger reindexing
    @GET
    @Path("index")
    @Transactional
    public String reindex() throws InterruptedException {
        searchSession.massIndexer().startAndWait();
        return "Indexed searches..";
    }

    // Reactive search
    // Use all the Uni searches and provide collected results
    @GET
    @Path("searchPatients")
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
    @Transactional
    public Uni<List<User>> searchUsers(@RestQuery String pattern,
                                       @RestQuery Optional<Integer> size) {
        return Uni.createFrom().item(() ->
                searchSession.search(User.class)
                        .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                                : f.simpleQueryString()
                                .fields("username", "name", "email").matching(pattern))
                        .sort(f -> f.field("username_sort"))//.then().field("firstName_sort"))
                        .fetchHits(size.orElse(20))
        );
    }

    @GET
    @Path("users/searchWithCondition")
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
    @Transactional
    public Uni<List<Condition>> searchConditionsReactive(@RestQuery String pattern,
                                                 @RestQuery Optional<Integer> size) {
        return Uni.createFrom().item(() ->
                searchSession.search(Condition.class)
                        .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                                : f.simpleQueryString()
                                .fields("name").matching(pattern))
                        .fetchHits(size.orElse(20))
        );
    }

    @GET
    @Path("searchDoctorPatients")
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
    @Transactional
    public Uni<Set<Encounter>> searchDoctorEncounters(@RestQuery Long doctorId, @RestQuery String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date realDate = sdf.parse(date);

            System.out.println("Date sent to repository: " + realDate);

            return Uni.createFrom().item(() ->
                            encounterRepository.findByPractitionerIdAndDate(doctorId, realDate)
                    //new ArrayList<>(encounterRepository.findByDate(realDate))
            );
        } catch (ParseException e) {
            // Handle the parse exception, log it, and return an appropriate response
            throw new RuntimeException("Error parsing date parameter", e);
        }
    }

//    // Non-reactive search?
//    @GET
//    @Path("searchPatients")
//    @Transactional
//    public Set<User> search(@RestQuery String pattern,
//                            @RestQuery Optional<Integer> size) {
//        Set<User> users = new HashSet<>();
//        users.addAll(searchPatientsWithCondition(pattern, size));
//        users.addAll(searchUsers(pattern, size));
//        return users;
//    }

//    @GET
//    @Path("users/search")
//    @Transactional
//    public List<User> searchUsers(@RestQuery String pattern,
//                                  @RestQuery Optional<Integer> size) {
//
//        return searchSession.search(User.class)
//                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
//                        : f.simpleQueryString()
//                        .fields("username", "name", "email").matching(pattern))
//                .sort(f -> f.field("username_sort"))//.then().field("firstName_sort"))
//                .fetchHits(size.orElse(20));
//    }

//    @GET
//    @Path("users/searchWithCondition")
//    @Transactional
//    public List<User> searchPatientsWithCondition(@RestQuery String pattern,
//                                                  @RestQuery Optional<Integer> size) {
//        List<Condition> conditions = searchConditions(pattern, size);
//
//        List<Long> patientIds = conditions.stream()
//                .map(Condition::getPatientId)
//                .toList();
//
//        List<User> usersToReturn = new ArrayList<>();
//        for (Long patientId : patientIds) {
//            User user = userRepository.findByPatientId(patientId);
//            usersToReturn.add(user);
//        }
//
//        return usersToReturn;
//    }

//    @GET
//    @Path("conditions/search")
//    @Transactional
//    public List<Condition> searchConditions(@RestQuery String pattern,
//                                            @RestQuery Optional<Integer> size) {
//        System.out.println("Searching for conditions...");
//        return searchSession.search(Condition.class)
//                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
//                        : f.simpleQueryString()
//                        .fields("name").matching(pattern))
//                .fetchHits(size.orElse(20));
//    }
}
