package se.kth.iv1201.recruitmentsystem.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.kth.iv1201.recruitmentsystem.domain.*;
import se.kth.iv1201.recruitmentsystem.repository.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * This class defines tasks that can be performed by the domain layer.
 *
 * Each method in this class corresponds to a transaction. The transaction starts
 * when the presentation layer calls a method and ends when a method returns.
 * The transaction ends with a commit if everything is well, or with a rollback
 * if an exception was thrown.
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class ApplicationService {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CompetenceRepository competenceRepository;
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    private CompetenceProfileRepository competenceProfileRepository;

    /**
     * Retrieve the logged in user from the database and
     * casts exceptions depending on null email and or ssn.
     * @param username of logged in user.
     * @throws UserException to inform 'controller' that field does not exist.
     */
    public void findPerson(String username) throws UserException{
        PersonDTO person = personRepository.findPersonByUsername(username);
        try{
            person.getEmail().length(); //nullpointer trigger
        }catch(NullPointerException e){
            try{
                person.getSsn().length(); //nullpointer trigger
            }catch(NullPointerException es){
                throw new UserException("missingEmailAndSsn");
            }

            throw new UserException("missingEmail");
        }
        try{
            person.getEmail().length(); //nullpointer trigger
        }catch(NullPointerException e){
            throw new UserException("missingSsn");
        }
    }

    public List<Competence> findCompetences(){
        return competenceRepository.findAll();
    }

    public void createApplication(String chosenCompetence, String fromDate, String toDate, String yearsOfExperience, String username) throws ParseException, UserException, ApplicationException {
        Person person = personRepository.findPersonByUsername(username);
        if(person == null) {
            throw new UserException("Failed to retrieve person from database.");
        }
        Competence competence = competenceRepository.findCompetenceByName(chosenCompetence);
        if(competence == null){
            throw new ApplicationException("Failed to retrieve competence from database.");
        }
        Date from_date = convertToDate(fromDate);
        Date to_date = convertToDate(toDate);
        BigDecimal years_of_experience = new BigDecimal(yearsOfExperience);
        try {
            competenceProfileRepository.save(new CompetenceProfile(person, competence, years_of_experience));
            availabilityRepository.save(new Availability(person, from_date, to_date));
        }catch(Exception e){
            throw new ApplicationException("Something went wrong while inserting application into database");
        }
    }

    private Date convertToDate(String startingDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date convertedDate = sdf.parse(startingDate);
        return new java.sql.Date(convertedDate.getTime());
    }

    /**
     * Attempts to create a person with specified i18n.
     * @param name The name of the person.
     * @param surname The surname of the person.
     * @param ssn The social security number of the person.
     * @param email The email of the person.
     * @param password The password of the person.
     * @param roleName The name of the role of the person.
     * @param username The username of the person.
     * @return The DTO corresponding to the created person
     * @throws UserException Thrown if Person could not be created.
     */
    public PersonDTO createPerson(String name, String surname, String ssn, String email,
                                  String password, String roleName, String username) throws UserException {

        Role role = roleRepository.findRoleByName(roleName);
        if(role == null)
            throw new UserException("Role name " + roleName + " does not exist in database.");

        if(personRepository.findPersonByUsername(username) != null)
            throw new UserException("Username " + username + " is already taken.");

        if(personRepository.findPersonByEmail(email) != null)
            throw new UserException("Email " + email + " is already in use.");

        if(personRepository.findPersonBySsn(ssn) != null)
            throw new UserException("SSN " + ssn + " is already registered.");

        Person person = new Person(name, surname, ssn, email, password, role, username);
        personRepository.save(person);
        return person;
    }
}
