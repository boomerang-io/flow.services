package io.boomerang.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import io.boomerang.kube.service.TektonServiceImpl;

/*
 * Implement asynchronous delete after a period of time to ensure
 * that logs can be retrieved / ingested from quick running tasks
 */
@Service
public class DeleteServiceImpl implements DeleteService {

//  private static final Logger LOGGER = LogManager.getLogger(DeleteServiceImpl.class);
  
  private static final long sleep = 1000;
  
  @Autowired
  private TektonServiceImpl tektonService;

  @Override
  @Async
  public void deleteTaskRun(String workflowId, String workflowActivityId, String taskActivityId, Map<String, String> customLabels) {
    try {
      Thread.sleep(sleep);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    tektonService.deleteTaskRun(workflowId,workflowActivityId, taskActivityId, customLabels);
  }
  
//  @Override
//  @Async
//  public void deleteWorkspacePVC(String workspaceId) {
//    try {
//      LOGGER.debug("Inside deleting service");
//      Thread.sleep(sleep);
//    } catch (InterruptedException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//    kubeService.deleteWorkspacePVC(workspaceId);
//  }
}
