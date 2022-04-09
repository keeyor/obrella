/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.liveapp.repository.structure.extension.impl;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.opendelos.liveapp.repository.structure.extension.DepartmentOoRepository;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Department;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class DepartmentOoRepositoryImpl implements DepartmentOoRepository {

     private final MongoTemplate mongoTemplate;

     @Autowired
     public DepartmentOoRepositoryImpl(MongoTemplate mongoTemplate) {
          this.mongoTemplate = mongoTemplate;
     }

     @Override
     public void findAndUpdate(Department department) {

          Query query = new Query();
          query.addCriteria(Criteria.where("id").is(department.getId()));
          Update update = new Update();
          update.set("title", department.getTitle());
          update.set("identity",department.getIdentity());
          update.set("url",department.getUrl());
          update.set("logoUrl",department.getLogoUrl());
          update.set("password",department.getPassword());
          update.set("schoolId",department.getSchoolId());
          if (department.getInstitutionId() != null) {
               update.set("institutionId",department.getInstitutionId());
          }
          if (department.getClassrooms() != null) {
               update.set("classrooms",department.getClassrooms());
          }
          if (department.getCustomPeriods() != null) {
               update.set("customPeriods",department.getCustomPeriods());
          }
          mongoTemplate.findAndModify(query, update, Department.class);
     }

     @Override
     public List<String> getAllClassrooms(String departmentId) {

          Department department = mongoTemplate.findById(departmentId,Department.class);
          assert department != null;
          return  department.getClassrooms();
     }
     @Override
     public void addClassroomToDepartment(String departmentId, String classroomId) {

          Department department = mongoTemplate.findById(departmentId,Department.class);
          assert department != null;
          department.getClassrooms().add(classroomId);
          mongoTemplate.save(department);
     }
     @Override
     public void deleteClassroomFromDepartment(String departmentId, String classroomId) {

          Department department = mongoTemplate.findById(departmentId,Department.class);
          assert department != null;
          department.getClassrooms().removeIf(roomId -> roomId.equals(classroomId));
          mongoTemplate.save(department);
     }
     @Override
     public long removeClassroomAssignmentsFromAllDepartments(String classroomId) {

          Query query = new Query(Criteria.where( "classrooms" ).is( classroomId ));
          Update update = new Update();
          update.pull("classrooms", classroomId);
          UpdateResult result = mongoTemplate.updateMulti( query, update, "Departments" );

          return result.getModifiedCount(); // document updated
     }
     /* BULK UPDATES */
     @Override
     public long updateCoursesDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "department._id" ).is( objID ));
          Update update = new Update();
          update.set( "department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "Courses" );

          return result.getModifiedCount(); // document updated
     }
     @Override
     public long updateStaffMembersDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "department._id" ).is( objID ));
          Update update = new Update();
          update.set( "department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "StaffMembers" );

          return result.getModifiedCount(); // document updated
     }
     @Override
     public long updateStaffMembersCourseDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "courses.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "courses.$[elem].department.title", departmentTitle );
          update.filterArray("elem.department._id", objID);
          UpdateResult result = mongoTemplate.updateMulti( query, update, "StaffMembers" );

          return result.getModifiedCount(); // document updated
     }

     @Override
     public long updateScheduledEventsResponsibleUnitsDepartment(String departmentId, String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "responsibleUnit._id" ).is( objID ));
          Update update = new Update();
          update.set( "responsibleUnit.$[elem].title", departmentTitle );
          update.filterArray("elem._id", objID);
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.events" );

          return result.getModifiedCount(); // document updated
     }
     @Override
     public long updateScheduledEventsResponsiblePersonDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "responsiblePerson.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "responsiblePerson.department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.events" );

          return result.getModifiedCount(); // document updated
     }
     @Override
     public long updateScheduledEventsEditorDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "editor.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "editor.department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.events" );

          return result.getModifiedCount(); // document updated
     }
     /* RESOURCES */
     @Override
     public long updateResourcesDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "department._id" ).is( objID ));
          Update update = new Update();
          update.set( "department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }
     public long updateResourcesSupervisorDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "supervisor.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "supervisor.department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }
     public long updateResourcesEditorDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "editor.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "editor.department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }
     public long updateResourcesCourseDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "course.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "course.department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }
     public long updateResourcesScheduledEventResponsiblePersonDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "event.responsiblePerson.department._id" ).is( objID ));
          Update update = new Update();
          update.set( "event.responsiblePerson.department.title", departmentTitle );
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }
     @Override
     public long updateResourcesScheduledEventsResponsibleUnitsDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "event.responsibleUnit._id" ).is( objID ));
          Update update = new Update();
          update.set( "event.responsibleUnit.$[elem].title", departmentTitle );
          update.filterArray("elem._id", objID);
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }
     @Override
     public long updateResourcesScheduledEventsEditorDepartment(String departmentId,String departmentIdentity, String departmentTitle) {
          ObjectId objID = new ObjectId(departmentId);
          Query query = new Query(Criteria.where( "event.editor._id" ).is( objID ));
          Update update = new Update();
          update.set( "event.editor.$[elem].title", departmentTitle );
          update.filterArray("elem._id", objID);
          UpdateResult result = mongoTemplate.updateMulti( query, update, "opendelos.resources" );

          return result.getModifiedCount(); // document updated
     }

     @Override
     public void AssignRoomsToDepartment(String departmentId, String[] roomIds) {
         for (String id : roomIds) {
               Update updateCmd = new Update();
               Query query = new Query();
               query.addCriteria(Criteria.where("id").is(departmentId));
               updateCmd.addToSet("classrooms", id);
              mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), Department.class);
         }
     }
     @Override
     public void UnAssignRoomFromDepartment(String departmentId, String roomId) {
          Update updateCmd = new Update();
          Query query = new Query();
          query.addCriteria(Criteria.where("id").is(departmentId));
          updateCmd.pull("classrooms", roomId);
          mongoTemplate.findAndModify(query, updateCmd, FindAndModifyOptions.options().upsert(true), Department.class);
     }

     /* CALENDAR */
     @Override
     public void saveCustomPeriod(String id, CustomPeriod cPeriod) {
          Department department = mongoTemplate.findById(id, Department.class );
          assert department != null;
          List<CustomPeriod> customPeriods = department.getCustomPeriods();
          int index = -1;
          boolean found = false;
          for (CustomPeriod customPeriod: customPeriods) {
               index++;
               if (customPeriod.getYear().equals(cPeriod.getYear())) {
                    customPeriods.set(index, cPeriod);
                    found = true;
                    break;
               }
          }
          if (!found) {
               customPeriods.add(cPeriod);
          }
          department.setCustomPeriods(customPeriods);
          mongoTemplate.save(department);
     }

     @Override
     public void deleteCustomPeriod(String id, String year) {
          Department department = mongoTemplate.findById(id, Department.class );
          assert department != null;
          List<CustomPeriod> customPeriods = department.getCustomPeriods();
          for (CustomPeriod customPeriod: customPeriods) {
               if (customPeriod.getYear().equals(year)) {
                    customPeriods.remove(customPeriod);
                    break;
               }
          }
          mongoTemplate.save(department);
     }

     @Override
     public CustomPeriod getCustomPeriod(String id, String year) {
          Department department = mongoTemplate.findById(id, Department.class );
          assert department != null;
          List<CustomPeriod> customPeriods = department.getCustomPeriods();
          for (CustomPeriod customPeriod: customPeriods) {
               if (customPeriod.getYear().equals(year)) {
                    return customPeriod;
               }
          }
          return null;
     }

     @Override
     public List<CustomPeriod> getCustomPeriods(String id) {
          Department department = mongoTemplate.findById(id, Department.class );
          assert department != null;
          return department.getCustomPeriods();
     }

     @Override
     public CustomPause getCustomPause(String id, String year) {
          Department department = mongoTemplate.findById(id, Department.class );
          assert department != null;
          List<CustomPause> customPauses = department.getCustomPauses();
          for (CustomPause customPause: customPauses) {
               if (customPause.getYear().equals(year)) {
                    return customPause;
               }
          }
          return null;
     }

     @Override
     public void saveCustomPause(String id, CustomPause cPause) {
          List<CustomPause> customPauses = new ArrayList<>();
          Department department = mongoTemplate.findById(id, Department.class );
          if (department != null && department.getCustomPauses() != null) {
               customPauses = department.getCustomPauses();
          }
          int index = -1;
          boolean found = false;
          for (CustomPause customPause: customPauses) {
               index++;
               if (customPause.getYear().equals(cPause.getYear())) {
                    customPauses.set(index, cPause);
                    found = true;
                    break;
               }
          }
          if (!found) {
               customPauses.add(cPause);
          }
          assert department != null;
          department.setCustomPauses(customPauses);
          mongoTemplate.save(department);
     }

     @Override
     public void deleteCustomPause(String id, String year) {
          Department department = mongoTemplate.findById(id, Department.class );
          assert department != null;
          List<CustomPause> customPauses = department.getCustomPauses();
          for (CustomPause customPause: customPauses) {
               if (customPause.getYear().equals(year)) {
                    customPauses.remove(customPause);
                    break;
               }
          }
          mongoTemplate.save(department);
     }

     @Override
     public List<CustomPause> getCustomPauses(String id) {
          List<CustomPause> customPauses = new ArrayList<>();
          Department department = mongoTemplate.findById(id, Department.class );

          if (department != null && department.getCustomPauses() != null) {
               customPauses.addAll(department.getCustomPauses());
          }
          return customPauses;
     }
}
