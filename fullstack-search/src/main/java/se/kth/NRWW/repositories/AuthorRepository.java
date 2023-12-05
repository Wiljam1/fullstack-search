package se.kth.NRWW.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;
import se.kth.NRWW.model.Author;

import java.util.List;

@ApplicationScoped
public class AuthorRepository implements PanacheRepository<Author> {

    // put your custom logic here as instance methods

    public Author findByName(String name){
        return find("name", name).firstResult();
    }

    public void deleteStefs(){
        delete("name", "Stef");
    }
}
