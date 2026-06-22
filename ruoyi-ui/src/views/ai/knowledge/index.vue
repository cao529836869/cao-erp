<template>
  <div class="app-container knowledge-page">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="78px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" clearable placeholder="请输入标题" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="模块" prop="moduleName">
        <el-input v-model="queryParams.moduleName" clearable placeholder="请输入模块" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['ai:knowledge:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['ai:knowledge:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['ai:knowledge:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="knowledgeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="标题" prop="title" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="模块" prop="moduleName" width="130" :show-overflow-tooltip="true" />
      <el-table-column label="来源" prop="sourceName" width="180" :show-overflow-tooltip="true" />
      <el-table-column label="向量模型" prop="embeddingModel" width="120" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updateTime" width="160" />
      <el-table-column label="操作" width="230" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['ai:knowledge:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" @click="handleRebuild(scope.row)" v-hasPermi="['ai:knowledge:edit']">重建向量</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['ai:knowledge:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="如：生产订单生成裁剪单条件" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模块" prop="moduleName">
              <el-input v-model="form.moduleName" placeholder="如：生产管理" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="来源类型" prop="sourceType">
              <el-select v-model="form.sourceType" clearable placeholder="请选择" style="width: 100%">
                <el-option label="业务规则" value="业务规则" />
                <el-option label="操作手册" value="操作手册" />
                <el-option label="数据库" value="数据库" />
                <el-option label="FAQ" value="FAQ" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源名称" prop="sourceName">
              <el-input v-model="form.sourceName" placeholder="如：erp_business_model.md" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="知识内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            maxlength="8000"
            show-word-limit
            placeholder="填写一段完整的 ERP 业务规则、操作说明或常见问题答案"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存并向量化</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledge, getKnowledge, addKnowledge, updateKnowledge, delKnowledge, rebuildEmbedding } from '@/api/ai/knowledge'

export default {
  name: 'AiKnowledge',
  data() {
    return {
      loading: false,
      submitLoading: false,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      knowledgeList: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        moduleName: undefined,
        status: undefined
      },
      form: {},
      rules: {
        title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
        content: [{ required: true, message: '知识内容不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listKnowledge(this.queryParams).then(response => {
        this.knowledgeList = response.rows
        this.total = response.total
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.chunkId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增知识片段'
    },
    handleUpdate(row) {
      this.reset()
      const chunkId = row.chunkId || this.ids
      getKnowledge(chunkId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改知识片段'
      })
    },
    handleRebuild(row) {
      this.$modal.confirm('确认重新生成该知识片段的向量？').then(() => {
        return rebuildEmbedding(row.chunkId)
      }).then(() => {
        this.$modal.msgSuccess('重建成功')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const chunkIds = row.chunkId || this.ids
      this.$modal.confirm('确认删除知识片段编号为 "' + chunkIds + '" 的数据项？').then(() => {
        return delKnowledge(chunkIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.submitLoading = true
        const request = this.form.chunkId ? updateKnowledge(this.form) : addKnowledge(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        chunkId: undefined,
        title: undefined,
        moduleName: undefined,
        sourceType: '业务规则',
        sourceName: undefined,
        content: undefined,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    }
  }
}
</script>

<style lang="scss" scoped>
.knowledge-page {
  background: #f5f7fa;
}
</style>
