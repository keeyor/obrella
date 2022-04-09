/* 
     Author: Michael Gatzonis - 12/19/2018 
     OpenDelosDAC
*/
package org.opendelos.live.repository.structure.extension;


import java.util.List;

import org.opendelos.model.structure.StreamingServer;

public interface StreamingServerOoRepository {

     List<StreamingServer> findAllIdsByEnabled(String enabled);
}
