/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.vodapp.repository.structure.extension;


import java.util.List;

import org.opendelos.model.structure.StreamingServer;

public interface StreamingServerOoRepository {

     List<StreamingServer> findAllIdsByEnabled(String enabled);
     List<StreamingServer> findAllIdsByEnabledAndType(String enabled, String type);
     List<StreamingServer> findAllByEnabledAndType(String enabled, String type);
}
