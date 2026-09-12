package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.ProcessOperationEntity;
import java.util.List;


public interface ProcessOperationService {

    /**
    * 鍒涘缓宸ュ簭
    * @param processOperation 宸ュ簭瀹炰綋
    * @return 宸ュ簭瀹炰綋;  
    */
    ProcessOperationEntity createProcessOperation(ProcessOperationEntity processOperation);

/**
* 鏍规嵁ID鏌ヨ宸ュ簭
* @param id 宸ュ簭ID
* @return 宸ュ簭瀹炰綋;
*/
ProcessOperationEntity getProcessOperationById(Long id);

/**
* 鏍规嵁宸ヨ壓璺嚎ID鏌ヨ宸ュ簭
* @param routeId 宸ヨ壓璺嚎ID
* @return 宸ュ簭鍒楄〃;
*/
List<ProcessOperationEntity> getProcessOperationsByRouteId(Long routeId);

/**
* 鏇存柊宸ュ簭
* @param id 宸ュ簭ID
* @param processOperation 宸ュ簭瀹炰綋
* @return 宸ュ簭瀹炰綋;
*/
ProcessOperationEntity updateProcessOperation(Long id, ProcessOperationEntity processOperation);

/**
* 鍒犻櫎宸ュ簭
* @param id 宸ュ簭ID
*/
void deleteProcessOperation(Long id);

/**
* 鏇存柊宸ュ簭鐨勫墠鍚庣疆鍏崇郴
* @param id 宸ュ簭ID
* @param predecessors 鍓嶇疆宸ュ簭JSON
* @return 宸ュ簭瀹炰綋;
*/
ProcessOperationEntity updateOperationPredecessors(Long id, String predecessors);
}

