package se.kth.NRWW.model.users;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String name;
    private String email;

    private String password;

    private UserType type;

    @JoinColumn(name = "staff_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL)
    private Staff staffProfile;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
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
