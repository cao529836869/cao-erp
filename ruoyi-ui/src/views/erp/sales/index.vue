<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="销售单号"><el-input v-model="queryParams.salesOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="客户"><el-input v-model="queryParams.customerName" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.orderStatus" clearable><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:sales:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:sales:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:sales:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['erp:sales:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="销售单号" align="center" prop="salesOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.salesOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户" align="left" prop="customerName" width="190" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.customerName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="款式SKU" align="left" prop="itemSummary" min-width="280" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-summary-text">{{ scope.row.itemSummary || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="订单日期" align="center" prop="orderDate" width="110" />
      <el-table-column label="交货日期" align="center" prop="deliveryDate" width="110" />
      <el-table-column label="总数量" align="right" prop="totalQty" width="100" />
      <el-table-column label="总金额" align="right" prop="totalAmount" width="110" />
      <el-table-column label="状态" align="center" prop="orderStatus" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpStatusToneClass(scope.row.orderStatus)]">{{ scope.row.orderStatus || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="170" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:sales:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1180px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="销售单号"><el-input v-model="form.salesOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户" prop="customerName"><el-input v-model="form.customerName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-select v-model="form.orderStatus" style="width:100%"><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="订单日期"><el-date-picker v-model="form.orderDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="交货日期"><el-date-picker v-model="form.deliveryDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <div class="mb8"><el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addDetail">新增明细</el-button></div>
        <el-table :data="form.detailList" border size="mini">
          <el-table-column label="款式" width="90"><template slot-scope="scope"><el-button type="text" size="mini" @click="openSkuSelector(scope.$index)">选SKU</el-button></template></el-table-column>
          <el-table-column label="SKU编码" min-width="170"><template slot-scope="scope"><el-input v-model="scope.row.skuCode" /></template></el-table-column>
          <el-table-column label="款号" min-width="130"><template slot-scope="scope"><el-input v-model="scope.row.styleNo" /></template></el-table-column>
          <el-table-column label="款式名称" min-width="150"><template slot-scope="scope"><el-input v-model="scope.row.styleName" /></template></el-table-column>
          <el-table-column label="颜色" width="100"><template slot-scope="scope"><el-input v-model="scope.row.colorName" /></template></el-table-column>
          <el-table-column label="尺码" width="90"><template slot-scope="scope"><el-input v-model="scope.row.sizeName" /></template></el-table-column>
          <el-table-column label="数量" width="130"><template slot-scope="scope"><el-input-number v-model="scope.row.orderQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="单价" width="120"><template slot-scope="scope"><el-input-number v-model="scope.row.unitPrice" :min="0" :precision="4" controls-position="right" style="width:100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template slot-scope="scope"><el-button type="text" @click="removeDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
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
import { listSales, getSales, addSales, updateSales, delSales } from "@/api/erp/sales"
import { listStyleSku } from "@/api/erp/style"

export default {
  name: "ErpSales",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, title: "",
      orderList: [], statuses: ["草稿", "已确认", "生产中", "已完成", "已取消"],
      queryParams: { pageNum: 1, pageSize: 10, salesOrderNo: undefined, customerName: undefined, orderStatus: undefined },
      form: {}, rules: { customerName: [{ required: true, message: "客户不能为空", trigger: "blur" }] },
      skuOpen: false, skuLoading: false, skuList: [], skuTotal: 0, skuRowIndex: -1, skuQuery: { pageNum: 1, pageSize: 10, styleNo: undefined, skuCode: undefined, status: "0" }
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listSales(this.queryParams).then(res => { this.orderList = res.rows; this.total = res.total; this.loading = false }) },
    reset() { this.form = { salesOrderNo: undefined, customerName: undefined, orderDate: this.parseTime(new Date(), "{y}-{m}-{d}"), deliveryDate: undefined, orderStatus: "草稿", detailList: [] }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.salesOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增销售订单" },
    handleUpdate(row) { this.reset(); getSales(row.salesOrderId || this.ids).then(res => { this.form = { ...res.data, detailList: res.data.detailList || [] }; this.open = true; this.title = "销售订单" }) },
    addDetail() { this.form.detailList.push({ orderQty: 0, unitPrice: 0 }) },
    removeDetail(index) { this.form.detailList.splice(index, 1) },
    openSkuSelector(index) { this.skuRowIndex = index; this.skuOpen = true; this.getSkuList() },
    getSkuList() { this.skuLoading = true; listStyleSku(this.skuQuery).then(res => { this.skuList = res.rows; this.skuTotal = res.total; this.skuLoading = false }) },
    selectSku(sku) {
      const row = this.form.detailList[this.skuRowIndex]
      Object.assign(row, { styleId: sku.styleId, skuId: sku.skuId, skuCode: sku.skuCode, styleNo: sku.styleNo, styleName: sku.styleName, colorName: sku.colorName, sizeName: sku.sizeName })
      this.skuOpen = false
    },
    submitForm() { this.$refs["form"].validate(valid => { if (!valid) return; const req = this.form.salesOrderId ? updateSales(this.form) : addSales(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.salesOrderId || this.ids; this.$modal.confirm('确认删除销售订单编号为"' + ids + '"的数据项？').then(() => delSales(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    handleExport() { this.download('erp/sales/export', this.queryParams, `sales_${new Date().getTime()}.xlsx`) }
  }
}
</script>
