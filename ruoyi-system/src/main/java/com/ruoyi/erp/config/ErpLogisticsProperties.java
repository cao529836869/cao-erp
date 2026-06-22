package com.ruoyi.erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "logistics")
public class ErpLogisticsProperties
{
    private boolean enabled = false;

    private String provider = "kdniao";

    private Kdniao kdniao = new Kdniao();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public Kdniao getKdniao()
    {
        return kdniao;
    }

    public void setKdniao(Kdniao kdniao)
    {
        this.kdniao = kdniao;
    }

    public static class Kdniao
    {
        private String ebusinessId;

        private String appKey;

        private String apiUrl = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";

        private String requestType = "1002";

        public String getEbusinessId()
        {
            return ebusinessId;
        }

        public void setEbusinessId(String ebusinessId)
        {
            this.ebusinessId = ebusinessId;
        }

        public String getAppKey()
        {
            return appKey;
        }

        public void setAppKey(String appKey)
        {
            this.appKey = appKey;
        }

        public String getApiUrl()
        {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl)
        {
            this.apiUrl = apiUrl;
        }

        public String getRequestType()
        {
            return requestType;
        }

        public void setRequestType(String requestType)
        {
            this.requestType = requestType;
        }
    }
}
