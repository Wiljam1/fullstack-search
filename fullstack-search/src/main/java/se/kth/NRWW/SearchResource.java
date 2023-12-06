package se.kth.NRWW;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.runtime.StartupEvent;
import se.kth.NRWW.model.patientjournal.Condition;
import se.kth.NRWW.model.users.User;
import se.kth.NRWW.repositories.ConditionRepository;
import se.kth.NRWW.repositories.UserRepository;


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

    // Used to search for patients based on name, conditions, username, email. More things can be added.
    @GET
    @Path("searchPatients")
    @Transactional
    public Set<User> search(@RestQuery String pattern,
                            @RestQuery Optional<Integer> size) {
        Set<User> users = new HashSet<>();
        users.addAll(searchPatientsWithCondition(pattern, size));
        users.addAll(searchUsers(pattern, size));
        return users;
    }

    @GET
    @Path("users/search")
    @Transactional
    public List<User> searchUsers(@RestQuery String pattern,
                                  @RestQuery Optional<Integer> size) {

        return searchSession.search(User.class)
                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                        : f.simpleQueryString()
                        .fields("username", "name", "email").matching(pattern))
                .sort(f -> f.field("username_sort"))//.then().field("firstName_sort"))
                .fetchHits(size.orElse(20));
    }

    @GET
    @Path("users/searchWithCondition")
    @Transactional
    public List<User> searchPatientsWithCondition(@RestQuery String pattern,
                                                  @RestQuery Optional<Integer> size) {
        List<Condition> conditions = searchConditions(pattern, size);

        List<Long> patientIds = conditions.stream()
                .map(Condition::getPatientId)
                .toList();

        List<User> usersToReturn = new ArrayList<>();
        for (Long patientId : patientIds) {
            User user = userRepository.findByPatientId(patientId);
            usersToReturn.add(user);
        }

        return usersToReturn;
    }

    @GET
    @Path("conditions/search")
    @Transactional
    public List<Condition> searchConditions(@RestQuery String pattern,
                                            @RestQuery Optional<Integer> size) {
        System.out.println("Searching for conditions...");
        return searchSession.search(Condition.class)
                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                        : f.simpleQueryString()
                        .fields("name").matching(pattern))
                .fetchHits(size.orElse(20));
    }

}
