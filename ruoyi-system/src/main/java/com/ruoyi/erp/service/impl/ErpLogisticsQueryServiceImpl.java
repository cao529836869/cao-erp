package com.ruoyi.erp.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.erp.config.ErpLogisticsProperties;
import com.ruoyi.erp.domain.ErpDeliveryOrder;
import com.ruoyi.erp.domain.ErpLogisticsQueryResult;
import com.ruoyi.erp.domain.ErpLogisticsTrace;
import com.ruoyi.erp.service.IErpLogisticsQueryService;

@Service
public class ErpLogisticsQueryServiceImpl implements IErpLogisticsQueryService
{
    private static final Map<String, String> SHIPPER_CODE_MAP = new HashMap<String, String>();

    private static final Map<String, String> STATE_NAME_MAP = new HashMap<String, String>();

    static
    {
        SHIPPER_CODE_MAP.put("顺丰", "SF");
        SHIPPER_CODE_MAP.put("顺丰速运", "SF");
        SHIPPER_CODE_MAP.put("中通", "ZTO");
        SHIPPER_CODE_MAP.put("中通快递", "ZTO");
        SHIPPER_CODE_MAP.put("圆通", "YTO");
        SHIPPER_CODE_MAP.put("圆通速递", "YTO");
        SHIPPER_CODE_MAP.put("韵达", "YD");
        SHIPPER_CODE_MAP.put("韵达快递", "YD");
        SHIPPER_CODE_MAP.put("申通", "STO");
        SHIPPER_CODE_MAP.put("申通快递", "STO");
        SHIPPER_CODE_MAP.put("邮政", "YZPY");
        SHIPPER_CODE_MAP.put("邮政快递包裹", "YZPY");
        SHIPPER_CODE_MAP.put("EMS", "EMS");
        SHIPPER_CODE_MAP.put("京东", "JD");
        SHIPPER_CODE_MAP.put("京东物流", "JD");
        SHIPPER_CODE_MAP.put("德邦", "DBL");
        SHIPPER_CODE_MAP.put("德邦快递", "DBL");
        SHIPPER_CODE_MAP.put("极兔", "JTSD");
        SHIPPER_CODE_MAP.put("极兔速递", "JTSD");
        SHIPPER_CODE_MAP.put("百世", "HTKY");
        SHIPPER_CODE_MAP.put("百世快递", "HTKY");
        SHIPPER_CODE_MAP.put("天天", "HHTT");

        STATE_NAME_MAP.put("0", "暂无轨迹");
        STATE_NAME_MAP.put("1", "已揽收");
        STATE_NAME_MAP.put("2", "在途中");
        STATE_NAME_MAP.put("3", "已签收");
        STATE_NAME_MAP.put("4", "问题件");
        STATE_NAME_MAP.put("5", "转寄");
        STATE_NAME_MAP.put("6", "清关");
        STATE_NAME_MAP.put("7", "派送中");
        STATE_NAME_MAP.put("8", "清关异常");
        STATE_NAME_MAP.put("14", "拒签");
    }

    @Autowired
    private ErpLogisticsProperties logisticsProperties;

    @Override
    public ErpLogisticsQueryResult query(ErpDeliveryOrder deliveryOrder)
    {
        ErpLogisticsQueryResult result = initResult(deliveryOrder);
        if (!logisticsProperties.isEnabled())
        {
            result.setMessage("第三方物流查询未启用，请先配置 logistics.enabled=true");
            return result;
        }
        if (!"kdniao".equalsIgnoreCase(logisticsProperties.getProvider()))
        {
            result.setMessage("暂不支持的物流查询服务：" + logisticsProperties.getProvider());
            return result;
        }
        ErpLogisticsProperties.Kdniao kdniao = logisticsProperties.getKdniao();
        if (kdniao == null || StringUtils.isBlank(kdniao.getEbusinessId()) || StringUtils.isBlank(kdniao.getAppKey()))
        {
            result.setMessage("快递鸟账号未配置，请配置 logistics.kdniao.ebusiness-id 和 logistics.kdniao.app-key");
            return result;
        }
        String shipperCode = resolveShipperCode(deliveryOrder.getLogisticsCompany());
        if (StringUtils.isBlank(shipperCode))
        {
            result.setMessage("无法识别物流公司，请填写常见快递名称，或直接填写快递鸟物流公司编码，如 SF/ZTO/YTO");
            return result;
        }
        result.setShipperCode(shipperCode);
        return queryKdniao(result, kdniao, shipperCode);
    }

