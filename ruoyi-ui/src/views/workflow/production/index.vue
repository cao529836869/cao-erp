<template>
  <div class="app-container workflow-production">
    <el-form :model="startForm" ref="startForm" size="small" :inline="true" label-width="100px" class="mb8">
      <el-form-item label="领料处理人">
        <el-input v-model="startForm.pickingAssignee" clearable placeholder="默认当前用户" />
      </el-form-item>
      <el-form-item label="裁剪处理人">
        <el-input v-model="startForm.cuttingAssignee" clearable placeholder="默认当前用户" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-s-promotion" size="mini" @click="openProductionDialog" v-hasPermi="['workflow:production:start']">发起流程</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="getList">刷新待办</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="taskList" border>
      <el-table-column label="任务" prop="taskName" width="150" />
      <el-table-column label="生产单" min-width="160">
        <template slot-scope="scope">{{ scope.row.variables.productionOrderNo || scope.row.variables.productionOrderId || '-' }}</template>
      </el-table-column>
      <el-table-column label="领料单" min-width="160">
        <template slot-scope="scope">{{ scope.row.variables.outboundOrderNo || scope.row.variables.outboundOrderId || '-' }}</template>
      </el-table-column>
      <el-table-column label="裁剪单" min-width="160">
        <template slot-scope="scope">{{ scope.row.variables.cutOrderNo || scope.row.variables.cutOrderId || '-' }}</template>
      </el-table-column>
      <el-table-column label="处理人" prop="assignee" width="120" />
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template slot-scope="scope">
          <el-button v-if="scope.row.taskName === '领料出库过账'" type="text" size="mini" icon="el-icon-box" @click="openPickingDialog(scope.row)" v-hasPermi="['workflow:production:picking']">领料过账</el-button>
          <el-button v-if="scope.row.taskName === '裁剪完成确认'" type="text" size="mini" icon="el-icon-scissors" @click="openCutDialog(scope.row)" v-hasPermi="['workflow:production:cut']">完成裁剪</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="选择草稿生产单" :visible.sync="productionOpen" width="1050px" append-to-body>
      <el-form :model="productionQuery" size="small" :inline="true" label-width="90px" class="mb8">
        <el-form-item label="生产单号">
          <el-input v-model="productionQuery.productionOrderNo" clearable @keyup.enter.native="getProductionList" />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="productionQuery.customerName" clearable @keyup.enter.native="getProductionList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="getProductionList">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetProductionQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table
        v-loading="productionLoading"
        :data="productionList"
        border
        highlight-current-row
        @row-dblclick="handleStart"
      >
        <el-table-column label="生产单号" prop="productionOrderNo" width="170" />
        <el-table-column label="客户" prop="customerName" min-width="180" show-overflow-tooltip />
        <el-table-column label="销售单号" prop="salesOrderNo" width="160" />
        <el-table-column label="款式SKU" prop="itemSummary" min-width="260" show-overflow-tooltip />
        <el-table-column label="计划开工" prop="planStartDate" width="110" />
        <el-table-column label="计划完工" prop="planFinishDate" width="110" />
        <el-table-column label="计划数" prop="totalQty" width="90" align="right" />
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" icon="el-icon-check" @click="handleStart(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="productionTotal>0"
        :total="productionTotal"
        :page.sync="productionQuery.pageNum"
        :limit.sync="productionQuery.pageSize"
        @pagination="getProductionList"
      />
    </el-dialog>

    <el-dialog title="领料批次确认" :visible.sync="pickingOpen" width="980px" append-to-body>
      <el-alert
        title="请为每个物料选择实际出库批次，保存后系统会执行领料过账并推进到裁剪单生成。"
        type="info"
        show-icon
        :closable="false"
        class="mb8"
      />
      <el-descriptions v-if="pickingForm.outboundOrderNo" :column="3" size="small" border class="mb8">
        <el-descriptions-item label="领料单">{{ pickingForm.outboundOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ pickingForm.warehouseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ pickingForm.orderStatus || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-loading="pickingLoading" :data="pickingForm.detailList" border size="mini">
        <el-table-column label="物料名称" prop="itemName" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="material-name">{{ scope.row.itemName || '-' }}</div>
            <div class="material-meta">{{ scope.row.itemCode || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="颜色/规格" min-width="130">
          <template slot-scope="scope">{{ [scope.row.colorName, scope.row.specName].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="需领数量" prop="outboundQty" width="110" align="right" />
        <el-table-column label="单位" prop="unitName" width="70" align="center" />
        <el-table-column label="批次" min-width="260">
          <template slot-scope="scope">
            <el-select
              v-model="scope.row.batchNo"
              placeholder="请选择库存批次"
              clearable
              filterable
              :loading="scope.row.batchLoading"
              :disabled="!pickingForm.warehouseId || !scope.row.itemId"
              style="width:100%"
              @visible-change="visible => handleBatchVisibleChange(visible, scope.row)"
              @change="batchNo => handleBatchChange(scope.row, batchNo)"
            >
              <el-option
                v-for="item in scope.row.batchOptions || []"
                :key="item.inventoryId"
                :label="formatBatchLabel(item)"
                :value="item.batchNo"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="pickingSubmitting" @click="handlePicking">确 定</el-button>
        <el-button @click="pickingOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="裁剪完成确认" :visible.sync="cutOpen" width="900px" append-to-body>
      <div v-loading="cutLoading">
        <el-descriptions v-if="cutOrder.cutOrderNo" :column="3" size="small" border class="mb8">
          <el-descriptions-item label="裁剪单">{{ cutOrder.cutOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="生产单">{{ cutOrder.productionOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ cutOrder.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划完成">{{ cutOrder.planFinishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划数量">{{ cutOrder.totalQty || 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ cutOrder.cutStatusName || cutOrder.cutStatus || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="cutOrder.detailList || []" border size="mini" class="mb8">
          <el-table-column label="款号" prop="styleNo" width="140" />
          <el-table-column label="款式名称" prop="styleName" min-width="180" show-overflow-tooltip />
          <el-table-column label="颜色" prop="colorName" width="100" />
          <el-table-column label="尺码" prop="sizeName" width="90" />
          <el-table-column label="计划数量" prop="planQty" width="110" align="right" />
          <el-table-column label="已裁数量" prop="cutQty" width="110" align="right" />
        </el-table>
      </div>
      <el-form ref="cutForm" :model="cutForm" :rules="cutRules" label-width="100px">
        <el-form-item label="实际裁剪数" prop="actualCutQty">
          <el-input-number v-model="cutForm.actualCutQty" :min="0.001" :precision="3" controls-position="right" style="width: 220px" />
          <span class="cut-plan-tip">计划数量：{{ cutOrder.totalQty || 0 }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="cutSubmitting" @click="handleCut">确 定</el-button>
        <el-button @click="cutOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { startProductionWorkflow, listProductionTodo, completeProductionPicking, completeProductionCut } from '@/api/workflow/production'
import { getOutbound, updateOutbound, listOutboundBatch } from '@/api/erp/outbound'
import { listProduction } from '@/api/erp/production'
import { getCut } from '@/api/erp/cut'

export default {
  name: 'ProductionWorkflow',
  data() {
    return {
      loading: false,
      taskList: [],
      pickingOpen: false,
      pickingLoading: false,
      pickingSubmitting: false,
      pickingTask: null,
      pickingForm: {
        detailList: []
      },
      batchOptionsCache: {},
      productionOpen: false,
      productionLoading: false,
      productionList: [],
      productionTotal: 0,
      productionQuery: {
        pageNum: 1,
        pageSize: 10,
        productionOrderNo: undefined,
        customerName: undefined,
        orderStatus: '草稿'
      },
      startForm: {
        pickingAssignee: undefined,
        cuttingAssignee: undefined
      },
      cutOpen: false,
      cutLoading: false,
      cutSubmitting: false,
      cutOrder: {},
      currentTask: null,
      cutForm: {
        cutOrderId: undefined,
        actualCutQty: undefined
      },
      cutRules: {
        actualCutQty: [{ required: true, message: '请输入实际裁剪数', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listProductionTodo().then(res => {
        this.taskList = res.rows || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    openProductionDialog() {
      this.productionOpen = true
      this.getProductionList()
    },
    getProductionList() {
      this.productionLoading = true
      listProduction(this.productionQuery).then(res => {
        this.productionList = res.rows || []
        this.productionTotal = res.total || 0
        this.productionLoading = false
      }).catch(() => {
        this.productionLoading = false
      })
    },
    resetProductionQuery() {
      this.productionQuery = {
        pageNum: 1,
        pageSize: 10,
        productionOrderNo: undefined,
        customerName: undefined,
        orderStatus: '草稿'
      }
      this.getProductionList()
    },
    handleStart(row) {
      const data = {
        ...this.startForm,
        productionOrderId: row.productionOrderId
      }
      startProductionWorkflow(data).then(() => {
          this.$modal.msgSuccess('流程已发起')
          this.productionOpen = false
          this.getList()
      })
    },
    openPickingDialog(row) {
      const outboundOrderId = row.variables.outboundOrderId
      if (!outboundOrderId) {
        this.$modal.msgError('当前流程还没有生成领料单')
        return
      }
      this.pickingTask = row
      this.pickingOpen = true
      this.pickingLoading = true
      this.batchOptionsCache = {}
      getOutbound(outboundOrderId).then(res => {
        this.pickingForm = {
          ...res.data,
          detailList: this.preparePickingDetails(res.data.detailList || [])
        }
        this.loadAllBatchOptions()
      }).finally(() => {
        this.pickingLoading = false
      })
    },
    preparePickingDetails(list) {
      return list.map(row => ({ ...row, batchOptions: [], batchLoading: false }))
    },
    getBatchCacheKey(row) {
      return [this.pickingForm.warehouseId || '', row.itemType || '', row.itemId || ''].join('_')
    },
    loadAllBatchOptions() {
      ;(this.pickingForm.detailList || []).forEach(row => this.loadBatchOptions(row))
    },
    loadBatchOptions(row) {
      if (!this.pickingForm.warehouseId || !row.itemId) return Promise.resolve([])
      const cacheKey = this.getBatchCacheKey(row)
      if (this.batchOptionsCache[cacheKey]) {
        this.$set(row, 'batchOptions', this.batchOptionsCache[cacheKey])
        return Promise.resolve(this.batchOptionsCache[cacheKey])
      }
      this.$set(row, 'batchLoading', true)
      return listOutboundBatch({ warehouseId: this.pickingForm.warehouseId, itemType: row.itemType || '物料', itemId: row.itemId })
        .then(res => {
          const options = (res.data || []).filter(item => item.batchNo)
          this.$set(this.batchOptionsCache, cacheKey, options)
          this.$set(row, 'batchOptions', options)
          return options
        })
        .finally(() => {
          this.$set(row, 'batchLoading', false)
        })
    },
    handleBatchVisibleChange(visible, row) {
      if (visible) this.loadBatchOptions(row)
    },
    handleBatchChange(row, batchNo) {
      const batch = (row.batchOptions || []).find(item => item.batchNo === batchNo)
      if (batch) {
        this.$set(row, 'unitName', batch.unitName || row.unitName)
        this.$set(row, 'unitPrice', batch.unitPrice || 0)
      }
    },
    formatBatchLabel(item) {
      const qty = item.availableQty !== undefined && item.availableQty !== null ? `可用：${item.availableQty}${item.unitName || ''}` : '可用：-'
      return `${item.batchNo}（${qty}）`
    },
    validatePickingBatch() {
      const missing = (this.pickingForm.detailList || []).filter(row => !row.batchNo)
      if (missing.length > 0) {
        this.$modal.msgError(`请选择批次：${missing.map(row => row.itemName || row.itemCode || row.itemId).join('、')}`)
        return false
      }
      const insufficient = (this.pickingForm.detailList || []).filter(row => {
        const batch = (row.batchOptions || []).find(item => item.batchNo === row.batchNo)
        return batch && Number(batch.availableQty || 0) < Number(row.outboundQty || 0)
      })
      if (insufficient.length > 0) {
        this.$modal.msgError(`库存不足：${insufficient.map(row => row.itemName || row.itemCode || row.itemId).join('、')}`)
        return false
      }
      return true
    },
    buildPickingSubmitForm() {
      const detailList = (this.pickingForm.detailList || []).map(({ batchOptions, batchLoading, ...row }) => row)
      return { ...this.pickingForm, detailList }
    },
    handlePicking() {
      if (this.pickingForm.orderStatus === '已出库' || this.pickingForm.orderStatus === '已完成') {
        this.pickingSubmitting = true
        completeProductionPicking(this.pickingTask.taskId, { outboundOrderId: this.pickingForm.outboundOrderId })
          .then(() => {
            this.$modal.msgSuccess('流程已推进')
            this.pickingOpen = false
            this.getList()
          })
          .finally(() => {
            this.pickingSubmitting = false
          })
        return
      }
      if (!this.validatePickingBatch()) return
      this.pickingSubmitting = true
      updateOutbound(this.buildPickingSubmitForm())
        .then(() => completeProductionPicking(this.pickingTask.taskId, { outboundOrderId: this.pickingForm.outboundOrderId }))
        .then(() => {
          this.$modal.msgSuccess('领料已过账')
          this.pickingOpen = false
          this.getList()
        })
        .finally(() => {
          this.pickingSubmitting = false
        })
    },
    openCutDialog(row) {
      const cutOrderId = row.variables.cutOrderId
      if (!cutOrderId) {
        this.$modal.msgError('当前流程还没有生成裁剪单')
        return
      }
      this.currentTask = row
      this.cutForm = {
        cutOrderId,
        actualCutQty: undefined
      }
      this.cutOrder = {}
      this.cutOpen = true
      this.cutLoading = true
      getCut(cutOrderId).then(res => {
        this.cutOrder = res.data || {}
        if (!this.cutForm.actualCutQty && this.cutOrder.totalQty) {
          this.cutForm.actualCutQty = this.cutOrder.totalQty
        }
      }).finally(() => {
        this.cutLoading = false
      })
    },
    handleCut() {
      this.$refs.cutForm.validate(valid => {
        if (!valid) return
        this.cutSubmitting = true
        completeProductionCut(this.currentTask.taskId, this.cutForm).then(() => {
          this.$modal.msgSuccess('裁剪已完成')
          this.cutOpen = false
          this.getList()
        }).finally(() => {
          this.cutSubmitting = false
        })
      })
    }
  }
}
</script>

<style scoped>
.workflow-production ::v-deep .el-input-number .el-input__inner {
  text-align: left;
}

.material-name {
  font-weight: 600;
  color: #303133;
}

.material-meta {
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}

.cut-plan-tip {
  margin-left: 12px;
  color: #606266;
}
</style>
