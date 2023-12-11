package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import se.kth.NRWW.model.users.User;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import se.kth.NRWW.model.users.User;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Long> {
    public User findByPatientId(Long patientId) {
        return find("patientProfile.id", patientId).firstResult();
    }
}