    private ErpLogisticsQueryResult initResult(ErpDeliveryOrder deliveryOrder)
    {
        ErpLogisticsQueryResult result = new ErpLogisticsQueryResult();
        result.setProvider(logisticsProperties.getProvider());
        result.setLogisticsCompany(deliveryOrder.getLogisticsCompany());
        result.setTrackingNo(deliveryOrder.getTrackingNo());
        result.setSuccess(false);
        return result;
    }

    private ErpLogisticsQueryResult queryKdniao(ErpLogisticsQueryResult result, ErpLogisticsProperties.Kdniao kdniao, String shipperCode)
    {
        try
        {
            JSONObject requestData = new JSONObject();
            requestData.put("OrderCode", "");
            requestData.put("ShipperCode", shipperCode);
            requestData.put("LogisticCode", result.getTrackingNo());
            String requestDataText = requestData.toJSONString();
            String dataSign = sign(requestDataText, kdniao.getAppKey());
            String param = "RequestData=" + encode(requestDataText)
                    + "&EBusinessID=" + encode(kdniao.getEbusinessId())
                    + "&RequestType=" + encode(StringUtils.defaultIfBlank(kdniao.getRequestType(), "1002"))
                    + "&DataSign=" + encode(dataSign)
                    + "&DataType=2";
            String responseText = HttpUtils.sendPost(kdniao.getApiUrl(), param);
            if (StringUtils.isBlank(responseText))
            {
                result.setMessage("物流服务未返回数据");
                return result;
            }
            JSONObject response = JSONObject.parseObject(responseText);
            boolean success = response.getBooleanValue("Success");
            result.setSuccess(success);
            result.setState(response.getString("State"));
            result.setStateName(STATE_NAME_MAP.getOrDefault(result.getState(), StringUtils.defaultString(result.getState(), "未知")));
            result.setMessage(success ? StringUtils.defaultString(response.getString("Reason"), "查询成功") : StringUtils.defaultString(response.getString("Reason"), "查询失败"));
            result.setTraces(parseTraces(response.getJSONArray("Traces")));
            return result;
        }
        catch (Exception e)
        {
            result.setMessage("物流查询失败：" + e.getMessage());
            return result;
        }
    }

    private List<ErpLogisticsTrace> parseTraces(JSONArray traces)
    {
        List<ErpLogisticsTrace> list = new ArrayList<ErpLogisticsTrace>();
        if (traces == null)
        {
            return list;
        }
        for (int i = 0; i < traces.size(); i++)
        {
            JSONObject item = traces.getJSONObject(i);
            ErpLogisticsTrace trace = new ErpLogisticsTrace();
            trace.setTime(item.getString("AcceptTime"));
            trace.setLocation(item.getString("Location"));
            trace.setStatus(item.getString("Action"));
            trace.setDescription(item.getString("AcceptStation"));
            list.add(trace);
        }
        return list;
    }

    private String resolveShipperCode(String logisticsCompany)
    {
        if (StringUtils.isBlank(logisticsCompany))
        {
            return null;
        }
        String value = logisticsCompany.trim();
        String upper = value.toUpperCase();
        if (upper.matches("[A-Z0-9]{2,10}"))
        {
            return upper;
        }
        String compact = value.replaceAll("\\s+", "");
        return SHIPPER_CODE_MAP.get(compact);
    }

    private String sign(String requestData, String appKey) throws Exception
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest((requestData + appKey).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    private String encode(String value) throws Exception
    {
        return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8.name());
    }
}
