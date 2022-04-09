package org.opendelos.live.services.structure;

import java.util.List;

import org.opendelos.live.repository.structure.StreamingServerRepository;
import org.opendelos.model.structure.StreamingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "streamers")
public class StreamingServerService {

    private final Logger logger = LoggerFactory.getLogger(StreamingServerService.class.getName());
    private final StreamingServerRepository streamingServerRepository;

    @Autowired
    public StreamingServerService(StreamingServerRepository streamingServerRepository) {
        this.streamingServerRepository = streamingServerRepository;
    }

    public List<StreamingServer> findAll() {
        logger.trace("StreamingServer.findAll");
        return streamingServerRepository.findAll();
    }

    @CacheEvict(allEntries = true)
    public void deleteAll() {
        logger.trace("StreamingServer.deleteAll");
        try {
            streamingServerRepository.deleteAll();
        }
        catch (Exception e) {
            logger.error("error: StreamingServer.deleteAll:" + e.getMessage());
        }
    }
    public String create(StreamingServer streamingServer) {
        String generatedId = null;
        try {
            StreamingServer nInstitution =  streamingServerRepository.save(streamingServer);
            generatedId = nInstitution.getId();
            logger.trace(String.format("StreamingServer.create: %s", streamingServer.getDescription()));
        }
        catch (Exception e) {
            logger.error("error: StreamingServer.create:" + e.getMessage());
        }
       return generatedId;
    }
    @Cacheable(key = "#id",unless="#result == null")
    public StreamingServer findById(String id) {
        logger.trace(String.format("StreamingServer.findById(%s)", id));
        return streamingServerRepository.findById(id).orElse(null);
    }
    @CacheEvict(key = "#streamingServer.id")
    public void update(StreamingServer streamingServer) {
        logger.trace(String.format("StreamingServer.update: %s", streamingServer.getDescription()));
        try {
            streamingServerRepository.save(streamingServer);
        }
        catch (Exception e) {
            logger.error("error: StreamingServer.update:" + e.getMessage());
        }
    }
    @CacheEvict(key = "#id")
    public void delete(String id) {
        logger.trace(String.format("StreamingServer.delete: %s", id));
        try {
            streamingServerRepository.deleteById(id);
        }
        catch (Exception e) {
            logger.error("error: StreamingServer.delete:" + e.getMessage());
        }
    }

    public StreamingServer findByIdentity(String identity) {
        logger.trace(String.format("StreamingServer.findByIdentity(%s)", identity));
        return streamingServerRepository.findByIdentity(identity);
    }

    public List<StreamingServer> getAllByStatus(String status) {
        return streamingServerRepository.findAllByEnabled(status);
    }
    public List<StreamingServer> getAllByStatusAndType(String status, String type) {
        return streamingServerRepository.findAllByEnabledAndType(status,type);
    }
    public List<StreamingServer> getAllIdsByEnabled(String status) {
        return streamingServerRepository.findAllIdsByEnabled(status);
    }

}
