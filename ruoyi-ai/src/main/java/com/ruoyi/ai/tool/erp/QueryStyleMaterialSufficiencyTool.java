package com.ruoyi.ai.tool.erp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.tool.AiTool;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.erp.domain.ErpInventory;
import com.ruoyi.erp.domain.ErpStyle;
import com.ruoyi.erp.domain.ErpStyleBom;
import com.ruoyi.erp.domain.ErpStyleBomDetail;
import com.ruoyi.erp.domain.ErpStyleSku;
import com.ruoyi.erp.mapper.ErpStyleBomDetailMapper;
import com.ruoyi.erp.mapper.ErpStyleBomMapper;
import com.ruoyi.erp.mapper.ErpStyleMapper;
import com.ruoyi.erp.mapper.ErpStyleSkuMapper;
import com.ruoyi.erp.service.IErpInventoryService;

/**
 * 款式物料齐套分析工具。
 *
 * 第一版只做只读分析：根据款式 SKU 找到款式 BOM，再汇总物料库存（不限批次），
 * 计算当前物料最多可支持生产多少件。
 */
@Component
public class QueryStyleMaterialSufficiencyTool implements AiTool
{
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final ErpStyleMapper styleMapper;

    private final ErpStyleSkuMapper styleSkuMapper;

    private final ErpStyleBomMapper styleBomMapper;

    private final ErpStyleBomDetailMapper styleBomDetailMapper;

    private final IErpInventoryService inventoryService;

