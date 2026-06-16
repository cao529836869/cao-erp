<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="仓库编码" prop="warehouseCode">
        <el-input v-model="queryParams.warehouseCode" placeholder="请输入仓库编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="仓库名称" prop="warehouseName">
        <el-input v-model="queryParams.warehouseName" placeholder="请输入仓库名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="仓库类型" prop="warehouseType">
        <el-select v-model="queryParams.warehouseType" placeholder="请选择仓库类型" clearable>
          <el-option v-for="item in warehouseTypes" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:warehouse:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:warehouse:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:warehouse:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="warehouseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="仓库编码" align="center" prop="warehouseCode" width="160">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.warehouseCode || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="仓库名称" align="left" prop="warehouseName" width="220" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.warehouseName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="仓库类型" align="center" prop="warehouseType" width="120">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.warehouseType)]">{{ scope.row.warehouseType || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="负责人" align="center" prop="managerName" width="110" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
      <el-table-column label="地址" align="left" prop="address" min-width="220" :show-overflow-tooltip="true">
        <template slot-scope="scope"><span class="erp-muted-strong">{{ scope.row.address || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpStatusToneClass(selectDictLabel(dict.type.sys_normal_disable, scope.row.status))]">
            {{ selectDictLabel(dict.type.sys_normal_disable, scope.row.status) || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['erp:warehouse:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:warehouse:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="仓库编码" prop="warehouseCode"><el-input v-model="form.warehouseCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库名称" prop="warehouseName"><el-input v-model="form.warehouseName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="仓库类型" prop="warehouseType"><el-select v-model="form.warehouseType" style="width:100%"><el-option v-for="item in warehouseTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="负责人"><el-input v-model="form.managerName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listWarehouse, getWarehouse, delWarehouse, addWarehouse, updateWarehouse } from "@/api/erp/warehouse"

export default {
  name: "ErpWarehouse",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0,
      warehouseList: [], title: "", open: false,
      warehouseTypes: ["物料仓", "成衣仓", "次品仓", "样品仓"],
      queryParams: { pageNum: 1, pageSize: 10, warehouseCode: undefined, warehouseName: undefined, warehouseType: undefined },
      form: {},
      rules: {
        warehouseCode: [{ required: true, message: "仓库编码不能为空", trigger: "blur" }],
        warehouseName: [{ required: true, message: "仓库名称不能为空", trigger: "blur" }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() { this.loading = true; listWarehouse(this.queryParams).then(res => { this.warehouseList = res.rows; this.total = res.total; this.loading = false }) },
    reset() { this.form = { warehouseId: undefined, warehouseCode: undefined, warehouseName: undefined, warehouseType: "物料仓", status: "0" }; this.resetForm("form") },
    cancel() { this.open = false; this.reset() },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.resetForm("queryForm"); this.handleQuery() },
    handleSelectionChange(selection) { this.ids = selection.map(item => item.warehouseId); this.single = selection.length !== 1; this.multiple = !selection.length },
    handleAdd() { this.reset(); this.open = true; this.title = "添加仓库" },
    handleUpdate(row) { this.reset(); getWarehouse(row.warehouseId || this.ids).then(res => { this.form = res.data; this.open = true; this.title = "修改仓库" }) },
    submitForm() { this.$refs["form"].validate(valid => { if (!valid) return; const req = this.form.warehouseId ? updateWarehouse(this.form) : addWarehouse(this.form); req.then(() => { this.$modal.msgSuccess("保存成功"); this.open = false; this.getList() }) }) },
    handleDelete(row) { const ids = row.warehouseId || this.ids; this.$modal.confirm('确认删除仓库编号为"' + ids + '"的数据项？').then(() => delWarehouse(ids)).then(() => { this.getList(); this.$modal.msgSuccess("删除成功") }).catch(() => {}) }
  }
}
</script>
