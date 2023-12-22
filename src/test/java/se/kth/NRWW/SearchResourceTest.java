package se.kth.NRWW;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.kth.NRWW.model.patientjournal.Condition;
import se.kth.NRWW.model.patientjournal.Encounter;
import se.kth.NRWW.model.users.User;
import se.kth.NRWW.repositories.ConditionRepository;
import se.kth.NRWW.repositories.EncounterRepository;
import se.kth.NRWW.repositories.ObservationRepository;
import se.kth.NRWW.repositories.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
class SearchResourceTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    ConditionRepository conditionRepository;

    @InjectMock
    EncounterRepository encounterRepository;

    @InjectMock
    ObservationRepository observationRepository;

    @Test
    public void searchUsers() {
        String pattern = "test";
        Optional<Integer> size = Optional.of(10);
        List<User> mockUsers = Collections.singletonList(new User(/* set properties */));
        Mockito.when(userRepository.searchUsers(pattern, size)).thenReturn(mockUsers);

        SearchResource resource = new SearchResource();
        resource.userRepository = userRepository;

        Uni<List<User>> result = resource.searchUsers(pattern, size);

        result.subscribe().with(users -> {
            assertEquals(mockUsers.size(), users.size());
        });
    }

    @Test
    void searchPatientsWithCondition() {
        String pattern = "test";
        Optional<Integer> size = Optional.of(10);

        Condition condition1 = new Condition();
        condition1.setPatientId(1L);

        Condition condition2 = new Condition();
        condition2.setPatientId(2L);

        List<Condition> mockConditions = Arrays.asList(condition1, condition2);

        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        List<User> mockUsers = Arrays.asList(user1, user2);

        // Mocking repository calls
        Mockito.when(conditionRepository.searchConditions(anyString(), any())).thenReturn(mockConditions);
        Mockito.when(userRepository.findByPatientId(1L)).thenReturn(user1);
        Mockito.when(userRepository.findByPatientId(2L)).thenReturn(user2);

        SearchResource resource = new SearchResource();
        resource.conditionRepository = conditionRepository;
        resource.userRepository = userRepository;

        Uni<List<User>> result = resource.searchPatientsWithCondition(pattern, size);

        result.subscribe().with(users -> {
            assertEquals(mockUsers.size(), users.size());
            assertEquals(mockUsers.get(0).getId(), users.get(0).getId());
            assertEquals(mockUsers.get(1).getId(), users.get(1).getId());
        });
    }

    @Test
    void searchConditionsReactive() {
        String pattern = "test";
        Optional<Integer> size = Optional.of(10);
        Condition mockCondition1 = new Condition();
        mockCondition1.setId(1L);
        mockCondition1.setName("Condition 1");
        mockCondition1.setPatientId(101L);

        Condition mockCondition2 = new Condition();
        mockCondition2.setId(2L);
        mockCondition2.setName("Condition 2");
        mockCondition2.setPatientId(102L);

        List<Condition> mockConditions = Arrays.asList(mockCondition1, mockCondition2);

        // Mocking repository call
        Mockito.when(conditionRepository.searchConditions(anyString(), any())).thenReturn(mockConditions);

        SearchResource resource = new SearchResource();
        resource.conditionRepository = conditionRepository;

        Uni<List<Condition>> result = resource.searchConditionsReactive(pattern, size);

        result.subscribe().with(conditions -> {
            assertEquals(mockConditions.size(), conditions.size());
            assertEquals(mockConditions.get(0).getId(), conditions.get(0).getId());
            assertEquals(mockConditions.get(1).getId(), conditions.get(1).getId());
        });
    }

    @Test
    void searchPatients() {
        Long doctorId = 123L;
        List<Long> mockUserIds = new ArrayList<>(Arrays.asList(1L, 2L));
        User mockUser1 = new User();
        mockUser1.setId(1L);
        User mockUser2 = new User();
        mockUser2.setId(2L);
        Set<User> mockUsers = new HashSet<>(Arrays.asList(mockUser1, mockUser2));

        // Mocking repository calls
        Mockito.when(conditionRepository.findPatientIdsByDoctorId(doctorId)).thenReturn(mockUserIds);
        Mockito.when(userRepository.findByPatientId(1L)).thenReturn(mockUser1);
        Mockito.when(userRepository.findByPatientId(2L)).thenReturn(mockUser2);

        SearchResource resource = new SearchResource();
        resource.userRepository = userRepository;
        resource.conditionRepository = conditionRepository;
        resource.observationRepository = observationRepository;
        resource.encounterRepository = encounterRepository;

        Uni<Set<User>> result = resource.searchDoctorPatients(doctorId);

        result.subscribe().with(users -> {
            assertEquals(mockUsers.size(), users.size());
        });
    }


    @Test
    void searchDoctorEncounters() throws ParseException {
        Long doctorId = 456L;
        String dateStr = "2023-01-01";
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        Encounter mockEncounter1 = new Encounter();
        mockEncounter1.setId(1L);
        Encounter mockEncounter2 = new Encounter();
        mockEncounter2.setId(2L);

        Set<Encounter> mockEncounters = new HashSet<>(Arrays.asList(mockEncounter1, mockEncounter2));

        // Mocking repository calls
        Mockito.when(encounterRepository.findByPractitionerIdAndDate(anyLong(), any())).thenReturn(mockEncounters);
        SearchResource resource = new SearchResource();
        resource.encounterRepository = encounterRepository;

        Uni<Set<Encounter>> result = resource.searchDoctorEncounters(doctorId, dateStr);

        result.subscribe().with(encounters -> {
            assertEquals(mockEncounters.size(), encounters.size());
        });
    }
}