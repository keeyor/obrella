package org.opendelos.sync.services.structure;

import java.util.List;
import java.util.Optional;

import org.opendelos.model.calendar.Period;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Department;
import org.opendelos.model.structure.StudyProgram;
import org.opendelos.sync.repository.structure.CourseRepository;
import org.opendelos.sync.repository.structure.DepartmentRepository;
import org.opendelos.sync.repository.structure.InstitutionRepository;
import org.opendelos.sync.repository.structure.StudyProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class StudyProgramService {

    private final Logger logger = LoggerFactory.getLogger(StudyProgramService.class.getName());
    private final StudyProgramRepository studyProgramRepository;
    private final DepartmentRepository departmentRepository;
    private final InstitutionRepository institutionRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public StudyProgramService(StudyProgramRepository studyProgramRepository, DepartmentRepository departmentRepository, InstitutionRepository institutionRepository, CourseRepository courseRepository) {
        this.studyProgramRepository = studyProgramRepository;
        this.departmentRepository = departmentRepository;
        this.institutionRepository = institutionRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudyProgram> findAll() {
        logger.trace("StudyProgram.findAll");
        return studyProgramRepository.findAll();
    }

    public void deleteAll() {
        logger.trace("StudyProgram.deleteAll");
        try {
            studyProgramRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: StudyProgram.deleteAll:" + e.getMessage());
        }
    }
    public String create(StudyProgram studyProgram) {
        String generatedId = null;
        try {
            //set program department +institution +school
            Optional<Department> department = departmentRepository.findById(studyProgram.getDepartmentId());
            if (department.isPresent()) {
                studyProgram.setSchoolId(department.get().getSchoolId());
                studyProgram.setId(null);
                StudyProgram nStudyProgram = studyProgramRepository.save(studyProgram);
                generatedId = nStudyProgram.getId();
                logger.trace(String.format("StudyProgram.create: %s", studyProgram.getTitle()));
            }
        }
        catch (Exception e) {
            logger.error("error: StudyProgram.create:" + e.getMessage());
        }
       return generatedId;
    }

    public StudyProgram findById(String id) {
        logger.trace(String.format("StudyProgram.findById(%s)", id));
        return studyProgramRepository.findById(id).orElse(null);
    }

    public void update(StudyProgram studyProgram) {
        logger.trace(String.format("StudyProgram.update: %s", studyProgram.getTitle()));
        try {
            studyProgramRepository.save(studyProgram);
        }
        catch (Exception e) {
            logger.error("error: StudyProgram.update:" + e.getMessage());
        }
    }


    public void delete(String id) throws Exception {
        //TODO: IMPORTANT !!! (probably) take additional actions when scheduler and calendar are implemented!!!
        //probably not needed since if found in scheduler -> is in course which is checked anyway
        StudyProgram studyProgram = studyProgramRepository.findById(id).orElse(null);
        if (studyProgram!= null) {
            if (courseRepository.findWithCriteria("_all","_all","_all",id).size() == 0) {
                    studyProgramRepository.deleteById(id);
            }
            else {
                throw new Exception("_FORBIDDEN_COURSES");
            }
        }
        else {
            throw new Exception("_NOT_FOUND");
        }
    }

    public StudyProgram findByIdentity(String identity) {
        logger.trace(String.format("StudyProgram.findByIdentity(%s)", identity));
        return studyProgramRepository.findByIdentity(identity);
    }

    public List<StudyProgram> findByDepartmentId(String departmentId) {
        return studyProgramRepository.findByDepartmentId(departmentId);
    }

    public List<StudyProgram> findByDepartmentIdentity(String departmentIdentity) {
        Department department = departmentRepository.findByIdentity(departmentIdentity);
        return studyProgramRepository.findByDepartmentId(department.getId());
    }

    public List<StudyProgram> findWithCriteria(String schoolId, String departmentId, String study) {
        return studyProgramRepository.findWithCriteria(schoolId,departmentId,study);
    }

    public void findAndUpdate(StudyProgram studyProgram) {
        //set program department +institution +school
        Optional<Department> department = departmentRepository.findById(studyProgram.getDepartmentId());
        if (department.isPresent()) {
            studyProgram.setSchoolId(department.get().getSchoolId());
            studyProgramRepository.findAndUpdate(studyProgram);
        }
    }

    /* Calendar  */

    public void saveCustomPeriod(String id, CustomPeriod customPeriod){
        studyProgramRepository.saveCustomPeriod(id,customPeriod);
    }

    public void deleteCustomPeriod(String id, String year) {
        studyProgramRepository.deleteCustomPeriod(id,year);
    }
    public List<CustomPeriod> getCustomPeriods(String id) {
        return  studyProgramRepository.getCustomPeriods(id);
    }
    public CustomPeriod getCustomPeriod(String id, String year){
        return  studyProgramRepository.getCustomPeriod(id,year);
    }
    public Period getStudyPeriod(String id, String did, String iid, String year, String period){
        CustomPeriod studyPeriods = studyProgramRepository.getCustomPeriod(id,year);
        if (studyPeriods == null) {
            studyPeriods = departmentRepository.getCustomPeriod(did,year);
            if (studyPeriods == null) {
                studyPeriods = institutionRepository.getCustomPeriod(iid,year);
            }
            studyPeriods.setInherited(true);
        }
        else {
            studyPeriods.setInherited(false);
        }
        for (Period study_period: studyPeriods.getPeriods().getPeriod()) {
            if (study_period.getName().equals(period)) {
                return study_period;
            }
        }
        return null;
    }
}
