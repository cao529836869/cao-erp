package com.ruoyi.web.controller.ai;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.ai.domain.AiKnowledgeChunk;
import com.ruoyi.ai.service.IAiKnowledgeService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController extends BaseController
{
    @Autowired
    private IAiKnowledgeService knowledgeService;

    @PreAuthorize("@ss.hasPermi('ai:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(AiKnowledgeChunk chunk)
    {
        startPage();
        List<AiKnowledgeChunk> list = knowledgeService.selectKnowledgeChunkList(chunk);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:export')")
    @Log(title = "AI知识库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AiKnowledgeChunk chunk)
    {
        List<AiKnowledgeChunk> list = knowledgeService.selectKnowledgeChunkList(chunk);
        ExcelUtil<AiKnowledgeChunk> util = new ExcelUtil<AiKnowledgeChunk>(AiKnowledgeChunk.class);
        util.exportExcel(response, list, "AI知识库数据");
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping(value = "/{chunkId}")
    public AjaxResult getInfo(@PathVariable Long chunkId)
    {
        return success(knowledgeService.selectKnowledgeChunkById(chunkId));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:add')")
    @Log(title = "AI知识库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AiKnowledgeChunk chunk)
    {
        chunk.setCreateBy(getUsername());
        return toAjax(knowledgeService.insertKnowledgeChunk(chunk));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:edit')")
    @Log(title = "AI知识库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AiKnowledgeChunk chunk)
    {
        chunk.setUpdateBy(getUsername());
        return toAjax(knowledgeService.updateKnowledgeChunk(chunk));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:edit')")
    @Log(title = "AI知识库向量重建", businessType = BusinessType.UPDATE)
    @PutMapping("/embedding/{chunkId}")
    public AjaxResult rebuildEmbedding(@PathVariable Long chunkId)
    {
        return toAjax(knowledgeService.rebuildEmbedding(chunkId));
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:remove')")
    @Log(title = "AI知识库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{chunkIds}")
    public AjaxResult remove(@PathVariable Long[] chunkIds)
    {
        return toAjax(knowledgeService.deleteKnowledgeChunkByIds(chunkIds));
    }
}
