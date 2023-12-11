package se.kth.NRWW.model.users;

import jakarta.persistence.*;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import java.util.Iterator;

@Entity
@Indexed
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @FullTextField(analyzer = "name")
    @KeywordField(name = "username_sort", sortable = Sortable.YES, normalizer = "sort")
    private String username;

    @FullTextField(analyzer = "english")
    private String name;

    @FullTextField(analyzer = "name")
    @KeywordField(name = "email_sort", sortable = Sortable.YES, normalizer = "sort")
    private String email;

    private String password;

    private UserType type;

    @JoinColumn(name = "staff_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL)
    @IndexedEmbedded
    private Staff staffProfile;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    @IndexedEmbedded
    private Patient patientProfile;

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Staff getStaffProfile() {
        return staffProfile;
    }

    public void setStaffProfile(Staff staffProfile) {
        this.staffProfile = staffProfile;
    }

    public Patient getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(Patient patientProfile) {
        this.patientProfile = patientProfile;
    }

    public Long getPatientId() {
        return patientProfile != null ? patientProfile.getId() : null;
    }

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

}