    public QueryStyleMaterialSufficiencyTool(ErpStyleMapper styleMapper, ErpStyleSkuMapper styleSkuMapper,
            ErpStyleBomMapper styleBomMapper, ErpStyleBomDetailMapper styleBomDetailMapper,
            IErpInventoryService inventoryService)
    {
        this.styleMapper = styleMapper;
        this.styleSkuMapper = styleSkuMapper;
        this.styleBomMapper = styleBomMapper;
        this.styleBomDetailMapper = styleBomDetailMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    public String name()
    {
        return "query_style_material_sufficiency";
    }

    @Override
    public String description()
    {
        return "分析某个款式SKU的BOM物料是否充足，并计算当前物料最多可生产多少件。";
    }

    @Override
    public JSONObject schema()
    {
        JSONObject schema = new JSONObject();
        schema.put("skuCode", "款式SKU编码，可选，例如 KDS-FLOW-002-GRAY-120");
        schema.put("styleNo", "款号，可选，例如 KDS-FLOW-002");
        schema.put("styleName", "款式名称，可选，例如 拼色连帽卫衣");
        schema.put("colorName", "颜色名称，可选，用于缩小SKU范围");
        schema.put("sizeName", "尺码名称，可选，用于缩小SKU范围");
        return schema;
    }

    @Override
    public JSONObject execute(JSONObject arguments)
    {
        // 如果无法唯一定位 SKU，工具不会猜测生产对象，而是返回候选项让上层追问。
        ErpStyleSku sku = resolveSku(arguments);
        if (sku == null)
        {
            return buildNeedSkuResult(arguments);
        }

        // 第一版按款式取可用 BOM；SKU 目前未建独立 BOM 版本关系，因此同款式 SKU 共用款式 BOM。
        List<ErpStyleBom> bomList = styleBomMapper.selectErpStyleBomByStyleId(sku.getStyleId());
        ErpStyleBom bom = selectActiveBom(bomList);
        if (bom == null)
        {
            JSONObject result = baseResult("no_bom", sku);
            result.put("message", "该款式未维护可用BOM，无法计算物料齐套情况");
            return result;
        }

        List<ErpStyleBomDetail> details = styleBomDetailMapper.selectErpStyleBomDetailByBomId(bom.getBomId());
        if (details == null || details.isEmpty())
        {
            JSONObject result = baseResult("no_bom_detail", sku);
            result.put("bomNo", bom.getBomNo());
            result.put("message", "该款式BOM没有物料明细，无法计算物料齐套情况");
            return result;
        }

        return buildSufficiencyResult(sku, bom, details);
    }

    private ErpStyleSku resolveSku(JSONObject arguments)
    {
        String skuCode = trimToNull(arguments.getString("skuCode"));
        if (skuCode != null)
        {
            ErpStyleSku sku = styleSkuMapper.selectErpStyleSkuBySkuCode(skuCode);
            if (sku != null)
            {
                return sku;
            }
        }

        /*
         * 用户给出明确款号或款式名称时，优先按 erp_style.style_id 定位款式并使用款式 BOM。
         * 这样“拼色连帽卫衣的物料是否充足”不会因为同款存在多个颜色/尺码 SKU 而被误判为必须追问。
         */
        ErpStyleSku styleTarget = resolveSingleStyleAsTarget(arguments);
        if (styleTarget != null)
        {
            return styleTarget;
        }
        styleTarget = resolveInventoryStyleAsTarget(arguments);
        if (styleTarget != null)
        {
            return styleTarget;
        }
        styleTarget = resolveBomStyleNoAsTarget(arguments);
        if (styleTarget != null)
        {
            return styleTarget;
        }

        // 没有明确 SKU 编码时，允许用款号/款名/颜色/尺码缩小候选范围。
        List<ErpStyleSku> candidates = findSkuCandidates(arguments);
        if (candidates.size() == 1)
        {
            return candidates.get(0);
        }
        return null;
    }

    private ErpStyleSku resolveSingleStyleAsTarget(JSONObject arguments)
    {
        String styleNo = trimToNull(arguments.getString("styleNo"));
        String styleName = trimToNull(arguments.getString("styleName"));
        String colorName = trimToNull(arguments.getString("colorName"));
        String sizeName = trimToNull(arguments.getString("sizeName"));
        if (StringUtils.isNotBlank(colorName) || StringUtils.isNotBlank(sizeName)
                || (styleNo == null && styleName == null))
        {
            return null;
        }

        ErpStyle query = new ErpStyle();
        query.setStyleNo(styleNo);
        query.setStyleName(styleName);
        List<ErpStyle> styles = styleMapper.selectErpStyleList(query);
        if (styles.size() != 1)
        {
            return null;
        }

        ErpStyle style = styles.get(0);
        return buildStyleTarget(style.getStyleId(), style.getStyleNo(), style.getStyleName());
    }

    private ErpStyleSku buildStyleTarget(Long styleId, String styleNo, String styleName)
    {
        ErpStyleSku target = new ErpStyleSku();
        target.setStyleId(styleId);
        target.setStyleNo(styleNo);
        target.setStyleName(StringUtils.defaultIfBlank(styleName, styleNo));
        target.setSkuCode(styleNo);
        return target;
    }

    private ErpStyleSku resolveBomStyleNoAsTarget(JSONObject arguments)
    {
        String styleNo = trimToNull(arguments.getString("styleNo"));
        String colorName = trimToNull(arguments.getString("colorName"));
        String sizeName = trimToNull(arguments.getString("sizeName"));
        if (styleNo == null || StringUtils.isNotBlank(colorName) || StringUtils.isNotBlank(sizeName))
        {
            return null;
        }

        /*
         * 兼容历史或演示数据：库存/BOM 已经有款号，但 erp_style 或 erp_style_sku 主数据不完整。
         * BOM 的 style_id/style_no 足够支撑本工具继续做只读物料齐套测算。
         */
        List<ErpStyleBom> bomList = styleBomMapper.selectErpStyleBomByStyleNo(styleNo);
        ErpStyleBom bom = selectActiveBom(bomList);
        if (bom == null || bom.getStyleId() == null)
        {
            return null;
        }
        return buildStyleTarget(bom.getStyleId(), bom.getStyleNo(), null);
    }

    private ErpStyleSku resolveInventoryStyleAsTarget(JSONObject arguments)
    {
        String styleNo = trimToNull(arguments.getString("styleNo"));
        String colorName = trimToNull(arguments.getString("colorName"));
        String sizeName = trimToNull(arguments.getString("sizeName"));
        if (styleNo == null || StringUtils.isNotBlank(colorName) || StringUtils.isNotBlank(sizeName))
        {
            return null;
        }

        /*
         * 库存查询里成衣编码可能能命中 KDS-FLOW-002，但款式主数据的 style_no 不一致。
         * 此时用库存 itemName 反查 erp_style.style_name，仍然回到 styleId + BOM 的主计算链路。
         */
        ErpInventory inventoryQuery = new ErpInventory();
        inventoryQuery.setItemType("成衣");
        inventoryQuery.setItemCode(styleNo);
        List<ErpInventory> inventories = inventoryService.selectInventoryList(inventoryQuery);
        for (ErpInventory inventory : inventories)
        {
            String itemName = trimToNull(inventory.getItemName());
            if (itemName == null)
            {
                continue;
            }
            ErpStyle styleQuery = new ErpStyle();
            styleQuery.setStyleName(itemName);
            List<ErpStyle> styles = styleMapper.selectErpStyleList(styleQuery);
            if (styles.size() == 1)
            {
                ErpStyle style = styles.get(0);
                return buildStyleTarget(style.getStyleId(), style.getStyleNo(), style.getStyleName());
            }
        }
        return null;
    }

    private JSONObject buildNeedSkuResult(JSONObject arguments)
    {
        List<ErpStyleSku> candidates = findSkuCandidates(arguments);
        JSONObject result = new JSONObject();
        result.put("status", candidates.isEmpty() ? "not_found" : "need_sku");
        result.put("message", candidates.isEmpty() ? "未找到匹配的款式SKU" : "需要确认具体SKU");

        JSONArray array = new JSONArray();
        for (int i = 0; i < candidates.size() && i < 10; i++)
        {
            ErpStyleSku sku = candidates.get(i);
            JSONObject item = new JSONObject();
            item.put("skuCode", sku.getSkuCode());
            item.put("styleNo", sku.getStyleNo());
            item.put("styleName", sku.getStyleName());
            item.put("colorName", sku.getColorName());
            item.put("sizeName", sku.getSizeName());
            array.add(item);
        }
        result.put("candidates", array);
        return result;
    }

    private List<ErpStyleSku> findSkuCandidates(JSONObject arguments)
    {
        List<ErpStyleSku> result = new ArrayList<>();
        String styleNo = trimToNull(arguments.getString("styleNo"));
        String styleName = trimToNull(arguments.getString("styleName"));
        String colorName = trimToNull(arguments.getString("colorName"));
        String sizeName = trimToNull(arguments.getString("sizeName"));

        if (styleNo != null)
        {
            ErpStyleSku query = new ErpStyleSku();
            query.setStyleNo(styleNo);
            query.setColorName(colorName);
            query.setSizeName(sizeName);
            result.addAll(styleSkuMapper.selectErpStyleSkuList(query));
        }

        if (styleName != null)
        {
            ErpStyle styleQuery = new ErpStyle();
            styleQuery.setStyleName(styleName);
            List<ErpStyle> styles = styleMapper.selectErpStyleList(styleQuery);
            for (ErpStyle style : styles)
            {
                ErpStyleSku query = new ErpStyleSku();
                query.setStyleNo(style.getStyleNo());
                query.setColorName(colorName);
                query.setSizeName(sizeName);
                result.addAll(styleSkuMapper.selectErpStyleSkuList(query));
            }
        }

        return distinctSkus(result);
    }

    private List<ErpStyleSku> distinctSkus(List<ErpStyleSku> list)
    {
        List<ErpStyleSku> result = new ArrayList<>();
        for (ErpStyleSku sku : list)
        {
            boolean exists = false;
            for (ErpStyleSku item : result)
            {
                if (StringUtils.equals(item.getSkuCode(), sku.getSkuCode()))
                {
                    exists = true;
                    break;
                }
            }
            if (!exists)
            {
                result.add(sku);
            }
        }
        return result;
    }

    private ErpStyleBom selectActiveBom(List<ErpStyleBom> bomList)
    {
        if (bomList == null || bomList.isEmpty())
        {
            return null;
        }
        // 优先使用已审核 BOM；如果历史数据没有审核状态，则退回列表第一条，保持工具可用。
        return bomList.stream()
                .filter(item -> StringUtils.equals("1", item.getBomStatus()))
                .findFirst()
                .orElse(bomList.get(0));
    }

    private JSONObject buildSufficiencyResult(ErpStyleSku sku, ErpStyleBom bom, List<ErpStyleBomDetail> details)
    {
        JSONObject result = baseResult("ok", sku);
        result.put("bomNo", bom.getBomNo());
        result.put("bomVersion", bom.getVersionNo());

        JSONArray materials = new JSONArray();
        BigDecimal maxProduceQty = null;
        JSONObject bottleneck = null;

        for (ErpStyleBomDetail detail : details)
        {
            BigDecimal requiredPerPiece = calculateRequiredPerPiece(detail);
            BigDecimal availableQty = sumMaterialAvailableQty(detail);
            // 单项物料可生产件数 = 当前可用库存 / 单件含损耗用量，向下取整表示保守可生产数量。
            BigDecimal producibleQty = requiredPerPiece.compareTo(BigDecimal.ZERO) <= 0
                    ? BigDecimal.ZERO
                    : availableQty.divide(requiredPerPiece, 0, RoundingMode.DOWN);

            JSONObject material = new JSONObject();
            material.put("materialCode", detail.getMaterialCode());
            material.put("materialName", detail.getMaterialName());
            material.put("materialType", detail.getMaterialType());
            material.put("colorName", detail.getColorName());
            material.put("specName", detail.getSpecName());
            material.put("unitName", detail.getUnitName());
            material.put("usageQty", detail.getUsageQty());
            material.put("lossRate", detail.getLossRate());
            material.put("requiredPerPiece", requiredPerPiece);
            material.put("availableQty", availableQty);
            material.put("producibleQty", producibleQty);
            materials.add(material);

            if (maxProduceQty == null || producibleQty.compareTo(maxProduceQty) < 0)
            {
                // 所有物料中的最小可生产件数决定整件成衣的生产上限，也就是瓶颈物料。
                maxProduceQty = producibleQty;
                bottleneck = material;
            }
        }

        result.put("materials", materials);
        result.put("maxProduceQty", maxProduceQty == null ? BigDecimal.ZERO : maxProduceQty);
        result.put("sufficient", maxProduceQty != null && maxProduceQty.compareTo(BigDecimal.ZERO) > 0);
        result.put("bottleneck", bottleneck);
        return result;
    }

    private JSONObject baseResult(String status, ErpStyleSku sku)
    {
        JSONObject result = new JSONObject();
        result.put("status", status);
        result.put("skuCode", sku.getSkuCode());
        result.put("targetType", StringUtils.isBlank(sku.getColorName()) && StringUtils.isBlank(sku.getSizeName()) ? "style" : "sku");
        result.put("styleNo", sku.getStyleNo());
        result.put("styleName", sku.getStyleName());
        result.put("colorName", sku.getColorName());
        result.put("sizeName", sku.getSizeName());
        return result;
    }

    private BigDecimal calculateRequiredPerPiece(ErpStyleBomDetail detail)
    {
        BigDecimal usageQty = detail.getUsageQty() == null ? BigDecimal.ZERO : detail.getUsageQty();
        BigDecimal lossRate = detail.getLossRate() == null ? BigDecimal.ZERO : detail.getLossRate();
        // BOM 中的单件用量按“标准用量 * (1 + 损耗率)”折算；损耗率兼容 0.05 和 5 两种写法。
        BigDecimal multiplier = BigDecimal.ONE.add(normalizeLossRate(lossRate));
        return usageQty.multiply(multiplier).setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeLossRate(BigDecimal lossRate)
    {
        if (lossRate.compareTo(BigDecimal.ONE) > 0)
        {
            return lossRate.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
        }
        return lossRate;
    }

    private BigDecimal sumMaterialAvailableQty(ErpStyleBomDetail detail)
    {
        ErpInventory query = new ErpInventory();
        query.setItemType("物料");
        query.setItemCode(detail.getMaterialCode());
        List<ErpInventory> inventories = inventoryService.selectInventoryList(query);

        // 这里有意不按批次筛选；业务要求是“物料不限批次”，因此汇总所有匹配库存行。
        BigDecimal total = BigDecimal.ZERO;
        for (ErpInventory inventory : inventories)
        {
            if (!matchesMaterial(detail, inventory))
            {
                continue;
            }
            total = total.add(inventory.getAvailableQty() == null ? BigDecimal.ZERO : inventory.getAvailableQty());
        }
        return total;
    }

    private boolean matchesMaterial(ErpStyleBomDetail detail, ErpInventory inventory)
    {
        /*
         * 库存表记录的是物料 SKU 编码，BOM 明细同时保存物料主编码和 materialSkuId。
         * 优先按 materialSkuId 匹配，编码前缀仅作为历史数据或缺失 SKU ID 时的兜底。
         */
        boolean sameSku = detail.getMaterialSkuId() != null && detail.getMaterialSkuId().equals(inventory.getItemId());
        boolean sameMaterialCode = StringUtils.isNotBlank(detail.getMaterialCode())
                && StringUtils.defaultString(inventory.getItemCode()).startsWith(detail.getMaterialCode());
        return StringUtils.equals("物料", inventory.getItemType())
                && (sameSku || sameMaterialCode)
                && (StringUtils.isBlank(detail.getColorName()) || StringUtils.equals(detail.getColorName(), inventory.getColorName()))
                && (StringUtils.isBlank(detail.getSpecName()) || StringUtils.equals(detail.getSpecName(), inventory.getSpecName()));
    }

    private String trimToNull(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
