<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable>
          <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="库存类型" prop="itemType">
        <el-select v-model="queryParams.itemType" placeholder="请选择类型" clearable>
          <el-option label="物料" value="物料" />
          <el-option label="成衣" value="成衣" />
        </el-select>
      </el-form-item>
      <el-form-item label="编码" prop="itemCode"><el-input v-model="queryParams.itemCode" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="名称" prop="itemName"><el-input v-model="queryParams.itemName" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="批次" prop="batchNo"><el-input v-model="queryParams.batchNo" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['erp:inventory:export']">导出</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="inventoryList">
      <el-table-column label="仓库" align="center" prop="warehouseName" width="140">
        <template slot-scope="scope"><span :class="['erp-chip', 'erp-warehouse-pill', $erpToneClass(scope.row.warehouseName)]">{{ scope.row.warehouseName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="itemType" width="90">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.itemType)]">{{ scope.row.itemType || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="编码" align="center" prop="itemCode" width="190">
        <template slot-scope="scope">
          <span
            v-if="canViewTransaction"
            class="erp-code-link"
            @click="handleViewTransaction(scope.row)"
          >{{ scope.row.itemCode || '-' }}</span>
          <span v-else class="erp-code-text">{{ scope.row.itemCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="名称" align="left" prop="itemName" min-width="220" :show-overflow-tooltip="true">
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.itemName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="颜色" align="center" prop="colorName" width="100" />
      <el-table-column label="尺码" align="center" prop="sizeName" width="90" />
      <el-table-column label="规格" align="center" prop="specName" width="120" />
      <el-table-column label="批次" align="center" prop="batchNo" width="110" />
      <el-table-column label="可用库存" align="right" prop="availableQty" width="110"><template slot-scope="scope"><span class="erp-number-strong">{{ scope.row.availableQty }}</span></template></el-table-column>
      <el-table-column label="锁定库存" align="right" prop="lockedQty" width="110"><template slot-scope="scope"><span class="erp-number-strong">{{ scope.row.lockedQty }}</span></template></el-table-column>
      <el-table-column label="价格" align="right" prop="unitPrice" width="100"><template slot-scope="scope"><span class="erp-number-strong">{{ scope.row.unitPrice }}</span></template></el-table-column>
      <el-table-column label="单位" align="center" prop="unitName" width="70" />
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160"><template slot-scope="scope">{{ parseTime(scope.row.updateTime) }}</template></el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-tickets" @click="handleViewTransaction(scope.row)" v-hasPermi="['erp:transaction:list']">流水</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listInventory } from "@/api/erp/inventory"
import { optionselectWarehouse } from "@/api/erp/warehouse"

export default {
  name: "ErpInventory",
  data() {
    return { loading: true, showSearch: true, total: 0, inventoryList: [], warehouseOptions: [], queryParams: { pageNum: 1, pageSize: 10, warehouseId: undefined, itemType: undefined, itemCode: undefined, itemName: undefined, batchNo: undefined } }
  },
  computed: {
    canViewTransaction() {
      return this.$auth.hasPermi("erp:transaction:list")
    }
  },
  created() { this.getWarehouses(); this.getList() },
  methods: {
    getWarehouses() { optionselectWarehouse().then(res => { this.warehouseOptions = res.data || [] }) },
    getList() { this.loading = true; listInventory(this.queryParams).then(res => { this.inventoryList = res.rows; this.total = res.total; this.loading = false }) },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleViewTransaction(row) {
      this.$router.push({
        name: "Transaction",
        query: {
          itemCode: row.itemCode,
          itemCodeExact: "1"
        }
      })
    },
    handleExport() { this.download('erp/inventory/export', this.queryParams, `inventory_${new Date().getTime()}.xlsx`) }
  }
}
</script>

<style scoped>
.inventory-code-link {
  padding: 0;
  font-size: 12px;
}
</style>
