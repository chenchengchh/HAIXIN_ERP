package com.hxcoe.aps.service;
import com.hxcoe.aps.entity.SchedulingConstraintEntity;
import java.util.List;

public interface SchedulingConstraintService {

/**
* 鍒涘缓鎺掔绾︽潫
* @param constraint 鎺掔绾︽潫瀹炰綋
* @return 鎺掔绾︽潫瀹炰綋;
*/
SchedulingConstraintEntity createConstraint(SchedulingConstraintEntity constraint);

/**
* 鏍规嵁ID鏌ヨ鎺掔绾︽潫
* @param id 鎺掔绾︽潫ID
* @return 鎺掔绾︽潫瀹炰綋;
*/
SchedulingConstraintEntity getConstraintById(Long id);

/**
* 鏍规嵁绾︽潫绫诲瀷鏌ヨ鎺掔绾︽潫
* @param constraintType 绾︽潫绫诲瀷
* @return 鎺掔绾︽潫鍒楄〃;
*/
List<SchedulingConstraintEntity> getConstraintsByType(String constraintType);

/**
* 鏌ヨ鎵€鏈夋縺娲荤殑鎺掔绾︽潫
* @return 鎺掔绾︽潫鍒楄〃;
*/
List<SchedulingConstraintEntity> getAllActiveConstraints();

/**
* 鏌ヨ鎵€鏈夋帓绋嬬害鏉?
* @return 鎺掔绾︽潫鍒楄〃;
*/
List<SchedulingConstraintEntity> getAllConstraints();

/**
* 鏇存柊鎺掔绾︽潫
* @param id 鎺掔绾︽潫ID
* @param constraint 鎺掔绾︽潫瀹炰綋
* @return 鎺掔绾︽潫瀹炰綋;
*/
SchedulingConstraintEntity updateConstraint(Long id, SchedulingConstraintEntity constraint);

/**
* 鍒犻櫎鎺掔绾︽潫
* @param id 鎺掔绾︽潫ID
*/
void deleteConstraint(Long id);

/**
* 婵€娲绘帓绋嬬害鏉?
* @param id 鎺掔绾︽潫ID
* @return 鎺掔绾︽潫瀹炰綋;
*/
SchedulingConstraintEntity activateConstraint(Long id);

/**
* 鍋滅敤鎺掔绾︽潫
* @param id 鎺掔绾︽潫ID
* @return 鎺掔绾︽潫瀹炰綋;
*/
SchedulingConstraintEntity deactivateConstraint(Long id);
}

