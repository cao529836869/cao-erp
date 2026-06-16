<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="客户编码" prop="customerCode">
        <el-input
          v-model="queryParams.customerCode"
          placeholder="请输入客户编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input
          v-model="queryParams.customerName"
          placeholder="请输入客户名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="请选择客户类型" clearable>
          <el-option label="品牌客户" value="品牌客户" />
          <el-option label="批发客户" value="批发客户" />
          <el-option label="门店客户" value="门店客户" />
          <el-option label="电商客户" value="电商客户" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系人" prop="contactName">
        <el-input
          v-model="queryParams.contactName"
          placeholder="请输入联系人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input
          v-model="queryParams.contactPhone"
          placeholder="请输入联系电话"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="客户状态" clearable>
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['erp:customer:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['erp:customer:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['erp:customer:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['erp:customer:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户编码" align="center" prop="customerCode" width="160">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.customerCode || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户名称" align="left" prop="customerName" width="240" :show-overflow-tooltip="true">
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.customerName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="客户类型" align="center" prop="customerType" width="120">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.customerType)]">{{ scope.row.customerType || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="联系人" align="center" prop="contactName" width="100" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
      <el-table-column label="地区" align="left" min-width="180" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span class="erp-muted-strong">{{ formatRegion(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="信用额度" align="right" prop="creditLimit" width="120" />
      <el-table-column label="结算方式" align="center" prop="settlementType" width="110" />
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
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['erp:customer:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['erp:customer:remove']"
          >删除</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户编码" prop="customerCode">
              <el-input v-model="form.customerCode" placeholder="请输入客户编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="客户类型" prop="customerType">
              <el-select v-model="form.customerType" placeholder="请选择客户类型" style="width: 100%">
                <el-option label="品牌客户" value="品牌客户" />
                <el-option label="批发客户" value="批发客户" />
                <el-option label="门店客户" value="门店客户" />
                <el-option label="电商客户" value="电商客户" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算方式" prop="settlementType">
              <el-select v-model="form.settlementType" placeholder="请选择结算方式" clearable style="width: 100%">
                <el-option label="月结" value="月结" />
                <el-option label="预付款" value="预付款" />
                <el-option label="货到付款" value="货到付款" />
                <el-option label="按批结算" value="按批结算" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="所在地区" prop="province">
              <region-cascader
                :province.sync="form.province"
                :city.sync="form.city"
                :county.sync="form.county"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="信用额度" prop="creditLimit">
              <el-input-number v-model="form.creditLimit" controls-position="right" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCustomer, getCustomer, delCustomer, addCustomer, updateCustomer } from "@/api/erp/customer"
import RegionCascader from "@/components/RegionCascader"

export default {
  name: "ErpCustomer",
  components: { RegionCascader },
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      customerList: [],
      title: "",
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        customerCode: undefined,
        customerName: undefined,
        customerType: undefined,
        contactName: undefined,
        contactPhone: undefined,
        status: undefined
      },
      form: {},
      rules: {
        customerCode: [
          { required: true, message: "客户编码不能为空", trigger: "blur" }
        ],
        customerName: [
          { required: true, message: "客户名称不能为空", trigger: "blur" }
        ],
        customerType: [
          { required: true, message: "客户类型不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询客户档案列表 */
    getList() {
      this.loading = true
      listCustomer(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.customerList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    formatRegion(row) {
      return [row.province, row.city, row.county].filter(Boolean).join(" / ")
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        customerId: undefined,
        customerCode: undefined,
        customerName: undefined,
        customerType: "品牌客户",
        contactName: undefined,
        contactPhone: undefined,
        contactEmail: undefined,
        province: undefined,
        city: undefined,
        county: undefined,
        address: undefined,
        creditLimit: 0,
        settlementType: undefined,
        status: "0",
        remark: undefined
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.customerId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加客户档案"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const customerId = row.customerId || this.ids
      getCustomer(customerId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改客户档案"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.customerId != undefined) {
            updateCustomer(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCustomer(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const customerIds = row.customerId || this.ids
      this.$modal.confirm('是否确认删除客户档案编号为"' + customerIds + '"的数据项？').then(function() {
        return delCustomer(customerIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('erp/customer/export', this.addDateRange({
        ...this.queryParams
      }, this.dateRange), `customer_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
