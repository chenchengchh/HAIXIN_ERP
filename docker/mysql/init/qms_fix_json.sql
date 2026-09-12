-- 更新检验标准JSON字段以匹配DTO结构
USE qms_db;

UPDATE qms_inspection_standard SET inspection_items_json='[{"itemName":"外观检查","itemType":"qualitative","specification":"无划痕、无氧化","tolerance":"","testMethod":"目视检查","testEquipment":"放大镜"},{"itemName":"尺寸测量","itemType":"quantitative","specification":"符合图纸公差","tolerance":"±0.1mm","testMethod":"卡尺测量","testEquipment":"数显卡尺"}]' WHERE standard_no='STD-2024-001';

UPDATE qms_inspection_standard SET inspection_items_json='[{"itemName":"硬度测试","itemType":"quantitative","specification":"HRC 45-50","tolerance":"HRC 45-50","testMethod":"硬度计测试","testEquipment":"洛氏硬度计"},{"itemName":"表面处理","itemType":"quantitative","specification":"镀层厚度≥10μm","tolerance":"≥10μm","testMethod":"测厚仪","testEquipment":"镀层测厚仪"}]' WHERE standard_no='STD-2024-002';

UPDATE qms_inspection_standard SET inspection_items_json='[{"itemName":"耐破强度","itemType":"quantitative","specification":"≥1500kPa","tolerance":"≥1500kPa","testMethod":"耐破度仪","testEquipment":"耐破度测试仪"},{"itemName":"防潮性能","itemType":"quantitative","specification":"24h增重≤5%","tolerance":"≤5%","testMethod":"恒温恒湿箱","testEquipment":"恒温恒湿试验箱"}]' WHERE standard_no='STD-2024-003';

UPDATE qms_inspection_standard SET inspection_items_json='[{"itemName":"纯度分析","itemType":"quantitative","specification":"≥99.5%","tolerance":"≥99.5%","testMethod":"色谱分析","testEquipment":"气相色谱仪"},{"itemName":"水分含量","itemType":"quantitative","specification":"≤0.5%","tolerance":"≤0.5%","testMethod":"卡尔费休法","testEquipment":"水分测定仪"}]' WHERE standard_no='STD-2024-004';

UPDATE qms_inspection_standard SET inspection_items_json='[{"itemName":"功能测试","itemType":"qualitative","specification":"全部功能正常","tolerance":"","testMethod":"老化测试","testEquipment":"老化测试台"},{"itemName":"安全测试","itemType":"qualitative","specification":"符合GB4943","tolerance":"","testMethod":"安规测试","testEquipment":"安规测试仪"}]' WHERE standard_no='STD-2024-005';

SELECT 'JSON字段更新完成' AS message;
