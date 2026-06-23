package com.ruoyi.ai.tool.erp;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.tool.AiTool;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.domain.ErpStyleSku;
import com.ruoyi.erp.mapper.ErpStyleSkuMapper;
import com.ruoyi.erp.service.IErpInventoryService;

/**
 * 查询库存工具。
 *
 * 这是第一版 Agent 的示例工具：只读、低风险、能直观看到模型从“回答问题”
 * 变成“先查 ERP 数据再回答问题”的效果。
 */
@Component
public class QueryInventoryTool implements AiTool
{
    private static final int DEFAULT_LIMIT = 10;

    private static final int MAX_LIMIT = 20;

    private final IErpInventoryService inventoryService;

    private final ErpStyleSkuMapper styleSkuMapper;

    public QueryInventoryTool(IErpInventoryService inventoryService, ErpStyleSkuMapper styleSkuMapper)
    {
        this.inventoryService = inventoryService;
        this.styleSkuMapper = styleSkuMapper;
    }

    @Override
    public String name()
    {
        return "query_inventory";
    }

    @Override
    public String description()
    {
        return "查询 ERP 库存。适用于用户询问库存、可用数量、锁定数量、仓库、物料或成衣库存时。";
    }

    @Override
    public JSONObject schema()
    {
        JSONObject schema = new JSONObject();
        schema.put("warehouseName", "仓库名称，可选，支持模糊匹配，例如：成品仓");
        schema.put("itemType", "库存类型，可选，使用系统实际值：成衣 或 物料");
        schema.put("itemCode", "物料/成衣编码，可选，支持模糊匹配");
        schema.put("itemName", "物料/成衣名称，可选，支持模糊匹配");
        schema.put("batchNo", "批次号，可选，支持模糊匹配");
        schema.put("limit", "最多返回条数，可选，默认10，最大20");
        return schema;
    }

    @Override
    public JSONObject execute(JSONObject arguments)
    {
        ErpInventory query = new ErpInventory();
        query.setWarehouseName(trimToNull(arguments.getString("warehouseName")));
        query.setItemType(normalizeItemType(arguments.getString("itemType")));
        query.setItemCode(trimToNull(arguments.getString("itemCode")));
        query.setItemName(trimToNull(arguments.getString("itemName")));
        query.setBatchNo(trimToNull(arguments.getString("batchNo")));

        int limit = normalizeLimit(arguments.getInteger("limit"));
        List<ErpInventory> rows = inventoryService.selectInventoryList(query);
        ErpStyleSku matchedStyleSku = null;

        /*
         * 成衣库存的 item_code 当前保存款号，item_id 保存 sku_id；
         * 用户输入完整 SKU 编码时，先用 SKU 主数据解析，再按 item_id 兜底查库存。
         */
        if (rows.isEmpty() && StringUtils.isNotBlank(query.getItemCode()))
        {
            matchedStyleSku = styleSkuMapper.selectErpStyleSkuBySkuCode(query.getItemCode());
            if (matchedStyleSku != null)
            {
                ErpInventory skuInventoryQuery = new ErpInventory();
                skuInventoryQuery.setWarehouseName(query.getWarehouseName());
                skuInventoryQuery.setItemType("成衣");
                skuInventoryQuery.setItemId(matchedStyleSku.getSkuId());
                skuInventoryQuery.setBatchNo(query.getBatchNo());
                rows = inventoryService.selectInventoryList(skuInventoryQuery);
            }
        }

        JSONArray data = new JSONArray();
        for (int i = 0; i < rows.size() && i < limit; i++)
        {
            ErpInventory item = rows.get(i);
            JSONObject row = new JSONObject();
            row.put("inventoryId", item.getInventoryId());
            row.put("warehouseName", item.getWarehouseName());
            row.put("itemType", item.getItemType());
            row.put("itemCode", item.getItemCode());
            row.put("itemName", item.getItemName());
            row.put("colorName", item.getColorName());
            row.put("sizeName", item.getSizeName());
            row.put("specName", item.getSpecName());
            row.put("batchNo", item.getBatchNo());
            row.put("availableQty", item.getAvailableQty());
            row.put("lockedQty", item.getLockedQty());
            row.put("unitName", item.getUnitName());
            data.add(row);
        }

        JSONObject result = new JSONObject();
        result.put("total", rows.size());
        result.put("returned", data.size());
        result.put("items", data);
        if (matchedStyleSku != null)
        {
            JSONObject sku = new JSONObject();
            sku.put("skuId", matchedStyleSku.getSkuId());
            sku.put("skuCode", matchedStyleSku.getSkuCode());
            sku.put("styleNo", matchedStyleSku.getStyleNo());
            sku.put("styleName", matchedStyleSku.getStyleName());
            sku.put("colorName", matchedStyleSku.getColorName());
            sku.put("sizeName", matchedStyleSku.getSizeName());
            result.put("matchedStyleSku", sku);
        }
        return result;
    }

    private String trimToNull(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String normalizeItemType(String itemType)
    {
        String normalized = trimToNull(itemType);
        if (normalized == null)
        {
            return null;
        }
        if ("product".equalsIgnoreCase(normalized) || "finished_goods".equalsIgnoreCase(normalized) || "finished".equalsIgnoreCase(normalized))
        {
            return "成衣";
        }
        if ("material".equalsIgnoreCase(normalized))
        {
            return "物料";
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit)
    {
        if (limit == null || limit <= 0)
        {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
