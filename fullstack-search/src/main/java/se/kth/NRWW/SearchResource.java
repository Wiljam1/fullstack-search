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
import se.kth.NRWW.model.users.User;
import se.kth.NRWW.repositories.ConditionRepository;
import se.kth.NRWW.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
