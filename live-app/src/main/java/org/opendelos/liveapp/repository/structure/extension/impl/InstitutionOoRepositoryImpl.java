/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.liveapp.repository.structure.extension.impl;

import java.util.List;

import org.opendelos.liveapp.repository.structure.extension.InstitutionOoRepository;
import org.opendelos.model.dates.CustomPause;
import org.opendelos.model.dates.CustomPeriod;
import org.opendelos.model.structure.Institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class InstitutionOoRepositoryImpl implements InstitutionOoRepository {

     private final MongoTemplate mongoTemplate;

     @Autowired
     public InstitutionOoRepositoryImpl(MongoTemplate mongoTemplate) {
          this.mongoTemplate = mongoTemplate;
     }

     @Override
     public void findAndUpdate(Institution institution) {

          Query query = new Query();
          query.addCriteria(Criteria.where("id").is(institution.getId()));
          Update update = new Update();
          update.set("title", institution.getTitle());
          update.set("identity",institution.getIdentity());
          update.set("url",institution.getUrl());
          update.set("logoUrl",institution.getLogoUrl());
          update.set("Administrator",institution.getAdministrator());
          if (institution.getOrganizationLicense() != null) {
               update.set("organizationLicense", institution.getOrganizationLicense());
          }
          if (institution.getCustomPeriods() != null) {
               update.set("customPeriods",institution.getCustomPeriods());
          }
          mongoTemplate.findAndModify(query, update, Institution.class);
     }

     /* CALENDAR */
     @Override
     public void saveCustomPeriod(String id, CustomPeriod cPeriod) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          List<CustomPeriod> customPeriods = institution.getCustomPeriods();
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
          institution.setCustomPeriods(customPeriods);
          mongoTemplate.save(institution);
     }

     @Override
     public void deleteCustomPeriod(String id, String year) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          List<CustomPeriod> customPeriods = institution.getCustomPeriods();
          for (CustomPeriod customPeriod: customPeriods) {
               if (customPeriod.getYear().equals(year)) {
                    customPeriods.remove(customPeriod);
                    break;
               }
          }
          mongoTemplate.save(institution);
     }
     @Override
     public CustomPeriod getCustomPeriod(String id, String year) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          List<CustomPeriod> customPeriods = institution.getCustomPeriods();
          for (CustomPeriod customPeriod: customPeriods) {
               if (customPeriod.getYear().equals(year)) {
                    return customPeriod;
               }
          }
          return null;
     }
     @Override
     public List<CustomPeriod> getCustomPeriods(String id) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          return institution.getCustomPeriods();
     }

     /* ARGIES*/
     @Override
     public CustomPause getCustomPause(String id, String year) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          List<CustomPause> customPauses = institution.getCustomPauses();
          for (CustomPause customPause: customPauses) {
               if (customPause.getYear().equals(year)) {
                    return customPause;
               }
          }
          return null;
     }

     @Override
     public void saveCustomPause(String id, CustomPause cPause) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          List<CustomPause> customPauses = institution.getCustomPauses();
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
          institution.setCustomPauses(customPauses);
          mongoTemplate.save(institution);
     }

     @Override
     public void deleteCustomPause(String id, String year) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          List<CustomPause> customPauses = institution.getCustomPauses();
          for (CustomPause customPause: customPauses) {
               if (customPause.getYear().equals(year)) {
                    customPauses.remove(customPause);
                    break;
               }
          }
          mongoTemplate.save(institution);
     }

     @Override
     public List<CustomPause> getCustomPauses(String id) {
          Institution institution = mongoTemplate.findById(id, Institution.class );
          assert institution != null;
          return institution.getCustomPauses();
     }


}
