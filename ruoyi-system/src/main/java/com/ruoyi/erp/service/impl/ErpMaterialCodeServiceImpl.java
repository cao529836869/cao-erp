package com.ruoyi.erp.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.erp.mapper.ErpMaterialCodeMapper;
import com.ruoyi.erp.service.IErpMaterialCodeService;

/**
 * 物料编码生成Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-31
 */
@Service
public class ErpMaterialCodeServiceImpl implements IErpMaterialCodeService
{
    private static final Map<String, String> TYPE_CODE_MAP = new HashMap<String, String>();

    private static final Map<String, String> CATEGORY_CODE_MAP = new HashMap<String, String>();

    static
    {
        TYPE_CODE_MAP.put("面料", "FAB");
        TYPE_CODE_MAP.put("辅料", "ACC");
        TYPE_CODE_MAP.put("包装材料", "PKG");
        TYPE_CODE_MAP.put("包装", "PKG");

        CATEGORY_CODE_MAP.put("棉毛布", "COT");
        CATEGORY_CODE_MAP.put("棉布", "COT");
        CATEGORY_CODE_MAP.put("罗纹", "RIB");
        CATEGORY_CODE_MAP.put("纽扣", "BTN");
        CATEGORY_CODE_MAP.put("拉链", "ZIP");
        CATEGORY_CODE_MAP.put("吊牌", "TAG");
        CATEGORY_CODE_MAP.put("包装袋", "BAG");
        CATEGORY_CODE_MAP.put("纸箱", "BOX");
        CATEGORY_CODE_MAP.put("织带", "WEB");
        CATEGORY_CODE_MAP.put("松紧带", "ELT");
        CATEGORY_CODE_MAP.put("缝纫线", "THD");
        CATEGORY_CODE_MAP.put("印花", "PRT");
        CATEGORY_CODE_MAP.put("绣花", "EMB");
        CATEGORY_CODE_MAP.put("贴标", "LBL");
    }

    @Autowired
    private ErpMaterialCodeMapper erpMaterialCodeMapper;

    @Override
    public synchronized String generateMaterialCode(String materialType, String categoryName)
    {
        String typeCode = resolveTypeCode(materialType);
        String categoryCode = resolveCategoryCode(categoryName);
        String yearMonth = new SimpleDateFormat("yyMM").format(new Date());
        String prefix = typeCode + "-" + categoryCode + "-" + yearMonth + "-";
        String maxCode = erpMaterialCodeMapper.selectMaxMaterialCodeByPrefix(prefix);
        int nextSeq = parseNextSeq(maxCode);
        return prefix + String.format(Locale.ROOT, "%04d", nextSeq);
    }

    private String resolveTypeCode(String materialType)
    {
        if (StringUtils.isBlank(materialType))
        {
            return "MAT";
        }
        return TYPE_CODE_MAP.getOrDefault(materialType.trim(), "MAT");
    }

    private String resolveCategoryCode(String categoryName)
    {
        if (StringUtils.isBlank(categoryName))
        {
            return "GEN";
        }
        return CATEGORY_CODE_MAP.getOrDefault(categoryName.trim(), "GEN");
    }

    private int parseNextSeq(String maxCode)
    {
        if (StringUtils.isBlank(maxCode) || maxCode.length() < 4)
        {
            return 1;
        }
        String seq = maxCode.substring(maxCode.length() - 4);
        if (!StringUtils.isNumeric(seq))
        {
            return 1;
        }
        return Integer.parseInt(seq) + 1;
    }
}
