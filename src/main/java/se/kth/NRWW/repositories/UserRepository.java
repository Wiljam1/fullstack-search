package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import se.kth.NRWW.model.users.User;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import se.kth.NRWW.model.users.User;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Long> {
    public User findByPatientId(Long patientId) {
        return find("patientProfile.id", patientId).firstResult();
    }

    public List<User> searchUsers(String pattern, Optional<Integer> size) {
        return find("username like concat('%', :pattern, '%') or name like concat('%', :pattern, '%') or email like concat('%', :pattern, '%')",
                Parameters.with("pattern", pattern))
                .page(Page.ofSize(size.orElse(20)))
                .list();
    }
}

