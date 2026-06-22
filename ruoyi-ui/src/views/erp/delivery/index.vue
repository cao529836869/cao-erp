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
      <el-table-column label="客户" align="left" prop="customerName" min-width="180" show-overflow-tooltip />
      <el-table-column label="仓库" align="center" prop="warehouseName" width="140" />
      <el-table-column label="发货日期" align="center" prop="deliveryDate" width="110" />
      <el-table-column label="发货数量" align="right" prop="totalQty" width="110" />
      <el-table-column label="出库单号" align="center" prop="outboundOrderNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.outboundOrderNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="物流单号" align="center" prop="trackingNo" width="170" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-button v-if="scope.row.trackingNo" type="text" size="mini" @click="openLogisticsInfo(scope.row)">{{ scope.row.trackingNo }}</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="deliveryStatus" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpStatusToneClass(scope.row.deliveryStatus)]">{{ scope.row.deliveryStatus || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleUpdate(scope.row)">查看</el-button>
          <el-button v-if="scope.row.deliveryStatus === '草稿'" size="mini" type="text" icon="el-icon-check" @click="handleConfirm(scope.row)" v-hasPermi="['erp:delivery:confirm']">确认</el-button>
          <el-button v-if="scope.row.deliveryStatus === '已确认'" size="mini" type="text" icon="el-icon-close" @click="handleCancelDelivery(scope.row)" v-hasPermi="['erp:delivery:cancel']">取消</el-button>
          <el-button v-if="scope.row.deliveryStatus === '已发货'" size="mini" type="text" icon="el-icon-truck" @click="openLogisticsEdit(scope.row)" v-hasPermi="['erp:delivery:logistics']">物流</el-button>
          <el-button v-if="scope.row.deliveryStatus === '草稿'" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:delivery:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="1180px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="发货单号"><el-input v-model="form.deliveryOrderNo" placeholder="留空自动生成" :disabled="!canEditForm()" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="销售单号"><el-input v-model="form.salesOrderNo" readonly><el-button slot="append" icon="el-icon-search" :disabled="!canEditForm()" @click="openSalesSelector" /></el-input></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户"><el-input v-model="form.customerName" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" style="width:100%" :disabled="!canEditForm()" @change="handleWarehouseChange"><el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="发货日期" prop="deliveryDate"><el-date-picker v-model="form.deliveryDate" value-format="yyyy-MM-dd" type="date" style="width:100%" :disabled="!canEditForm()" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-input v-model="form.deliveryStatus" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出库单号"><el-input v-model="form.outboundOrderNo" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="物流公司"><el-input v-model="form.logisticsCompany" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="物流单号"><el-input v-model="form.trackingNo" disabled /></el-form-item></el-col>
        </el-row>
        <div class="mb8"><el-button type="primary" plain size="mini" icon="el-icon-plus" :disabled="!canEditForm()" @click="addDetail">新增明细</el-button></div>
        <el-table :data="form.detailList" border size="mini">
          <el-table-column label="销售明细" width="150">
            <template slot-scope="scope">
              <el-select v-model="scope.row.salesDetailId" clearable filterable :disabled="!canEditForm()" @change="id => handleSalesDetailChange(scope.row, id)">
                <el-option v-for="item in salesDetailOptions" :key="item.salesDetailId" :label="formatSalesDetail(item)" :value="item.salesDetailId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="90"><template slot-scope="scope"><el-button type="text" size="mini" :disabled="!canEditForm() || !form.warehouseId" @click="openInventorySelector(scope.$index)">选库存</el-button></template></el-table-column>
          <el-table-column label="SKU编码" prop="skuCode" min-width="170" />
          <el-table-column label="款式名称" prop="styleName" min-width="150" />
          <el-table-column label="颜色" prop="colorName" width="100" />
          <el-table-column label="尺码" prop="sizeName" width="90" />
          <el-table-column label="批次" prop="batchNo" width="130" />
          <el-table-column label="库存" prop="stockQty" align="right" width="100" />
          <el-table-column label="锁定" prop="lockedQty" align="right" width="100" />
          <el-table-column label="可用" align="right" width="100"><template slot-scope="scope">{{ calcAvailable(scope.row) }}</template></el-table-column>
          <el-table-column label="发货数量" width="130"><template slot-scope="scope"><el-input-number v-model="scope.row.deliveryQty" :min="0" :max="calcAvailable(scope.row)" :precision="3" controls-position="right" style="width:100%" :disabled="!canEditForm()" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template slot-scope="scope"><el-button type="text" :disabled="!canEditForm()" @click="removeDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="canEditForm()" type="primary" @click="submitForm">保 存</el-button>
        <el-button v-if="form.deliveryOrderId && form.deliveryStatus === '草稿'" type="success" @click="handleConfirm(form)">确认并生成出库单</el-button>
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="选择销售订单" :visible.sync="salesOpen" width="980px" append-to-body>
      <el-form :model="salesQuery" size="small" :inline="true" label-width="80px">
        <el-form-item label="销售单号"><el-input v-model="salesQuery.salesOrderNo" clearable @keyup.enter.native="getSalesList" /></el-form-item>
        <el-form-item label="客户"><el-input v-model="salesQuery.customerName" clearable @keyup.enter.native="getSalesList" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="getSalesList">搜索</el-button></el-form-item>
      </el-form>
      <el-table v-loading="salesLoading" :data="salesList" border height="360">
        <el-table-column label="销售单号" prop="salesOrderNo" width="170" />
        <el-table-column label="客户" prop="customerName" min-width="190" />
        <el-table-column label="订单日期" prop="orderDate" width="110" />
        <el-table-column label="交货日期" prop="deliveryDate" width="110" />
        <el-table-column label="状态" prop="orderStatus" width="100" />
        <el-table-column label="操作" width="80" align="center"><template slot-scope="scope"><el-button type="text" size="mini" @click="selectSales(scope.row)">选择</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="salesTotal>0" :total="salesTotal" :page.sync="salesQuery.pageNum" :limit.sync="salesQuery.pageSize" @pagination="getSalesList" />
    </el-dialog>

    <el-dialog title="选择成衣库存" :visible.sync="inventoryOpen" width="1080px" append-to-body>
      <el-form :model="inventoryQuery" size="small" :inline="true" label-width="80px">
        <el-form-item label="SKU编码"><el-input v-model="inventoryQuery.itemCode" clearable @keyup.enter.native="getInventoryList" /></el-form-item>
        <el-form-item label="款式名称"><el-input v-model="inventoryQuery.itemName" clearable @keyup.enter.native="getInventoryList" /></el-form-item>
        <el-form-item label="批次"><el-input v-model="inventoryQuery.batchNo" clearable @keyup.enter.native="getInventoryList" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="getInventoryList">搜索</el-button></el-form-item>
      </el-form>
      <el-table v-loading="inventoryLoading" :data="inventoryList" border height="380">
        <el-table-column label="SKU编码" prop="itemCode" min-width="180" />
        <el-table-column label="款式名称" prop="itemName" min-width="150" />
        <el-table-column label="颜色" prop="colorName" width="90" />
        <el-table-column label="尺码" prop="sizeName" width="90" />
        <el-table-column label="批次" prop="batchNo" width="130" />
        <el-table-column label="库存" prop="availableQty" align="right" width="100" />
        <el-table-column label="锁定" prop="lockedQty" align="right" width="100" />
        <el-table-column label="可用" align="right" width="100"><template slot-scope="scope">{{ inventoryAvailable(scope.row) }}</template></el-table-column>
        <el-table-column label="操作" width="80" align="center"><template slot-scope="scope"><el-button type="text" size="mini" :disabled="inventoryAvailable(scope.row) <= 0" @click="selectInventory(scope.row)">选择</el-button></template></el-table-column>
      </el-table>
      <pagination v-show="inventoryTotal>0" :total="inventoryTotal" :page.sync="inventoryQuery.pageNum" :limit.sync="inventoryQuery.pageSize" @pagination="getInventoryList" />
    </el-dialog>

    <el-dialog title="物流信息" :visible.sync="logisticsOpen" width="760px" append-to-body>
      <el-form ref="logisticsForm" :model="logisticsForm" :rules="logisticsRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="发货单号"><el-input v-model="logisticsForm.deliveryOrderNo" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="销售单号"><el-input v-model="logisticsForm.salesOrderNo" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户"><el-input v-model="logisticsForm.customerName" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出库单号"><el-input v-model="logisticsForm.outboundOrderNo" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="物流公司"><el-input v-model="logisticsForm.logisticsCompany" :disabled="!logisticsEditable" placeholder="如 顺丰速运" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="物流单号" prop="trackingNo"><el-input v-model="logisticsForm.trackingNo" :disabled="!logisticsEditable" placeholder="请输入物流单号" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div v-if="!logisticsEditable" v-loading="logisticsLoading">
        <el-alert
          v-if="logisticsResult.message"
          :type="logisticsResult.success ? 'success' : 'warning'"
          :closable="false"
          show-icon
          :title="formatLogisticsMessage()"
        />
        <el-table class="mt12" :data="logisticsResult.traces || []" border size="mini" empty-text="暂无物流轨迹">
          <el-table-column label="时间" prop="time" width="160" />
          <el-table-column label="地点" prop="location" width="120" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.location || '-' }}</template>
          </el-table-column>
          <el-table-column label="轨迹" prop="description" min-width="340" show-overflow-tooltip />
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="logisticsEditable" type="primary" @click="submitLogistics">保 存</el-button>
        <el-button v-if="!logisticsEditable" icon="el-icon-refresh" @click="queryLogisticsTrace(logisticsForm.deliveryOrderId)">刷 新</el-button>
        <el-button v-if="!logisticsEditable && logisticsUrl" type="primary" plain @click="openLogisticsWindow">外部查询</el-button>
        <el-button @click="logisticsOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDelivery, getDelivery, addDelivery, updateDelivery, delDelivery, confirmDelivery, cancelDelivery, updateDeliveryLogistics, queryDeliveryLogistics } from "@/api/erp/delivery"
