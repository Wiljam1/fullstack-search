package se.kth.NRWW;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.runtime.StartupEvent;
import se.kth.NRWW.model.User;

@Path("/")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class SearchResource {

    @Inject
    SearchSession searchSession;

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        //if (Book.count() > 0) {
            searchSession.massIndexer()
                    .startAndWait();
        //}
    }

    @GET
    @Path("users/search")
    @Transactional
    public List<User> searchUsers(@RestQuery String pattern,
                                  @RestQuery Optional<Integer> size) {
        System.out.println("Searching for users...");
        return searchSession.search(User.class)
                .where(f -> pattern == null || pattern.trim().isEmpty() ? f.matchAll()
                        : f.simpleQueryString()
                        .fields("username", "name", "email").matching(pattern))
                .sort(f -> f.field("username_sort"))//.then().field("firstName_sort"))
                .fetchHits(size.orElse(20));
    }
}
