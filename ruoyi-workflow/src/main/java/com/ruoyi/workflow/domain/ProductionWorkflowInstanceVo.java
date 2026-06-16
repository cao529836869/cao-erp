package com.ruoyi.workflow.domain;

import java.util.Map;

public class ProductionWorkflowInstanceVo
{
    private String processInstanceId;
    private String businessKey;
    private Map<String, Object> variables;

    public String getProcessInstanceId()
    {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        this.processInstanceId = processInstanceId;
    }

    public String getBusinessKey()
    {
        return businessKey;
    }

    public void setBusinessKey(String businessKey)
    {
        this.businessKey = businessKey;
    }

    public Map<String, Object> getVariables()
    {
        return variables;
    }

    public void setVariables(Map<String, Object> variables)
    {
        this.variables = variables;
    }
}
