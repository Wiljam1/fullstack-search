package se.kth.NRWW.model.patientjournal;

import io.quarkus.agroal.DataSource;
import jakarta.persistence.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.util.Date;

@Entity
@Indexed
@Table(name = "encounters")
public class Encounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long practitionerId;

    private Long patientId;

    private Date date;
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(Long practitionerId) {
        this.practitionerId = practitionerId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Encounter{" +
                "id=" + id +
                ", practitionerId=" + practitionerId +
                ", patientId=" + patientId +
                ", date=" + date +
                ", location='" + location + '\'' +
                '}';
    }
}
