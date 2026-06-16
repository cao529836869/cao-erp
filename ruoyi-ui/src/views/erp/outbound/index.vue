<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="出库单号"><el-input v-model="queryParams.outboundOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="出库类型"><el-select v-model="queryParams.outboundType" clearable><el-option v-for="item in outboundTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.orderStatus" clearable><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:outbound:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:outbound:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:outbound:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="出库单号" align="center" prop="outboundOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.outboundOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="outboundType" width="120">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.outboundType)]">{{ scope.row.outboundType || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseName" width="140">
        <template slot-scope="scope"><span :class="['erp-chip', 'erp-warehouse-pill', $erpToneClass(scope.row.warehouseName)]">{{ scope.row.warehouseName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户" align="left" prop="customerName" width="190" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.customerName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="物料SKU" align="left" prop="itemSummary" min-width="260" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-summary-text">{{ scope.row.itemSummary || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="出库日期" align="center" prop="outboundDate" width="110" />
      <el-table-column label="总数量" align="right" prop="totalQty" width="100" />
      <el-table-column label="总金额" align="right" prop="totalAmount" width="100" />
      <el-table-column label="状态" align="center" prop="orderStatus" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpStatusToneClass(scope.row.orderStatus)]">{{ scope.row.orderStatus || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button v-if="canPost(scope.row)" size="mini" type="text" icon="el-icon-check" @click="handlePost(scope.row)" v-hasPermi="['erp:outbound:post']">过账</el-button>
          <el-button v-if="canCancelPost(scope.row)" size="mini" type="text" icon="el-icon-refresh-left" @click="handleCancelPost(scope.row)" v-hasPermi="['erp:outbound:cancelPost']">取消过账</el-button>
          <el-button v-if="scope.row.orderStatus === '草稿'" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:outbound:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1180px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="出库单号"><el-input v-model="form.outboundOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出库类型" prop="outboundType"><el-select v-model="form.outboundType" style="width:100%"><el-option v-for="item in outboundTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出库日期" prop="outboundDate"><el-date-picker v-model="form.outboundDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%" @change="handleWarehouseChange"><el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="来源单号"><el-input v-model="form.sourceNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户"><el-input v-model="form.customerName" /></el-form-item></el-col>
        </el-row>
        <div class="mb8"><el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addDetail">新增明细</el-button></div>
        <el-table :data="form.detailList" border size="mini">
          <el-table-column label="物料" width="90"><template slot-scope="scope"><el-button type="text" size="mini" @click="openSkuSelector(scope.$index)">选物料</el-button></template></el-table-column>
          <el-table-column label="ID" width="80"><template slot-scope="scope"><el-input v-model.number="scope.row.itemId" disabled /></template></el-table-column>
          <el-table-column label="编码" min-width="180"><template slot-scope="scope"><el-input v-model="scope.row.itemCode" /></template></el-table-column>
          <el-table-column label="名称" min-width="150"><template slot-scope="scope"><el-input v-model="scope.row.itemName" /></template></el-table-column>
          <el-table-column label="颜色" width="100"><template slot-scope="scope"><el-input v-model="scope.row.colorName" /></template></el-table-column>
          <el-table-column label="规格" width="120"><template slot-scope="scope"><el-input v-model="scope.row.specName" /></template></el-table-column>
          <el-table-column label="批次" width="170">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.batchNo"
                placeholder="请选择批次"
                clearable
                filterable
                :loading="scope.row.batchLoading"
                :disabled="!form.warehouseId || !scope.row.itemId"
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
          <el-table-column label="数量" width="120"><template slot-scope="scope"><el-input-number v-model="scope.row.outboundQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="单位" width="80"><template slot-scope="scope"><el-input v-model="scope.row.unitName" /></template></el-table-column>
          <el-table-column label="单价" width="120"><template slot-scope="scope"><el-input-number v-model="scope.row.unitPrice" :min="0" :precision="4" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template slot-scope="scope"><el-button type="text" @click="removeDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button v-if="canSubmitForm()" type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
    </el-dialog>

    <el-dialog title="选择物料SKU" :visible.sync="skuOpen" width="920px" append-to-body>
      <el-form :model="skuQuery" size="small" :inline="true" label-width="80px">
        <el-form-item label="SKU编码"><el-input v-model="skuQuery.materialSkuCode" clearable @keyup.enter.native="getSkuList" /></el-form-item>
        <el-form-item label="物料名称"><el-input v-model="skuQuery.materialName" clearable @keyup.enter.native="getSkuList" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="getSkuList">搜索</el-button></el-form-item>
      </el-form>
      <el-table v-loading="skuLoading" :data="skuList" border height="360">
        <el-table-column label="SKU编码" prop="materialSkuCode" min-width="210" />
        <el-table-column label="物料名称" prop="materialName" min-width="150" />
        <el-table-column label="颜色" prop="colorName" width="90" />
        <el-table-column label="规格" prop="specName" width="120" />
        <el-table-column label="单位" prop="unitName" width="70" />
        <el-table-column label="操作" width="80" align="center"><template slot-scope="scope"><el-button type="text" size="mini" @click="selectSku(scope.row)">选择</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="skuTotal>0" :total="skuTotal" :page.sync="skuQuery.pageNum" :limit.sync="skuQuery.pageSize" @pagination="getSkuList" />
    </el-dialog>
  </div>
</template>

<script>
import { listOutbound, getOutbound, addOutbound, updateOutbound, delOutbound, postOutbound, cancelPostOutbound, listOutboundBatch } from "@/api/erp/outbound"
import { optionselectWarehouse } from "@/api/erp/warehouse"
import { listMaterialSku } from "@/api/erp/material"

export default {
  name: "ErpOutbound",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, title: "",
      orderList: [], warehouseOptions: [], outboundTypes: ["销售出库", "生产领料", "采购退货", "盘亏出库", "其他出库"], statuses: ["草稿", "已审核", "已拣货", "已出库", "已完成", "已取消", "已作废"],
      queryParams: { pageNum: 1, pageSize: 10, outboundOrderNo: undefined, outboundType: undefined, orderStatus: undefined },
      form: {}, rules: { outboundType: [{ required: true, message: "出库类型不能为空", trigger: "change" }], outboundDate: [{ required: true, message: "出库日期不能为空", trigger: "change" }], warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }] },
      skuOpen: false, skuLoading: false, skuList: [], skuTotal: 0, skuRowIndex: -1, skuQuery: { pageNum: 1, pageSize: 10, materialSkuCode: undefined, materialName: undefined },
      batchOptionsCache: {}
    }
  },
  created() { this.initQueryFromRoute(); this.getWarehouses(); this.getList() },
  watch: {
    "$route.query": {
      handler() {
        this.initQueryFromRoute()
        this.getList()
      }
    }
  },
  methods: {
    initQueryFromRoute() {
      const query = this.$route.query || {}
      if (query.outboundOrderNo) {
        this.queryParams.outboundOrderNo = query.outboundOrderNo
        this.queryParams.pageNum = 1
      }
    },
    getWarehouses() { optionselectWarehouse().then(res => { this.warehouseOptions = res.data || [] }) },
    getList() {
      this.loading = true
      listOutbound(this.queryParams).then(res => {
        this.orderList = res.rows
        this.total = res.total
        this.loading = false
        this.openRouteDetail()
      })
    },
    openRouteDetail() {
      const query = this.$route.query || {}
      if (query.openDetail !== "1" || !query.outboundOrderNo) return
      const row = (this.orderList || []).find(item => item.outboundOrderNo === query.outboundOrderNo) || (this.orderList || [])[0]
      if (row && row.outboundOrderId) {
        this.handleUpdate(row)
        this.$router.replace({ name: this.$route.name, query: { outboundOrderNo: query.outboundOrderNo } }).catch(() => {})
      }
    },
    reset() { this.form = { outboundOrderNo: undefined, outboundType: "生产领料", outboundDate: this.parseTime(new Date(), "{y}-{m}-{d}"), warehouseId: undefined, orderStatus: "草稿", detailList: [] }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.outboundOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增出库单" },
    handleUpdate(row) { this.reset(); getOutbound(row.outboundOrderId || this.ids).then(res => { this.form = { ...res.data, detailList: this.prepareDetailRows(res.data.detailList || []) }; this.open = true; this.title = "出库单"; this.loadAllDetailBatchOptions() }) },
    addDetail() { this.form.detailList.push({ itemType: "物料", outboundQty: 0, unitPrice: 0, unitName: "米", batchNo: "", batchOptions: [], batchLoading: false }) },
    removeDetail(index) { this.form.detailList.splice(index, 1) },
    openSkuSelector(index) { this.skuRowIndex = index; this.skuOpen = true; this.getSkuList() },
    getSkuList() { this.skuLoading = true; listMaterialSku(this.skuQuery).then(res => { this.skuList = res.rows; this.skuTotal = res.total; this.skuLoading = false }) },
    selectSku(sku) {
      const row = this.form.detailList[this.skuRowIndex]
      Object.assign(row, { itemType: "物料", itemId: sku.materialSkuId, itemCode: sku.materialSkuCode, itemName: sku.materialName, colorName: sku.colorName, specName: sku.specName, unitName: sku.unitName, batchNo: "" })
      this.$set(row, "batchOptions", [])
      this.loadBatchOptions(row)
      this.skuOpen = false
    },
    prepareDetailRows(list) {
      return list.map(row => ({ ...row, batchOptions: [], batchLoading: false }))
    },
    getBatchCacheKey(row) {
      return [this.form.warehouseId || "", row.itemType || "", row.itemId || ""].join("_")
    },
    loadAllDetailBatchOptions() {
      if (!this.form.warehouseId || !this.form.detailList) return
      this.form.detailList.forEach(row => this.loadBatchOptions(row))
    },
    loadBatchOptions(row) {
      if (!this.form.warehouseId || !row.itemId) return Promise.resolve([])
      const cacheKey = this.getBatchCacheKey(row)
      if (this.batchOptionsCache[cacheKey]) {
        this.$set(row, "batchOptions", this.batchOptionsCache[cacheKey])
        return Promise.resolve(this.batchOptionsCache[cacheKey])
      }
      this.$set(row, "batchLoading", true)
      return listOutboundBatch({ warehouseId: this.form.warehouseId, itemType: row.itemType || "物料", itemId: row.itemId })
        .then(res => {
          const options = (res.data || []).filter(item => item.batchNo)
          this.$set(this.batchOptionsCache, cacheKey, options)
          this.$set(row, "batchOptions", options)
          return options
        })
        .finally(() => { this.$set(row, "batchLoading", false) })
    },
    handleBatchVisibleChange(visible, row) {
      if (visible) this.loadBatchOptions(row)
    },
    handleBatchChange(row, batchNo) {
      const batch = (row.batchOptions || []).find(item => item.batchNo === batchNo)
      if (batch) {
        this.$set(row, "unitName", batch.unitName || row.unitName)
        this.$set(row, "unitPrice", batch.unitPrice || 0)
      }
    },
    handleWarehouseChange() {
      this.batchOptionsCache = {}
      ;(this.form.detailList || []).forEach(row => {
        row.batchNo = ""
        this.$set(row, "batchOptions", [])
        this.loadBatchOptions(row)
      })
    },
    formatBatchLabel(item) {
      const qty = item.availableQty !== undefined && item.availableQty !== null ? `（可用：${item.availableQty}${item.unitName || ""}）` : ""
      return `${item.batchNo}${qty}`
    },
    buildSubmitForm() {
      const detailList = (this.form.detailList || []).map(({ batchOptions, batchLoading, ...row }) => row)
      return { ...this.form, detailList }
    },
    canSubmitForm() { return !this.form.outboundOrderId || this.form.orderStatus === "草稿" },
    submitForm() { this.$refs["form"].validate(valid => { if (!valid) return; const data = this.buildSubmitForm(); const req = data.outboundOrderId ? updateOutbound(data) : addOutbound(data); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.outboundOrderId || this.ids; this.$modal.confirm('确认删除出库单编号为"' + ids + '"的数据项？').then(() => delOutbound(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    canPost(row) { return row.orderStatus === "草稿" || row.orderStatus === "已审核" || row.orderStatus === "已拣货" },
    canCancelPost(row) { return row.orderStatus === "已出库" },
    handlePost(row) { this.$modal.confirm('确认过账出库单"' + row.outboundOrderNo + '"？').then(() => postOutbound(row.outboundOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("过账成功") }).catch(() => {}) },
    handleCancelPost(row) { this.$modal.confirm('确认取消出库单"' + row.outboundOrderNo + '"的过账？库存将按原出库数量补回。').then(() => cancelPostOutbound(row.outboundOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("取消过账成功") }).catch(() => {}) }
  }
}
</script>
