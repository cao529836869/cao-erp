<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="发货单号"><el-input v-model="queryParams.deliveryOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="销售单号"><el-input v-model="queryParams.salesOrderNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="客户"><el-input v-model="queryParams.customerName" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.deliveryStatus" clearable><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:delivery:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:delivery:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:delivery:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['erp:delivery:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="发货单号" align="center" prop="deliveryOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.deliveryOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="销售单号" align="center" prop="salesOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.salesOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户" align="left" prop="customerName" min-width="190" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.customerName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseName" width="140">
        <template slot-scope="scope"><span :class="['erp-chip', 'erp-warehouse-pill', $erpToneClass(scope.row.warehouseName)]">{{ scope.row.warehouseName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="发货日期" align="center" prop="deliveryDate" width="110" />
      <el-table-column label="发货数量" align="right" prop="totalQty" width="110" />
      <el-table-column label="状态" align="center" prop="deliveryStatus" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpStatusToneClass(scope.row.deliveryStatus)]">{{ scope.row.deliveryStatus || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="170" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:delivery:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="发货单号"><el-input v-model="form.deliveryOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="销售单号"><el-input v-model="form.salesOrderNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="form.customerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库"><el-select v-model="form.warehouseId" style="width:100%" @change="handleWarehouseChange"><el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货日期"><el-date-picker v-model="form.deliveryDate" value-format="yyyy-MM-dd" type="date" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="发货数量"><el-input-number v-model="form.totalQty" :min="0" :precision="3" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.deliveryStatus" style="width:100%"><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listDelivery, getDelivery, addDelivery, updateDelivery, delDelivery } from "@/api/erp/delivery"
import { optionselectWarehouse } from "@/api/erp/warehouse"

export default {
  name: "ErpDelivery",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, title: "",
      orderList: [], warehouseOptions: [], statuses: ["草稿", "已发货", "已取消"],
      queryParams: { pageNum: 1, pageSize: 10, deliveryOrderNo: undefined, salesOrderNo: undefined, customerName: undefined, deliveryStatus: undefined },
      form: {}
    }
  },
  created() { this.getWarehouses(); this.getList() },
  methods: {
    getWarehouses() { optionselectWarehouse().then(res => { this.warehouseOptions = res.data || [] }) },
    getList() { this.loading = true; listDelivery(this.queryParams).then(res => { this.orderList = res.rows; this.total = res.total; this.loading = false }) },
    reset() { this.form = { deliveryOrderNo: undefined, salesOrderNo: undefined, customerName: undefined, warehouseId: undefined, warehouseName: undefined, deliveryDate: this.parseTime(new Date(), "{y}-{m}-{d}"), totalQty: 0, deliveryStatus: "草稿" }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.deliveryOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增发货单" },
    handleUpdate(row) { this.reset(); getDelivery(row.deliveryOrderId || this.ids).then(res => { this.form = res.data; this.open = true; this.title = "发货单" }) },
    handleWarehouseChange(value) { const item = this.warehouseOptions.find(item => item.warehouseId === value); this.form.warehouseName = item ? item.warehouseName : undefined },
    submitForm() { const req = this.form.deliveryOrderId ? updateDelivery(this.form) : addDelivery(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) },
    handleDelete(row) { const ids = row.deliveryOrderId || this.ids; this.$modal.confirm('确认删除发货单编号为"' + ids + '"的数据项？').then(() => delDelivery(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    handleExport() { this.download('erp/delivery/export', this.queryParams, `delivery_${new Date().getTime()}.xlsx`) }
  }
}
</script>
