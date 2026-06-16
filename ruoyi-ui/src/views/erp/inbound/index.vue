<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="入库单号"><el-input v-model="queryParams.inboundOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="入库类型"><el-select v-model="queryParams.inboundType" clearable><el-option v-for="item in inboundTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.orderStatus" clearable><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:inbound:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:inbound:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:inbound:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="入库单号" align="center" prop="inboundOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.inboundOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="inboundType" width="120">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.inboundType)]">{{ scope.row.inboundType || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseName" width="140">
        <template slot-scope="scope"><span :class="['erp-chip', 'erp-warehouse-pill', $erpToneClass(scope.row.warehouseName)]">{{ scope.row.warehouseName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="供应商" align="left" prop="supplierName" width="190" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.supplierName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="物料SKU" align="left" prop="itemSummary" min-width="260" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-summary-text">{{ scope.row.itemSummary || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="入库日期" align="center" prop="inboundDate" width="110" />
      <el-table-column label="总数量" align="right" prop="totalQty" width="100" />
      <el-table-column label="总金额" align="right" prop="totalAmount" width="100" />
      <el-table-column label="状态" align="center" prop="orderStatus" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpStatusToneClass(scope.row.orderStatus)]">{{ scope.row.orderStatus || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button v-if="canPost(scope.row)" size="mini" type="text" icon="el-icon-check" @click="handlePost(scope.row)" v-hasPermi="['erp:inbound:post']">过账</el-button>
          <el-button v-if="scope.row.orderStatus === '已入库'" size="mini" type="text" icon="el-icon-refresh-left" @click="handleCancelPost(scope.row)" v-hasPermi="['erp:inbound:cancelPost']">取消过账</el-button>
          <el-button v-if="scope.row.orderStatus === '草稿'" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:inbound:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1180px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="入库单号"><el-input v-model="form.inboundOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="入库类型" prop="inboundType"><el-select v-model="form.inboundType" style="width:100%"><el-option v-for="item in inboundTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="入库日期" prop="inboundDate"><el-date-picker v-model="form.inboundDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%"><el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="来源单号"><el-input v-model="form.sourceNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="供应商"><el-input v-model="form.supplierName" /></el-form-item></el-col>
        </el-row>
        <div class="mb8"><el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addDetail">新增明细</el-button></div>
        <el-table :data="form.detailList" border size="mini">
          <el-table-column label="物料" width="90"><template slot-scope="scope"><el-button type="text" size="mini" @click="openSkuSelector(scope.$index)">选物料</el-button></template></el-table-column>
          <el-table-column label="ID" width="80"><template slot-scope="scope"><el-input v-model.number="scope.row.itemId" disabled /></template></el-table-column>
          <el-table-column label="编码" min-width="180"><template slot-scope="scope"><el-input v-model="scope.row.itemCode" /></template></el-table-column>
          <el-table-column label="名称" min-width="150"><template slot-scope="scope"><el-input v-model="scope.row.itemName" /></template></el-table-column>
          <el-table-column label="颜色" width="100"><template slot-scope="scope"><el-input v-model="scope.row.colorName" /></template></el-table-column>
          <el-table-column label="规格" width="120"><template slot-scope="scope"><el-input v-model="scope.row.specName" /></template></el-table-column>
          <el-table-column label="批次" width="110"><template slot-scope="scope"><el-input v-model="scope.row.batchNo" /></template></el-table-column>
          <el-table-column label="数量" width="120"><template slot-scope="scope"><el-input-number v-model="scope.row.inboundQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="单位" width="80"><template slot-scope="scope"><el-input v-model="scope.row.unitName" /></template></el-table-column>
          <el-table-column label="单价" width="120"><template slot-scope="scope"><el-input-number v-model="scope.row.unitPrice" :min="0" :precision="4" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template slot-scope="scope"><el-button type="text" @click="removeDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
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
import { listInbound, getInbound, addInbound, updateInbound, delInbound, postInbound, cancelPostInbound } from "@/api/erp/inbound"
import { optionselectWarehouse } from "@/api/erp/warehouse"
import { listMaterialSku } from "@/api/erp/material"

export default {
  name: "ErpInbound",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, title: "",
      orderList: [], warehouseOptions: [], inboundTypes: ["采购入库", "生产入库", "销售退货", "盘盈入库", "其他入库"], statuses: ["草稿", "已审核", "已入库", "已取消", "已作废"],
      queryParams: { pageNum: 1, pageSize: 10, inboundOrderNo: undefined, inboundType: undefined, orderStatus: undefined },
      form: {}, rules: { inboundType: [{ required: true, message: "入库类型不能为空", trigger: "change" }], inboundDate: [{ required: true, message: "入库日期不能为空", trigger: "change" }], warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }] },
      skuOpen: false, skuLoading: false, skuList: [], skuTotal: 0, skuRowIndex: -1, skuQuery: { pageNum: 1, pageSize: 10, materialSkuCode: undefined, materialName: undefined }
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
      if (query.inboundOrderNo) {
        this.queryParams.inboundOrderNo = query.inboundOrderNo
        this.queryParams.pageNum = 1
      }
    },
    getWarehouses() { optionselectWarehouse().then(res => { this.warehouseOptions = res.data || [] }) },
    getList() {
      this.loading = true
      listInbound(this.queryParams).then(res => {
        this.orderList = res.rows
        this.total = res.total
        this.loading = false
        this.openRouteDetail()
      })
    },
    openRouteDetail() {
      const query = this.$route.query || {}
      if (query.openDetail !== "1" || !query.inboundOrderNo) return
      const row = (this.orderList || []).find(item => item.inboundOrderNo === query.inboundOrderNo) || (this.orderList || [])[0]
      if (row && row.inboundOrderId) {
        this.handleUpdate(row)
        this.$router.replace({ name: this.$route.name, query: { inboundOrderNo: query.inboundOrderNo } }).catch(() => {})
      }
    },
    reset() { this.form = { inboundOrderNo: undefined, inboundType: "采购入库", inboundDate: this.parseTime(new Date(), "{y}-{m}-{d}"), warehouseId: undefined, orderStatus: "草稿", detailList: [] }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.inboundOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增入库单" },
    handleUpdate(row) { this.reset(); getInbound(row.inboundOrderId || this.ids).then(res => { this.form = { ...res.data, detailList: this.prepareDetailRows(res.data.detailList || []) }; this.open = true; this.title = "入库单" }) },
    buildEmptyDetail() {
      return {
        itemType: "物料",
        itemId: undefined,
        itemCode: "",
        itemName: "",
        colorName: "",
        sizeName: "",
        specName: "",
        batchNo: "",
        inboundQty: 0,
        unitName: "",
        unitPrice: 0
      }
    },
    prepareDetailRows(list) {
      return list.map(row => ({ ...this.buildEmptyDetail(), ...row }))
    },
    addDetail() { this.form.detailList.push(this.buildEmptyDetail()) },
    removeDetail(index) { this.form.detailList.splice(index, 1) },
    openSkuSelector(index) {
      this.skuRowIndex = index
      this.skuQuery.pageNum = 1
      this.skuOpen = true
      this.getSkuList()
    },
    getSkuList() { this.skuLoading = true; listMaterialSku(this.skuQuery).then(res => { this.skuList = res.rows; this.skuTotal = res.total; this.skuLoading = false }) },
    selectSku(sku) {
      if (this.skuRowIndex < 0) return
      const row = {
        ...this.buildEmptyDetail(),
        ...this.form.detailList[this.skuRowIndex],
        itemType: "物料",
        itemId: sku.materialSkuId,
        itemCode: sku.materialSkuCode || "",
        itemName: sku.materialName || "",
        colorName: sku.colorName || "",
        sizeName: "",
        specName: sku.specName || "",
        unitName: sku.unitName || ""
      }
      this.$set(this.form.detailList, this.skuRowIndex, row)
      this.skuOpen = false
      this.skuRowIndex = -1
    },
    submitForm() { this.$refs["form"].validate(valid => { if (!valid) return; const req = this.form.inboundOrderId ? updateInbound(this.form) : addInbound(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.inboundOrderId || this.ids; this.$modal.confirm('确认删除入库单编号为"' + ids + '"的数据项？').then(() => delInbound(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    canPost(row) { return row.orderStatus === "草稿" || row.orderStatus === "已审核" },
    handlePost(row) { this.$modal.confirm('确认过账入库单"' + row.inboundOrderNo + '"？').then(() => postInbound(row.inboundOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("过账成功") }).catch(() => {}) },
    handleCancelPost(row) { this.$modal.confirm('确认取消入库单"' + row.inboundOrderNo + '"的过账？库存将按原入库数量扣回。').then(() => cancelPostInbound(row.inboundOrderId)).then(() => { this.getList(); this.$modal.msgSuccess("取消过账成功") }).catch(() => {}) }
  }
}
</script>
