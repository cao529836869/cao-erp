<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="生产单号"><el-input v-model="queryParams.productionOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="销售单号"><el-input v-model="queryParams.salesOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="客户"><el-input v-model="queryParams.customerName" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.orderStatus" clearable><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:production:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:production:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:production:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['erp:production:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="生产单号" align="center" prop="productionOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.productionOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户" align="left" prop="customerName" width="180" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.customerName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="销售单号" align="center" prop="salesOrderNo" width="160">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.salesOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="款式SKU" align="left" prop="itemSummary" min-width="260" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-summary-text">{{ scope.row.itemSummary || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="开工日期" align="center" prop="planStartDate" width="110" />
      <el-table-column label="完工日期" align="center" prop="planFinishDate" width="110" />
      <el-table-column label="计划数" align="right" prop="totalQty" width="90" />
      <el-table-column label="完成数" align="right" prop="completedQty" width="90" />
      <el-table-column label="状态" align="center" prop="orderStatus" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpStatusToneClass(scope.row.orderStatus)]">{{ scope.row.orderStatus || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button v-if="scope.row.orderStatus === '草稿'" size="mini" type="text" icon="el-icon-s-promotion" @click="handleRelease(scope.row)" v-hasPermi="['erp:production:release']">下达</el-button>
          <el-button v-if="canBuildPicking(scope.row)" size="mini" type="text" icon="el-icon-box" @click="handleBuildPicking(scope.row)" v-hasPermi="['erp:production:picking']">领料</el-button>
          <el-button v-if="canBuildCut(scope.row)" size="mini" type="text" icon="el-icon-scissors" @click="handleBuildCut(scope.row)" v-hasPermi="['erp:cut:add']">裁剪单</el-button>
          <el-button v-if="canClose(scope.row)" size="mini" type="text" icon="el-icon-circle-close" @click="handleClose(scope.row)" v-hasPermi="['erp:production:close']">关闭</el-button>
          <el-button v-if="scope.row.orderStatus === '草稿'" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:production:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1180px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="生产单号"><el-input v-model="form.productionOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="客户">
              <el-select v-model="form.customerName" placeholder="请选择客户" clearable filterable style="width:100%">
                <el-option
                  v-for="item in customerOptions"
                  :key="item.customerId"
                  :label="item.customerName"
                  :value="item.customerName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="销售单号"><el-input v-model="form.salesOrderNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="开工日期" prop="planStartDate"><el-date-picker v-model="form.planStartDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="完工日期" prop="planFinishDate"><el-date-picker v-model="form.planFinishDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-input v-model="form.orderStatus" disabled /></el-form-item></el-col>
        </el-row>
        <div class="mb8"><el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addDetail">新增明细</el-button></div>
        <el-table :data="form.detailList" border size="mini">
          <el-table-column label="款式" width="90"><template slot-scope="scope"><el-button type="text" size="mini" @click="openSkuSelector(scope.$index)">选SKU</el-button></template></el-table-column>
          <el-table-column label="款号" min-width="150"><template slot-scope="scope"><el-input v-model="scope.row.styleNo" /></template></el-table-column>
          <el-table-column label="款式名称" min-width="150"><template slot-scope="scope"><el-input v-model="scope.row.styleName" /></template></el-table-column>
          <el-table-column label="颜色" width="100"><template slot-scope="scope"><el-input v-model="scope.row.colorName" /></template></el-table-column>
          <el-table-column label="尺码" width="90"><template slot-scope="scope"><el-input v-model="scope.row.sizeName" /></template></el-table-column>
          <el-table-column label="计划数" width="130"><template slot-scope="scope"><el-input-number v-model="scope.row.planQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="已裁剪" width="110"><template slot-scope="scope"><el-input-number v-model="scope.row.cutQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="已缝制" width="110"><template slot-scope="scope"><el-input-number v-model="scope.row.sewnQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="合格数" width="110"><template slot-scope="scope"><el-input-number v-model="scope.row.qualifiedQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template slot-scope="scope"><el-button type="text" @click="removeDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button v-if="canSubmitForm()" type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
    </el-dialog>

    <el-dialog title="选择款式SKU" :visible.sync="skuOpen" width="920px" append-to-body>
      <el-form :model="skuQuery" size="small" :inline="true" label-width="80px">
        <el-form-item label="款号"><el-input v-model="skuQuery.styleNo" clearable @keyup.enter.native="getSkuList" /></el-form-item>
        <el-form-item label="SKU"><el-input v-model="skuQuery.skuCode" clearable @keyup.enter.native="getSkuList" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="getSkuList">搜索</el-button></el-form-item>
      </el-form>
      <el-table v-loading="skuLoading" :data="skuList" border height="360">
        <el-table-column label="SKU编码" prop="skuCode" min-width="210" />
        <el-table-column label="款号" prop="styleNo" width="130" />
        <el-table-column label="款式名称" prop="styleName" min-width="150" />
        <el-table-column label="颜色" prop="colorName" width="90" />
        <el-table-column label="尺码" prop="sizeName" width="80" />
        <el-table-column label="操作" width="80" align="center"><template slot-scope="scope"><el-button type="text" size="mini" @click="selectSku(scope.row)">选择</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="skuTotal>0" :total="skuTotal" :page.sync="skuQuery.pageNum" :limit.sync="skuQuery.pageSize" @pagination="getSkuList" />
    </el-dialog>
  </div>
</template>

<script>
import { listProduction, getProduction, addProduction, updateProduction, delProduction, releaseProduction, closeProduction, buildProductionPicking, buildProductionCut } from "@/api/erp/production"
import { listStyleSku } from "@/api/erp/style"
import { optionselectCustomer } from "@/api/erp/customer"

export default {
  name: "ErpProduction",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, title: "",
      orderList: [], customerOptions: [], statuses: ["草稿", "已下达", "裁剪中", "缝制中", "后整中", "已完成", "已关闭"],
      queryParams: { pageNum: 1, pageSize: 10, productionOrderNo: undefined, salesOrderNo: undefined, customerName: undefined, orderStatus: undefined },
      form: {}, rules: { planStartDate: [{ required: true, message: "计划开工日期不能为空", trigger: "change" }], planFinishDate: [{ required: true, message: "计划完工日期不能为空", trigger: "change" }] },
      skuOpen: false, skuLoading: false, skuList: [], skuTotal: 0, skuRowIndex: -1, skuQuery: { pageNum: 1, pageSize: 10, styleNo: undefined, skuCode: undefined, status: "0" }
    }
  },
  created() { this.getCustomerOptions(); this.getList() },
  methods: {
    getCustomerOptions() { optionselectCustomer().then(res => { this.customerOptions = res.data || [] }) },
    getList() { this.loading = true; listProduction(this.queryParams).then(res => { this.orderList = res.rows; this.total = res.total; this.loading = false }) },
    reset() { this.form = { productionOrderNo: undefined, salesOrderNo: undefined, customerName: undefined, planStartDate: this.parseTime(new Date(), "{y}-{m}-{d}"), planFinishDate: undefined, orderStatus: "草稿", detailList: [] }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.productionOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增生产订单" },
    handleUpdate(row) { this.reset(); getProduction(row.productionOrderId || this.ids).then(res => { this.form = { ...res.data, detailList: this.prepareDetailRows(res.data.detailList || []) }; this.open = true; this.title = "生产订单" }) },
    buildEmptyDetail() {
      return { styleId: undefined, skuId: undefined, styleNo: "", styleName: "", colorName: "", sizeName: "", planQty: 0, cutQty: 0, sewnQty: 0, finishedQty: 0, qualifiedQty: 0 }
    },
    prepareDetailRows(list) {
      return list.map(row => ({ ...this.buildEmptyDetail(), ...row }))
    },
    addDetail() { this.form.detailList.push(this.buildEmptyDetail()) },
    removeDetail(index) { this.form.detailList.splice(index, 1) },
    openSkuSelector(index) { this.skuRowIndex = index; this.skuOpen = true; this.getSkuList() },
    getSkuList() { this.skuLoading = true; listStyleSku(this.skuQuery).then(res => { this.skuList = res.rows; this.skuTotal = res.total; this.skuLoading = false }) },
    selectSku(sku) {
      const row = { ...this.form.detailList[this.skuRowIndex], styleId: sku.styleId, skuId: sku.skuId, styleNo: sku.styleNo || "", styleName: sku.styleName || "", colorName: sku.colorName || "", sizeName: sku.sizeName || "" }
      this.$set(this.form.detailList, this.skuRowIndex, row)
      this.skuOpen = false
    },
    submitForm() { this.$refs["form"].validate(valid => { if (!valid) return; const req = this.form.productionOrderId ? updateProduction(this.form) : addProduction(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) }) },
    canSubmitForm() { return !this.form.productionOrderId || this.form.orderStatus === "草稿" },
    handleDelete(row) { const ids = row.productionOrderId || this.ids; this.$modal.confirm('确认删除生产订单编号为"' + ids + '"的数据项？').then(() => delProduction(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    handleRelease(row) { this.$modal.confirm('确认下达生产订单"' + row.productionOrderNo + '"？下达后将不能修改明细。').then(() => releaseProduction(row.productionOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("下达成功") }).catch(() => {}) },
    handleBuildPicking(row) { this.$modal.confirm('确认按BOM生成生产订单"' + row.productionOrderNo + '"的领料出库单？').then(() => buildProductionPicking(row.productionOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("领料单已生成，请到出库单确认批次后过账") }).catch(() => {}) },
    handleBuildCut(row) { this.$modal.confirm('确认根据生产订单"' + row.productionOrderNo + '"创建裁剪单？系统会校验生产领料是否已出库。').then(() => buildProductionCut(row.productionOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("裁剪单已生成") }).catch(() => {}) },
    handleClose(row) { this.$modal.confirm('确认关闭生产订单"' + row.productionOrderNo + '"？').then(() => closeProduction(row.productionOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("关闭成功") }).catch(() => {}) },
    canBuildPicking(row) { return ["已下达", "裁剪中", "缝制中", "后整中"].indexOf(row.orderStatus) !== -1 },
    canBuildCut(row) { return ["已下达", "裁剪中", "缝制中", "后整中"].indexOf(row.orderStatus) !== -1 && row.pickingPostedFlag === 1 },
    canClose(row) { return ["草稿", "已下达", "裁剪中", "缝制中", "后整中"].indexOf(row.orderStatus) !== -1 },
    handleExport() { this.download('erp/production/export', this.queryParams, `production_${new Date().getTime()}.xlsx`) }
  }
}
</script>
