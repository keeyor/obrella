package org.opendelos.vodapp.services.structure;

import java.util.List;

import org.opendelos.model.structure.School;
import org.opendelos.vodapp.repository.structure.DepartmentRepository;
import org.opendelos.vodapp.repository.structure.SchoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
////@CacheConfig(cacheNames = "schools")
public class SchoolService {

    private final Logger logger = LoggerFactory.getLogger(SchoolService.class.getName());
    private final SchoolRepository schoolRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public SchoolService(SchoolRepository schoolRepository, DepartmentRepository departmentRepository) {
        this.schoolRepository = schoolRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<School> findAllSortedByTitle() {
        logger.trace("School.findAll");
       // Sort sort = Sort.by(Sort.Order.asc("Title"));
        return schoolRepository.findAll();
    }
  //  //@CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("School.deleteAll");
        try {
            schoolRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: School.deleteAll:" + e.getMessage());
        }
    }
   // //@CacheEvict(allEntries = true)
    public String create(School school) throws Exception {
        
        String generatedId;
        school.setIdentity(school.getIdentity().toLowerCase()); //force lowercase
        if (schoolRepository.findByIdentity(school.getIdentity()) == null) {
                    school.setId(null);
                    School nSchool = schoolRepository.save(school);
                    generatedId = nSchool.getId();
                    logger.trace(String.format("School.create: %s", school.getTitle()));
        }
        else {
            throw new Exception("_DUPLICATE_IDENTITY");
        }
        return generatedId;
    }
  //  //@Cacheable
    public School findById(String id) {
        logger.trace(String.format("School.findById(%s)", id));
        return schoolRepository.findById(id).orElse(null);
    }
 //   @CachePut(key = "#school.id")
    public void update(School school) {
        logger.trace(String.format("School.update: %s", school.getTitle()));
        try {
            schoolRepository.save(school);
        }
        catch (Exception e) {
            logger.error("error: School.update:" + e.getMessage());
        }
    }
 //   //@CacheEvict(key = "#id")
    public void delete(String id) throws Exception {
        logger.trace(String.format("School.delete: %s", id));

        School school = schoolRepository.findById(id).orElse(null);
        if (school!= null) {
            if (departmentRepository.findAllBySchoolId(id).size() == 0) {
                schoolRepository.deleteById(id);
            }
            else {
                throw new Exception("_FORBIDDEN");
            }
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    public School findByIdentity(String identity) {
        logger.trace(String.format("School.findByIdentity(%s)", identity));
        return schoolRepository.findByIdentity(identity);
    }

}