import { optionselectWarehouse } from "@/api/erp/warehouse"
import { listSales, getSales } from "@/api/erp/sales"
import { listInventory } from "@/api/erp/inventory"

export default {
  name: "ErpDelivery",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0, open: false, title: "",
      orderList: [], warehouseOptions: [], statuses: ["草稿", "已确认", "已发货", "已取消"],
      queryParams: { pageNum: 1, pageSize: 10, deliveryOrderNo: undefined, salesOrderNo: undefined, customerName: undefined, deliveryStatus: undefined },
      form: {}, rules: { warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }], deliveryDate: [{ required: true, message: "发货日期不能为空", trigger: "change" }] },
      salesOpen: false, salesLoading: false, salesList: [], salesTotal: 0, salesQuery: { pageNum: 1, pageSize: 10, salesOrderNo: undefined, customerName: undefined },
      salesDetailOptions: [],
      logisticsOpen: false, logisticsEditable: false, logisticsLoading: false, logisticsForm: {}, logisticsResult: {}, logisticsUrl: "", logisticsRules: { trackingNo: [{ required: true, message: "物流单号不能为空", trigger: "blur" }] },
      inventoryOpen: false, inventoryLoading: false, inventoryList: [], inventoryTotal: 0, inventoryRowIndex: -1, inventoryQuery: { pageNum: 1, pageSize: 10, warehouseId: undefined, itemType: "成衣", itemCode: undefined, itemName: undefined, batchNo: undefined }
    }
  },
  created() { this.getWarehouses(); this.getList() },
  methods: {
    getWarehouses() { optionselectWarehouse().then(res => { this.warehouseOptions = res.data || [] }) },
    getList() { this.loading = true; listDelivery(this.queryParams).then(res => { this.orderList = res.rows; this.total = res.total; this.loading = false }) },
    reset() { this.form = { deliveryOrderNo: undefined, salesOrderId: undefined, salesOrderNo: undefined, customerId: undefined, customerName: undefined, warehouseId: undefined, warehouseName: undefined, deliveryDate: this.parseTime(new Date(), "{y}-{m}-{d}"), totalQty: 0, deliveryStatus: "草稿", detailList: [] }; this.salesDetailOptions = []; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.deliveryOrderId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "新增发货单" },
    handleUpdate(row) { this.reset(); getDelivery(row.deliveryOrderId || this.ids).then(res => { this.form = { ...res.data, detailList: res.data.detailList || [] }; this.loadSalesDetails(this.form.salesOrderId); this.open = true; this.title = "发货单" }) },
    canEditForm() { return !this.form.deliveryOrderId || this.form.deliveryStatus === "草稿" },
    handleWarehouseChange(value) { const item = this.warehouseOptions.find(item => item.warehouseId === value); this.form.warehouseName = item ? item.warehouseName : undefined; this.form.detailList = [] },
    addDetail() { this.form.detailList.push({ salesDetailId: undefined, inventoryId: undefined, deliveryQty: 0, stockQty: 0, lockedQty: 0, availableQty: 0 }) },
    removeDetail(index) { this.form.detailList.splice(index, 1) },
    submitForm() { this.$refs["form"].validate(valid => { if (!valid) return; const req = this.form.deliveryOrderId ? updateDelivery(this.form) : addDelivery(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.deliveryOrderId || this.ids; this.$modal.confirm('确认删除发货单编号为"' + ids + '"的数据项？').then(() => delDelivery(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) },
    handleConfirm(row) { this.$modal.confirm('确认发货单"' + row.deliveryOrderNo + '"并锁定库存、生成销售出库单？').then(() => confirmDelivery(row.deliveryOrderId)).then(() => { this.open = false; this.getList(); this.$modal.msgSuccess("确认成功，销售出库单已生成") }).catch(() => {}) },
    handleCancelDelivery(row) { this.$modal.confirm('确认取消发货单"' + row.deliveryOrderNo + '"？系统将释放锁定库存。').then(() => cancelDelivery(row.deliveryOrderId)).then(() => { this.open = false; this.getList(); this.$modal.msgSuccess("取消成功") }).catch(() => {}) },
    openLogisticsInfo(row) {
      this.logisticsResult = {}
      getDelivery(row.deliveryOrderId).then(res => {
        this.logisticsForm = res.data || {}
        this.logisticsEditable = false
        this.logisticsUrl = this.buildLogisticsUrl(this.logisticsForm.trackingNo)
        this.logisticsOpen = true
        this.queryLogisticsTrace(this.logisticsForm.deliveryOrderId)
      })
    },
    openLogisticsEdit(row) {
      this.logisticsResult = {}
      this.logisticsUrl = ""
      getDelivery(row.deliveryOrderId).then(res => { this.logisticsForm = res.data || {}; this.logisticsEditable = true; this.logisticsOpen = true })
    },
    queryLogisticsTrace(deliveryOrderId) {
      if (!deliveryOrderId) return
      this.logisticsLoading = true
      queryDeliveryLogistics(deliveryOrderId).then(res => {
        this.logisticsResult = res.data || {}
      }).finally(() => {
        this.logisticsLoading = false
      })
    },
    formatLogisticsMessage() {
      const result = this.logisticsResult || {}
      const stateName = result.stateName ? "物流状态：" + result.stateName + "；" : ""
      return stateName + (result.message || "")
    },
    buildLogisticsUrl(trackingNo) {
      if (!trackingNo) return ""
      return "https://www.kuaidi100.com/chaxun?com=auto&nu=" + encodeURIComponent(trackingNo)
    },
    openLogisticsWindow() {
      if (this.logisticsUrl) window.open(this.logisticsUrl, "_blank")
    },
    submitLogistics() { this.$refs["logisticsForm"].validate(valid => { if (!valid) return; updateDeliveryLogistics({ deliveryOrderId: this.logisticsForm.deliveryOrderId, logisticsCompany: this.logisticsForm.logisticsCompany, trackingNo: this.logisticsForm.trackingNo }).then(() => { this.$modal.msgSuccess("物流信息已保存"); this.logisticsOpen = false; this.getList() }) }) },
    openSalesSelector() { this.salesOpen = true; this.getSalesList() },
    getSalesList() { this.salesLoading = true; listSales(this.salesQuery).then(res => { this.salesList = res.rows; this.salesTotal = res.total; this.salesLoading = false }) },
    selectSales(row) { getSales(row.salesOrderId).then(res => { const order = res.data || {}; this.form.salesOrderId = order.salesOrderId; this.form.salesOrderNo = order.salesOrderNo; this.form.customerId = order.customerId; this.form.customerName = order.customerName; this.salesDetailOptions = order.detailList || []; this.salesOpen = false }) },
    loadSalesDetails(salesOrderId) { if (!salesOrderId) return; getSales(salesOrderId).then(res => { this.salesDetailOptions = (res.data && res.data.detailList) || [] }) },
    formatSalesDetail(item) { return [item.skuCode, item.styleName, item.colorName, item.sizeName, item.orderQty].filter(Boolean).join(" / ") },
    handleSalesDetailChange(row, id) { const item = this.salesDetailOptions.find(item => item.salesDetailId === id); if (item && !row.deliveryQty) this.$set(row, "deliveryQty", item.orderQty || 0) },
    openInventorySelector(index) { this.inventoryRowIndex = index; this.inventoryQuery.warehouseId = this.form.warehouseId; this.inventoryOpen = true; this.getInventoryList() },
    getInventoryList() { this.inventoryLoading = true; listInventory(this.inventoryQuery).then(res => { this.inventoryList = res.rows; this.inventoryTotal = res.total; this.inventoryLoading = false }) },
    selectInventory(item) {
      const row = this.form.detailList[this.inventoryRowIndex]
      Object.assign(row, { inventoryId: item.inventoryId, skuId: item.itemId, skuCode: item.itemCode, styleNo: item.itemCode, styleName: item.itemName, colorName: item.colorName, sizeName: item.sizeName, batchNo: item.batchNo, stockQty: item.availableQty || 0, lockedQty: item.lockedQty || 0, availableQty: this.inventoryAvailable(item), unitName: item.unitName, unitPrice: item.unitPrice || 0 })
      if (!row.deliveryQty || row.deliveryQty > row.availableQty) this.$set(row, "deliveryQty", row.availableQty)
      this.inventoryOpen = false
    },
    calcAvailable(row) { return Math.max(Number(row.availableQty !== undefined ? row.availableQty : ((row.stockQty || 0) - (row.lockedQty || 0))), 0) },
    inventoryAvailable(row) { return Math.max(Number(row.availableQty || 0) - Number(row.lockedQty || 0), 0) },
    handleExport() { this.download('erp/delivery/export', this.queryParams, `delivery_${new Date().getTime()}.xlsx`) }
  }
}
</script>
