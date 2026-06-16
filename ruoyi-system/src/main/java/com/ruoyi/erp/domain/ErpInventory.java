package com.ruoyi.erp.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存汇总对象 erp_inventory
 */
public class ErpInventory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inventoryId;
    private Long warehouseId;
    @Excel(name = "仓库")
    private String warehouseName;
    @Excel(name = "库存类型")
    private String itemType;
    private Long itemId;
    @Excel(name = "物料/成衣编码")
    private String itemCode;
    @Excel(name = "物料/成衣名称")
    private String itemName;
    @Excel(name = "颜色")
    private String colorName;
    @Excel(name = "尺码")
    private String sizeName;
    @Excel(name = "规格")
    private String specName;
    @Excel(name = "批次号")
    private String batchNo;
    @Excel(name = "可用库存")
    private BigDecimal availableQty;
    @Excel(name = "锁定库存")
    private BigDecimal lockedQty;
    @Excel(name = "价格")
    private BigDecimal unitPrice;
    @Excel(name = "单位")
    private String unitName;

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }
    public String getSizeName() { return sizeName; }
    public void setSizeName(String sizeName) { this.sizeName = sizeName; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public BigDecimal getLockedQty() { return lockedQty; }
    public void setLockedQty(BigDecimal lockedQty) { this.lockedQty = lockedQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}
