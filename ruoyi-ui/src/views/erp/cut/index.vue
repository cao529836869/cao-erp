<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="裁剪单号"><el-input v-model="queryParams.cutOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="生产单号"><el-input v-model="queryParams.productionOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="客户"><el-input v-model="queryParams.customerName" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.cutStatus" clearable><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!-- <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:cut:add']">新增</el-button></el-col> -->
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:cut:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:cut:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['erp:cut:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="orderList" :row-class-name="cutRowClassName" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="裁剪单号" align="center" prop="cutOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.cutOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="生产单号" align="center" prop="productionOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.productionOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户" align="left" prop="customerName" width="180" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.customerName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="款式SKU" align="left" prop="itemSummary" min-width="260" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-summary-text">{{ scope.row.itemSummary || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="计划完成时间" align="center" prop="planFinishTime" width="160" />
      <el-table-column label="实际完成时间" align="center" prop="finishTime" width="160" />
      <el-table-column label="计划数量" align="right" prop="totalQty" width="100" />
      <el-table-column label="实际完成" align="right" prop="actualCutQty" width="100">
        <template slot-scope="scope">
          <span :class="actualCompleteClass(scope.row)">{{ formatQty(scope.row.actualCutQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpStatusToneClass(scope.row.timeoutFlag === 1 ? '已超时' : statusLabel(scope.row.cutStatus))]">
            {{ scope.row.timeoutFlag === 1 ? '已超时' : statusLabel(scope.row.cutStatus) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button v-if="canOperate(scope.row)" size="mini" type="text" icon="el-icon-check" @click="handleFinish(scope.row)" v-hasPermi="['erp:cut:finish']">完成</el-button>
          <el-button v-if="canOperate(scope.row)" size="mini" type="text" icon="el-icon-time" @click="handleDelay(scope.row)" v-hasPermi="['erp:cut:delay']">延期</el-button>
          <el-button v-if="canOperate(scope.row)" size="mini" type="text" icon="el-icon-close" @click="handleCancel(scope.row)" v-hasPermi="['erp:cut:cancel']">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1180px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" :disabled="isCutFinished" :class="{ 'cut-readonly-form': isCutFinished }">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="裁剪单号"><el-input v-model="form.cutOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="生产单号"><el-input v-model="form.productionOrderNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="销售单号"><el-input v-model="form.salesOrderNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户"><el-input v-model="form.customerName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划完成" prop="planFinishTime"><el-date-picker v-model="form.planFinishTime" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-select v-model="form.cutStatus" style="width:100%"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <div class="mb8"><el-button v-if="!isCutFinished" type="primary" plain size="mini" icon="el-icon-plus" @click="addDetail">新增明细</el-button></div>
        <el-table :data="form.detailList" border size="mini">
          <el-table-column label="款号" min-width="140"><template slot-scope="scope"><el-input v-model="scope.row.styleNo" /></template></el-table-column>
          <el-table-column label="款式名称" min-width="150"><template slot-scope="scope"><el-input v-model="scope.row.styleName" /></template></el-table-column>
          <el-table-column label="颜色" width="100"><template slot-scope="scope"><el-input v-model="scope.row.colorName" /></template></el-table-column>
          <el-table-column label="尺码" width="90"><template slot-scope="scope"><el-input v-model="scope.row.sizeName" /></template></el-table-column>
          <el-table-column label="计划数" width="130"><template slot-scope="scope"><el-input-number v-model="scope.row.planQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="已裁数" width="130"><template slot-scope="scope"><el-input-number v-model="scope.row.cutQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column v-if="!isCutFinished" label="操作" width="70"><template slot-scope="scope"><el-button type="text" @click="removeDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button v-if="!isCutFinished" type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
    </el-dialog>

    <el-dialog title="延期完成时间" :visible.sync="delayOpen" width="420px" append-to-body>
      <el-form :model="delayForm" label-width="100px">
        <el-form-item label="计划完成"><el-date-picker v-model="delayForm.planFinishTime" value-format="yyyy-MM-dd HH:mm:ss" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="delayForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button type="primary" @click="submitDelay">确 定</el-button><el-button @click="delayOpen=false">取 消</el-button></div>
    </el-dialog>

    <el-dialog title="完成裁剪" :visible.sync="finishOpen" width="420px" append-to-body>
      <el-form ref="finishForm" :model="finishForm" :rules="finishRules" label-width="90px">
        <el-form-item label="计划数">
          <el-input-number v-model="finishForm.planQty" :disabled="true" :precision="3" style="width:100%" />
        </el-form-item>
        <el-form-item label="已裁数" prop="actualCutQty">
          <el-input-number v-model="finishForm.actualCutQty" :min="0.001" :precision="3" controls-position="right" style="width:100%" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button type="primary" @click="submitFinish">确 定</el-button><el-button @click="finishOpen=false">取 消</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listCut, getCut, addCut, updateCut, delCut, finishCut, cancelCut, delayCut } from "@/api/erp/cut"

export default {
  name: "ErpCut",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, delayOpen: false, finishOpen: false, title: "",
      orderList: [], statusOptions: [{ value: 0, label: "待裁剪" }, { value: 1, label: "裁剪中" }, { value: 2, label: "已完成" }, { value: 3, label: "已取消" }],
      queryParams: { pageNum: 1, pageSize: 10, cutOrderNo: undefined, productionOrderNo: undefined, customerName: undefined, cutStatus: undefined },
      form: {}, delayForm: {}, finishForm: {},
      rules: { planFinishTime: [{ required: true, message: "计划完成时间不能为空", trigger: "change" }] },
      finishRules: { actualCutQty: [{ required: true, message: "已裁数不能为空", trigger: "change" }] }
    }
  },
  computed: {
    isCutFinished() {
      return this.form && (this.form.cutStatus === 2 || this.form.cutStatus === "2")
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listCut(this.queryParams).then(res => { this.orderList = res.rows; this.total = res.total; this.loading = false }) },
    reset() { this.form = { cutOrderNo: undefined, productionOrderNo: undefined, salesOrderNo: undefined, customerName: undefined, planFinishTime: undefined, cutStatus: 0, detailList: [] }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.cutOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增裁剪单" },
    handleUpdate(row) { this.reset(); getCut(row.cutOrderId || this.ids).then(res => { this.form = { ...res.data, detailList: res.data.detailList || [] }; this.open = true; this.title = "裁剪单" }) },
    addDetail() { this.form.detailList.push({ planQty: 0, cutQty: 0 }) },
    removeDetail(index) { this.form.detailList.splice(index, 1) },
    submitForm() {
      if (this.isCutFinished) {
        this.$modal.msgWarning("已完成的裁剪单不能修改")
        return
      }
      this.$refs["form"].validate(valid => { if (!valid) return; const req = this.form.cutOrderId ? updateCut(this.form) : addCut(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) })
    },
    handleDelete(row) { const ids = row.cutOrderId || this.ids; this.$modal.confirm('确认删除裁剪单编号为"' + ids + '"的数据项？').then(() => delCut(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    handleFinish(row) {
      this.finishForm = { cutOrderId: row.cutOrderId, cutOrderNo: row.cutOrderNo, planQty: row.totalQty || 0, actualCutQty: row.totalQty || undefined }
      this.finishOpen = true
      this.$nextTick(() => { if (this.$refs["finishForm"]) this.$refs["finishForm"].clearValidate() })
    },
    submitFinish() {
      this.$refs["finishForm"].validate(valid => {
        if (!valid) return
        finishCut(this.finishForm.cutOrderId, { actualCutQty: this.finishForm.actualCutQty }).then(() => {
          this.finishOpen = false
          this.getList()
          this.$modal.msgSuccess("裁剪单已完成，生产入库单已生成")
        })
      })
    },
    handleCancel(row) { this.$modal.confirm('确认取消裁剪单"' + row.cutOrderNo + '"？').then(() => cancelCut(row.cutOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("裁剪单已取消") }).catch(() => {}) },
    handleDelay(row) { this.delayForm = { cutOrderId: row.cutOrderId, planFinishTime: row.planFinishTime, remark: row.remark }; this.delayOpen = true },
    submitDelay() { delayCut(this.delayForm.cutOrderId, this.delayForm).then(() => { this.delayOpen = false; this.getList(); this.$modal.msgSuccess("延期成功") }) },
    canOperate(row) { return row.cutStatus === 0 || row.cutStatus === 1 },
    formatQty(value) { return value === undefined || value === null ? "0" : value },
    actualCompleteClass(row) {
      const actual = Number(row.actualCutQty || 0)
      const plan = Number(row.totalQty || 0)
      if (actual > plan) return "actual-complete-over"
      if (actual === plan) return "actual-complete-equal"
      return "actual-complete-less"
    },
    statusLabel(value) { const item = this.statusOptions.find(item => item.value === value); return item ? item.label : "未知" },
    statusTag(value) { return value === 2 ? "success" : value === 3 ? "info" : "warning" },
    cutRowClassName({ row }) { return row.timeoutFlag === 1 ? "cut-timeout-row" : "" },
    handleExport() { this.download('erp/cut/export', this.queryParams, `cut_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style scoped>
::v-deep .cut-timeout-row {
  color: #f56c6c;
  background: #fff5f5;
}
.actual-complete-over {
  color: #1d4ed8;
  font-weight: 600;
}
.actual-complete-equal {
  color: #15803d;
  font-weight: 600;
}
.actual-complete-less {
  color: #c2410c;
  font-weight: 600;
}
::v-deep .cut-readonly-form .el-input.is-disabled .el-input__inner,
::v-deep .cut-readonly-form .el-textarea.is-disabled .el-textarea__inner,
::v-deep .cut-readonly-form .el-input-number.is-disabled .el-input__inner {
  color: #1f2937;
  -webkit-text-fill-color: #1f2937;
  background-color: #f8fafc;
  border-color: #d8dee9;
  cursor: default;
}
::v-deep .cut-readonly-form .el-input.is-disabled .el-input__suffix,
::v-deep .cut-readonly-form .el-select .el-input.is-disabled .el-input__suffix {
  color: #64748b;
}
</style>
