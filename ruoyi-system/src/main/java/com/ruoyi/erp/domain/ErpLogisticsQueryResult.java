package com.ruoyi.erp.domain;

import java.util.ArrayList;
import java.util.List;

public class ErpLogisticsQueryResult
{
    private String provider;

    private String logisticsCompany;

    private String shipperCode;

    private String trackingNo;

    private String state;

    private String stateName;

    private boolean success;

    private String message;

    private List<ErpLogisticsTrace> traces = new ArrayList<ErpLogisticsTrace>();

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getLogisticsCompany()
    {
        return logisticsCompany;
    }

    public void setLogisticsCompany(String logisticsCompany)
    {
        this.logisticsCompany = logisticsCompany;
    }

    public String getShipperCode()
    {
        return shipperCode;
    }

    public void setShipperCode(String shipperCode)
    {
        this.shipperCode = shipperCode;
    }

    public String getTrackingNo()
    {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo)
    {
        this.trackingNo = trackingNo;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getStateName()
    {
        return stateName;
    }

    public void setStateName(String stateName)
    {
        this.stateName = stateName;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public List<ErpLogisticsTrace> getTraces()
    {
        return traces;
    }

    public void setTraces(List<ErpLogisticsTrace> traces)
    {
        this.traces = traces;
    }
}
