package com.hxcoe.erp.config;

import com.hxcoe.erp.entity.AccountEntity;
import com.hxcoe.erp.entity.OrganizationEntity;
import com.hxcoe.erp.entity.WarehouseEntity;
import com.hxcoe.erp.repository.AccountRepository;
import com.hxcoe.erp.repository.OrganizationRepository;
import com.hxcoe.erp.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ErpDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ErpDataInitializer.class);

    private final WarehouseRepository warehouseRepository;
    private final OrganizationRepository organizationRepository;
    private final AccountRepository accountRepository;

    @Value("${erp.master-data.warehouse-initializer-enabled:false}")
    private boolean warehouseInitializerEnabled;

    public ErpDataInitializer(
            WarehouseRepository warehouseRepository,
            OrganizationRepository organizationRepository,
            AccountRepository accountRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.organizationRepository = organizationRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * 初始化ERP基础主数据（仓库/组织/科目），仅在缺失时补齐，避免重复写入。
     *
     * @param args 启动参数
     */
    @Override
    @Transactional
    public void run(String... args) {
        try {
            initOrganization();
            if (warehouseInitializerEnabled) {
                initWarehouses();
            }
            initAccounts();
        } catch (Exception e) {
            logger.warn("ERP基础数据初始化失败: {}", e.getMessage(), e);
        }
    }

    private void initOrganization() {
        String orgCode = "ORG-HQ";
        Optional<OrganizationEntity> existing = organizationRepository.findByOrganizationCode(orgCode);
        if (existing.isPresent()) {
            return;
        }

        OrganizationEntity entity = new OrganizationEntity();
        entity.setOrganizationCode(orgCode);
        entity.setOrganizationName("总部");
        entity.setOrganizationType("company");
        entity.setLevel(1);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        organizationRepository.save(entity);
    }

    private void initWarehouses() {
        ensureWarehouse("WH001", "默认仓库", "main", "总部");
        ensureWarehouse("WH002", "原材料仓", "raw", "总部");
        ensureWarehouse("WH003", "成品仓", "finished", "总部");
    }

    private void ensureWarehouse(String code, String name, String type, String location) {
        Optional<WarehouseEntity> existing = warehouseRepository.findByWarehouseCode(code);
        if (existing.isPresent()) {
            return;
        }

        WarehouseEntity entity = new WarehouseEntity();
        entity.setWarehouseCode(code);
        entity.setWarehouseName(name);
        entity.setWarehouseType(type);
        entity.setLocation(location);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        warehouseRepository.save(entity);
    }

    private void initAccounts() {
        ensureAccount("1001", "库存现金", "asset", "debit");
        ensureAccount("1122", "应收账款", "asset", "debit");
        ensureAccount("2202", "应付账款", "liability", "credit");
        ensureAccount("6001", "主营业务收入", "income", "credit");
        ensureAccount("6401", "主营业务成本", "expense", "debit");
    }

    private void ensureAccount(String code, String name, String accountType, String balanceDirection) {
        Optional<AccountEntity> existing = accountRepository.findByAccountCode(code);
        if (existing.isPresent()) {
            return;
        }

        AccountEntity entity = new AccountEntity();
        entity.setAccountCode(code);
        entity.setAccountName(name);
        entity.setAccountType(accountType);
        entity.setBalanceDirection(balanceDirection);
        entity.setLevel(1);
        entity.setIsLeaf(1);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setCreatedBy("system");
        entity.setUpdatedBy("system");
        accountRepository.save(entity);
    }
}
