package se.kth.NRWW.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.agroal.DataSource;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.sql.Date;

@Entity
//Indexed
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date birthdate;

    @OneToOne
    @JsonIgnore
    private User user;


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }
}
