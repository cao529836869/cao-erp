<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="物料编码" prop="materialCode">
        <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="物料名称" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="物料类型" prop="materialType">
        <el-select v-model="queryParams.materialType" placeholder="请选择物料类型" clearable>
          <el-option v-for="item in materialTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="物料分类" prop="categoryName">
        <el-input v-model="queryParams.categoryName" placeholder="请输入物料分类" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="物料状态" clearable>
          <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['erp:material:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['erp:material:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['erp:material:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['erp:material:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
      class="erp-table"
      v-loading="loading"
      :data="materialList"
      :row-class-name="materialRowClassName"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="物料编码" align="center" prop="materialCode" width="170">
        <template slot-scope="scope">
          <span class="erp-code-text">{{ scope.row.materialCode || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物料名称" align="left" prop="materialName" width="260" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span class="erp-name-text">{{ scope.row.materialName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物料类型" align="center" prop="materialType" width="120">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpToneClass(scope.row.materialType)]">
            {{ scope.row.materialType || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="物料分类" align="center" prop="categoryName" width="140">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpToneClass(scope.row.categoryName)]">
            {{ scope.row.categoryName || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="库存单位" align="center" prop="unitName" width="90" />
      <el-table-column label="默认供应商" align="left" prop="defaultSupplierName" min-width="240" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span class="erp-muted-strong">{{ scope.row.defaultSupplierName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前库存" align="right" prop="currentStockQty" width="110">
        <template slot-scope="scope">
          <span :class="{ 'stock-danger-text': isLowStock(scope.row) }">{{ formatQty(scope.row.currentStockQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="安全库存" align="right" prop="safeStockQty" width="110">
        <template slot-scope="scope">
          <span :class="{ 'stock-danger-text': isLowStock(scope.row) }">{{ formatQty(scope.row.safeStockQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpStatusToneClass(selectDictLabel(dict.type.sys_normal_disable, scope.row.status))]">
            {{ selectDictLabel(dict.type.sys_normal_disable, scope.row.status) || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['erp:material:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:material:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="960px" append-to-body class="material-form-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="96px" class="material-form">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="物料编码" prop="materialCode">
                  <el-input v-model="form.materialCode" placeholder="可自动生成">
                    <el-button slot="append" @click="handleGenerateCode">生成</el-button>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="物料名称" prop="materialName">
                  <el-input v-model="form.materialName" placeholder="请输入物料名称" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="物料类型" prop="materialType">
                  <el-select v-model="form.materialType" placeholder="请选择物料类型" style="width: 100%">
                    <el-option v-for="item in materialTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="物料分类" prop="categoryName">
                  <el-input v-model="form.categoryName" placeholder="如棉毛布、拉链、吊牌" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="库存单位" prop="unitName">
                  <el-select v-model="form.unitName" placeholder="请选择库存单位" filterable allow-create default-first-option style="width: 100%">
                    <el-option v-for="item in unitOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="默认供应商" prop="defaultSupplierName">
                  <el-input v-model="form.defaultSupplierName" placeholder="请输入默认供应商" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="安全库存" prop="safeStockQty">
                  <el-input-number v-model="form.safeStockQty" controls-position="right" :min="0" :precision="3" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="当前库存">
                  <el-input :value="formatQty(form.currentStockQty)" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入内容" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="SKU" name="sku">
            <div class="sub-toolbar">
              <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addSkuRow">新增SKU</el-button>
            </div>
            <el-table :data="form.skuList" size="mini" border class="sub-table">
              <el-table-column label="SKU编码" min-width="170">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.materialSkuCode" placeholder="不填自动生成" />
                </template>
              </el-table-column>
              <el-table-column label="颜色" min-width="110">
                <template slot-scope="scope"><el-input v-model="scope.row.colorName" placeholder="颜色" /></template>
              </el-table-column>
              <el-table-column label="颜色编码" min-width="100">
                <template slot-scope="scope"><el-input v-model="scope.row.colorCode" placeholder="编码" /></template>
              </el-table-column>
              <el-table-column label="规格" min-width="130">
                <template slot-scope="scope"><el-input v-model="scope.row.specName" placeholder="幅宽/型号" /></template>
              </el-table-column>
              <el-table-column label="幅宽" min-width="100">
                <template slot-scope="scope"><el-input-number v-model="scope.row.widthValue" :min="0" :precision="2" controls-position="right" style="width: 100%" /></template>
              </el-table-column>
              <el-table-column label="克重" min-width="100">
                <template slot-scope="scope"><el-input-number v-model="scope.row.gramWeight" :min="0" :precision="2" controls-position="right" style="width: 100%" /></template>
              </el-table-column>
              <el-table-column label="单位" min-width="90">
                <template slot-scope="scope"><el-input v-model="scope.row.unitName" placeholder="单位" /></template>
              </el-table-column>
              <el-table-column label="条码" min-width="130">
                <template slot-scope="scope"><el-input v-model="scope.row.barcode" placeholder="条码" /></template>
              </el-table-column>
              <el-table-column label="状态" min-width="100">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.status" style="width: 100%">
                    <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" icon="el-icon-delete" @click="removeSkuRow(scope.$index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMaterial, getMaterial, delMaterial, addMaterial, updateMaterial, generateMaterialCode } from "@/api/erp/material"

export default {
  name: "ErpMaterial",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      materialList: [],
      title: "",
      open: false,
      activeTab: "basic",
      dateRange: [],
      materialTypeOptions: [
        { label: "面料", value: "面料" },
        { label: "辅料", value: "辅料" },
        { label: "包装材料", value: "包装材料" }
      ],
      unitOptions: ["米", "公斤", "个", "张", "件", "码", "卷", "包"],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        materialCode: undefined,
        materialName: undefined,
        materialType: undefined,
        categoryName: undefined,
        status: undefined
      },
      form: {},
      rules: {
        materialName: [
          { required: true, message: "物料名称不能为空", trigger: "blur" }
        ],
        materialType: [
          { required: true, message: "物料类型不能为空", trigger: "change" }
        ],
        unitName: [
          { required: true, message: "库存单位不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMaterial(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.materialList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        materialId: undefined,
        materialCode: undefined,
        materialName: undefined,
        materialType: "面料",
        categoryName: undefined,
        unitName: "米",
        defaultSupplierId: undefined,
        defaultSupplierName: undefined,
        safeStockQty: 0,
        currentStockQty: 0,
        status: "0",
        remark: undefined,
        skuList: []
      }
      this.activeTab = "basic"
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.materialId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加物料档案"
    },
    handleUpdate(row) {
      this.reset()
      const materialId = row.materialId || this.ids
      getMaterial(materialId).then(response => {
        const data = response.data || {}
        this.form = {
          ...data,
          status: this.normalizeStatus(data.status, "0"),
          skuList: (data.skuList || []).map(item => ({
            ...item,
            status: this.normalizeStatus(item.status, "0")
          }))
        }
        this.open = true
        this.title = "修改物料档案"
      })
    },
    normalizeStatus(value, defaultValue) {
      return value === null || value === undefined || value === "" ? defaultValue : String(value)
    },
    formatQty(value) {
      const num = Number(value || 0)
      return num.toFixed(3).replace(/\.?0+$/, "")
    },
    numberValue(value) {
      const number = Number(value)
      return Number.isNaN(number) ? 0 : number
    },
    isLowStock(row) {
      return this.numberValue(row.safeStockQty) > 0 && this.numberValue(row.currentStockQty) < this.numberValue(row.safeStockQty)
    },
    materialRowClassName({ row }) {
      return this.isLowStock(row) ? "low-stock-row" : ""
    },
    handleGenerateCode() {
      generateMaterialCode({
        materialType: this.form.materialType,
        categoryName: this.form.categoryName
      }).then(response => {
        this.$set(this.form, "materialCode", response.materialCode)
      })
    },
    addSkuRow() {
      if (!this.form.skuList) {
        this.$set(this.form, "skuList", [])
      }
      this.form.skuList.push({
        materialSkuCode: undefined,
        colorCode: undefined,
        colorName: undefined,
        specName: undefined,
        widthValue: undefined,
        gramWeight: undefined,
        unitName: this.form.unitName,
        barcode: undefined,
        status: "0",
        remark: undefined
      })
    },
    removeSkuRow(index) {
      this.form.skuList.splice(index, 1)
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.materialId != undefined) {
            updateMaterial(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addMaterial(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const materialIds = row.materialId || this.ids
      this.$modal.confirm('是否确认删除物料档案编号为"' + materialIds + '"的数据项？').then(function() {
        return delMaterial(materialIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('erp/material/export', this.addDateRange({
        ...this.queryParams
      }, this.dateRange), `material_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
::v-deep .material-form-dialog .el-dialog__body {
  padding: 18px 24px 4px !important;
}

.material-form {
  padding-right: 6px;
}

.material-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.sub-toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}

.sub-table ::v-deep .el-input__inner {
  height: 28px;
  line-height: 28px;
  padding: 0 8px;
}

::v-deep .low-stock-row > td {
  background: #fff5f5 !important;
}

::v-deep .low-stock-row:hover > td {
  background: #ffecec !important;
}

.stock-danger-text {
  color: #c03535;
  font-weight: 700;
}

</style>
