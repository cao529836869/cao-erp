package com.ruoyi.ai.service;

import java.util.List;

public interface IAiEmbeddingService
{
    List<Double> embed(String input);

    String embedAsJson(String input);
}
