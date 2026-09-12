package com.hxcoe.ems.init;

import com.hxcoe.ems.entity.*;
import com.hxcoe.ems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数据初始化类
 * 在应用启动时生成化妆品行业相关的模拟数据
 *
 * @author author
 * @date 2026-01-01
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private MeterDeviceRepository meterDeviceRepository;

    @Autowired
    private RealTimeDataRepository realTimeDataRepository;

    @Autowired
    private EnergyAnomalyRepository energyAnomalyRepository;

    @Autowired
    private OptimizationSuggestionRepository optimizationSuggestionRepository;

    @Autowired
    private CalibrationHistoryRepository calibrationHistoryRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有数据，避免重复初始化
        if (meterDeviceRepository.count() == 0) {
            System.out.println("开始初始化EMS模块模拟数据...");
            // 1. 创建设备
            List<MeterDeviceEntity> devices = createMeterDevices();
            // 2. 创建实时数据
            createRealTimeData(devices);
            // 3. 创建能耗异常
            createEnergyAnomalies(devices);
            // 4. 创建优化建议
            createOptimizationSuggestions();
            // 5. 创建校准历史
            createCalibrationHistory(devices);
            System.out.println("EMS模块模拟数据初始化完成！");
        } else {
            System.out.println("EMS模块已有数据，跳过初始化。");
        }
    }

    /**
     * 创建采集设备
     *
     * @return 设备列表
     */
    private List<MeterDeviceEntity> createMeterDevices() {
        List<MeterDeviceEntity> devices = new ArrayList<>();

        // 化妆品工厂区域
        String[] areas = {"生产车间", "包装车间", "仓库", "办公区", "实验室"};
        // 能源类型
        String[] energyTypes = {"电力", "水", "燃气", "热能"};
        // 设备类型映射
        String[] deviceTypes = {"电表", "水表", "燃气表", "热能表"};

        int deviceId = 1;
        for (String area : areas) {
            for (int i = 0; i < energyTypes.length; i++) {
                MeterDeviceEntity device = new MeterDeviceEntity();
                device.setName(area + "-" + deviceTypes[i]);
                device.setType(deviceTypes[i]);
                device.setIpAddress("192.168.1." + deviceId);
                device.setStatus(random.nextBoolean() ? "online" : "offline");
                device.setLastUpdate(LocalDateTime.now());
                device.setCreatedAt(LocalDateTime.now());
                device.setUpdatedAt(LocalDateTime.now());
                devices.add(device);
                deviceId++;
            }
        }

        return meterDeviceRepository.saveAll(devices);
    }

    /**
     * 创建实时数据
     *
     * @param devices 设备列表
     */
    private void createRealTimeData(List<MeterDeviceEntity> devices) {
        List<RealTimeDataEntity> realTimeDataList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        
        // 为每个设备生成最近24小时的实时数据，每小时一条
        for (MeterDeviceEntity device : devices) {
            String energyType = "";
            String unit = "";
            double baseValue = 0;

            // 根据设备类型确定能源类型和单位
            switch (device.getType()) {
                case "电表":
                    energyType = "电力";
                    unit = "kWh";
                    baseValue = 100 + random.nextDouble() * 200;
                    break;
                case "水表":
                    energyType = "水";
                    unit = "m³";
                    baseValue = 10 + random.nextDouble() * 50;
                    break;
                case "燃气表":
                    energyType = "燃气";
                    unit = "m³";
                    baseValue = 20 + random.nextDouble() * 80;
                    break;
                case "热能表":
                    energyType = "热能";
                    unit = "GJ";
                    baseValue = 5 + random.nextDouble() * 25;
                    break;
            }

            // 生成24小时数据
            for (int hour = 0; hour < 24; hour++) {
                RealTimeDataEntity realTimeData = new RealTimeDataEntity();
                realTimeData.setEnergyType(energyType);
                realTimeData.setArea(device.getName().split("-")[0]);
                realTimeData.setActualValue(baseValue + random.nextDouble() * 50);
                realTimeData.setUnit(unit);
                realTimeData.setCollectionTime(now.minusHours(hour));
                realTimeData.setStatus(random.nextDouble() > 0.1 ? "normal" : "abnormal"); // 90%正常，10%异常
                realTimeData.setDeviceId(device.getId());
                realTimeData.setCreatedAt(LocalDateTime.now());
                realTimeData.setUpdatedAt(LocalDateTime.now());
                realTimeDataList.add(realTimeData);
            }
        }

        realTimeDataRepository.saveAll(realTimeDataList);
    }

    /**
     * 创建能耗异常
     *
     * @param devices 设备列表
     */
    private void createEnergyAnomalies(List<MeterDeviceEntity> devices) {
        List<EnergyAnomalyEntity> anomalies = new ArrayList<>();

        // 异常类型
        String[] anomalyTypes = {"能耗突增", "能耗突降", "持续高能耗", "持续低能耗"};
        LocalDateTime now = LocalDateTime.now();

        // 为每个设备生成1-3个异常
        for (MeterDeviceEntity device : devices) {
            int anomalyCount = random.nextInt(3) + 1;

            for (int i = 0; i < anomalyCount; i++) {
                EnergyAnomalyEntity anomaly = new EnergyAnomalyEntity();
                anomaly.setEnergyType(device.getType().equals("电表") ? "电力" : 
                                        device.getType().equals("水表") ? "水" : 
                                        device.getType().equals("燃气表") ? "燃气" : "热能");
                anomaly.setArea(device.getName().split("-")[0]);
                anomaly.setAnomalyType(anomalyTypes[random.nextInt(anomalyTypes.length)]);
                anomaly.setActualValue(150 + random.nextDouble() * 200);
                anomaly.setExpectedValue(100 + random.nextDouble() * 100);
                anomaly.setDetectionTime(now.minusHours(random.nextInt(24)));
                anomaly.setStatus(random.nextBoolean() ? "pending" : "processed");
                
                if (anomaly.getStatus().equals("processed")) {
                    anomaly.setProcessedTime(now.minusHours(random.nextInt(12)));
                    anomaly.setProcessedBy("管理员" + (random.nextInt(5) + 1));
                    anomaly.setProcessedRemark("已处理，原因：设备故障");
                }
                
                anomaly.setCreatedAt(LocalDateTime.now());
                anomaly.setUpdatedAt(LocalDateTime.now());
                anomalies.add(anomaly);
            }
        }

        energyAnomalyRepository.saveAll(anomalies);
    }

    /**
     * 创建优化建议
     */
    private void createOptimizationSuggestions() {
        List<OptimizationSuggestionEntity> suggestions = new ArrayList<>();

        // 化妆品工厂区域
        String[] areas = {"生产车间", "包装车间", "仓库", "办公区", "实验室"};
        
        // 优化建议模板
        String[][] suggestionTemplates = {
                {"优化设备运行时间", "建议调整设备运行时间，避开用电高峰期，预计可节省15%的电力消耗。", "15%电力节省"},
                {"更换高效设备", "建议更换为高效节能设备，预计可降低20%的能耗。", "20%能耗降低"},
                {"优化生产工艺", "建议优化生产工艺，减少能源浪费，预计可节省10%的能源消耗。", "10%能源节省"},
                {"加强设备维护", "建议定期维护设备，提高设备效率，预计可降低8%的能耗。", "8%能耗降低"},
                {"安装节能控制系统", "建议安装节能控制系统，实现能源的智能管理，预计可节省25%的能源消耗。", "25%能源节省"}
        };

        LocalDateTime now = LocalDateTime.now();

        // 为每个区域生成2-4条优化建议
        for (String area : areas) {
            int suggestionCount = random.nextInt(3) + 2;

            for (int i = 0; i < suggestionCount; i++) {
                OptimizationSuggestionEntity suggestion = new OptimizationSuggestionEntity();
                int templateIndex = random.nextInt(suggestionTemplates.length);
                suggestion.setTitle(suggestionTemplates[templateIndex][0]);
                suggestion.setContent(suggestionTemplates[templateIndex][1]);
                suggestion.setTargetArea(area);
                suggestion.setEstimatedEffect(suggestionTemplates[templateIndex][2]);
                suggestion.setStatus(random.nextBoolean() ? "pending" : "adopted");
                suggestion.setCreatedAt(now.minusDays(random.nextInt(30)));
                suggestion.setUpdatedAt(now.minusDays(random.nextInt(30)));
                suggestions.add(suggestion);
            }
        }

        optimizationSuggestionRepository.saveAll(suggestions);
    }

    /**
     * 创建校准历史
     *
     * @param devices 设备列表
     */
    private void createCalibrationHistory(List<MeterDeviceEntity> devices) {
        List<CalibrationHistoryEntity> histories = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        // 为每个设备生成3-5条校准历史
        for (MeterDeviceEntity device : devices) {
            int historyCount = random.nextInt(3) + 3;

            for (int i = 0; i < historyCount; i++) {
                CalibrationHistoryEntity history = new CalibrationHistoryEntity();
                history.setMeterId(device.getId());
                history.setMeterName(device.getName());
                double rawValue = 100 + random.nextDouble() * 200;
                double coefficient = 0.95 + random.nextDouble() * 0.1;
                history.setRawValue(rawValue);
                history.setCalibratedValue(rawValue * coefficient);
                history.setCoefficient(coefficient);
                history.setReason("定期校准");
                history.setCalibrationTime(now.minusDays(random.nextInt(90)));
                history.setCreatedAt(LocalDateTime.now());
                history.setUpdatedAt(LocalDateTime.now());
                histories.add(history);
            }
        }

        calibrationHistoryRepository.saveAll(histories);
    }
}