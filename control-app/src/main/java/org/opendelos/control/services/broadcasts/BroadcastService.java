package org.opendelos.control.services.broadcasts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.opendelos.control.repository.broadcasts.BroadcastRepository;

import org.opendelos.model.scheduler.Broadcast;
import org.opendelos.model.scheduler.ScheduleDTO;
import org.opendelos.model.scheduler.TimeTableResults;
import org.opendelos.model.scheduler.common.YouTubeBroadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {

    private final Logger logger = LoggerFactory.getLogger(BroadcastService.class);
    private final BroadcastRepository broadcastRepository;

    @Autowired
    public BroadcastService(BroadcastRepository broadcastRepository) {
        this.broadcastRepository = broadcastRepository;
    }

    public List<Broadcast> findAll() {
        logger.trace("Broadcast.findAll");
        return broadcastRepository.findAll();
    }

    public void deleteAll() {
        logger.trace("Broadcast.deleteAll");
        try {
            broadcastRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: deleteAll:" + e.getMessage());
        }
    }

    public String create(Broadcast broadcast) {
        String generatedId= null;
        broadcast.setId(null); // ensure that is not empty String
        try {
            Broadcast nInstitution =  broadcastRepository.save(broadcast);
            generatedId = nInstitution.getId();
            logger.trace(String.format("Broadcast.created with id: %s:",generatedId));
        }
        catch (Exception e) {
            logger.error("error: Broadcast.create:" + e.getMessage());
        }
        return generatedId;
    }

    public Broadcast findById(String id) {
        logger.trace(String.format("Broadcast.findById(%s)", id));
        return  broadcastRepository.findById(id).orElse(null);
    }


    public void update(Broadcast broadcast) {
        logger.trace(String.format("Broadcast.update: %s", broadcast.getId()));
        try {
            broadcastRepository.save(broadcast);
        }
        catch (Exception e) {
            logger.error("error: Broadcast.update:" + e.getMessage());
        }
    }

    public void delete(String id) {
        logger.trace(String.format("Broadcast.delete: %s", id));
        try {
            broadcastRepository.deleteById(id);
        }
        catch (Exception e) {
            logger.error("error: Broadcast.delete:" + e.getMessage());
        }
    }

    public void AddFutureScheduleDatesToBroadcasts(TimeTableResults timeTableResults) {

        //## 05/02/2022 :: Add all future broadcasts to "Scheduler.Broadcasts" Collection
        for (ScheduleDTO scheduleDTO: timeTableResults.getResults()) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
            int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
            LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
            LocalDateTime now = LocalDateTime.now();
            if (broadcast_datetime.isAfter(now)) {
                Broadcast broadcast = this.ScheduleDto2Broadcast(scheduleDTO, "scheduled",0);
                this.create(broadcast);
            }
        }
    }

    //# NOT used for now :: use it to save scheduler log (outcome of broadcast)
    public void AddScheduledDTO2Broadcasts(ScheduleDTO scheduleDTO, String status, int parts) {

            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int broadcast_hour = Integer.parseInt(scheduleDTO.getStartTime().substring(0,2));
            int broadcast_min = Integer.parseInt(scheduleDTO.getStartTime().substring(3,5));
            LocalDateTime broadcast_datetime = LocalDate.parse(scheduleDTO.getDate(), f).atTime(broadcast_hour,broadcast_min);
            LocalDateTime now = LocalDateTime.now();
            Broadcast broadcast = this.ScheduleDto2Broadcast(scheduleDTO,status,parts);
            this.create(broadcast);
    }

    public Broadcast ScheduleDto2Broadcast(ScheduleDTO scheduleDTO, String status, int parts) {

         Broadcast broadcast = new Broadcast();
         broadcast.setId(null);
         broadcast.setScheduleId(scheduleDTO.getId());
         broadcast.setDate(scheduleDTO.getDate());
         broadcast.setStartTime(scheduleDTO.getStartTime());
         broadcast.setDateModified(scheduleDTO.getDateModified());
         broadcast.setPeriod(scheduleDTO.getPeriod());
         broadcast.setAcademicYear(scheduleDTO.getAcademicYear());
         broadcast.setBroadcast(scheduleDTO.isBroadcast());
         broadcast.setAccess(scheduleDTO.getAccess());
         broadcast.setRecording(scheduleDTO.isRecording());
         broadcast.setPublication(scheduleDTO.getPublication());

         if (scheduleDTO.isBroadcastToChannel()) {
             YouTubeBroadcast youTubeBroadcast = new YouTubeBroadcast();
             youTubeBroadcast.setBroadcast(true);
             broadcast.setYouTubeBroadcast(youTubeBroadcast);
         }

         broadcast.setType(scheduleDTO.getType());
         if (scheduleDTO.getType().equals("lecture")) {
             broadcast.setReferenceId(scheduleDTO.getCourse().getId());
         }
         else  {
             broadcast.setReferenceId(scheduleDTO.getScheduledEvent().getId());
         }
         broadcast.setClassroomId(scheduleDTO.getClassroom().getId());
         broadcast.setIncludesPresentation(false);
         broadcast.setSupervisorId(scheduleDTO.getSupervisor().getId());
         broadcast.setStatus(status);
         broadcast.setParts(parts);

         return broadcast;
    }
}
