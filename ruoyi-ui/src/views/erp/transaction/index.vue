<template>
  <div class="app-container transaction-page">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="80px"
    >
      <el-form-item label="流水编号" prop="transactionNo">
        <el-input v-model="queryParams.transactionNo" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="业务单号" prop="businessNo">
        <el-input v-model="queryParams.businessNo" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="流水类型" prop="transactionType">
        <el-input v-model="queryParams.transactionType" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="编码" prop="itemCode">
        <el-input v-model="queryParams.itemCode" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="批次" prop="batchNo">
        <el-input v-model="queryParams.batchNo" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['erp:transaction:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="transactionList">
      <el-table-column label="流水编号" align="center" prop="transactionNo" width="200">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.transactionNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="transactionType" width="120">
        <template slot-scope="scope">
          <el-tag
            size="mini"
            effect="plain"
            :type="transactionTagType(scope.row)"
            :class="['transaction-type-tag', transactionTone(scope.row)]"
          >
            {{ scope.row.transactionType || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务单号" align="center" prop="businessNo" width="190">
        <template slot-scope="scope">
          <span
            v-if="canOpenBusiness(scope.row.businessNo)"
            class="erp-code-link business-link"
            role="button"
            tabindex="0"
            @click="openBusiness(scope.row.businessNo)"
            @keydown.enter.prevent="openBusiness(scope.row.businessNo)"
          >
            {{ scope.row.businessNo }}
          </span>
          <span v-else class="erp-code-text">{{ scope.row.businessNo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseName" width="140">
        <template slot-scope="scope">
          <span :class="['erp-chip', 'erp-warehouse-pill', warehouseTone(scope.row.warehouseName)]">
            {{ scope.row.warehouseName || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="编码" align="center" prop="itemCode" width="190">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.itemCode || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="名称" align="left" prop="itemName" min-width="200" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.itemName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="批次" align="center" prop="batchNo" width="110" />
      <el-table-column label="入库" align="right" prop="inQty" width="90">
        <template slot-scope="scope">
          <span :class="{ 'qty-in': numberValue(scope.row.inQty) > 0 }">{{ scope.row.inQty }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出库" align="right" prop="outQty" width="90">
        <template slot-scope="scope">
          <span :class="{ 'qty-out': numberValue(scope.row.outQty) > 0 }">{{ scope.row.outQty }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结存" align="right" prop="balanceQty" width="90" />
      <el-table-column label="单位" align="center" prop="unitName" width="70" />
      <el-table-column label="操作人" align="center" prop="operatorName" width="100" />
      <el-table-column label="发生时间" align="center" prop="transactionTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.transactionTime) }}</template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listTransaction } from "@/api/erp/inventory"

export default {
  name: "ErpTransaction",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      transactionList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        transactionNo: undefined,
        businessNo: undefined,
        transactionType: undefined,
        warehouseId: undefined,
        itemType: undefined,
        itemCode: undefined,
        itemCodeExact: undefined,
        batchNo: undefined
      }
    }
  },
  created() {
    this.initQueryFromRoute()
    this.getList()
  },
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
      this.queryParams.transactionNo = undefined
      this.queryParams.businessNo = undefined
      this.queryParams.transactionType = undefined
      this.queryParams.warehouseId = undefined
      this.queryParams.itemType = undefined
      this.queryParams.itemCode = query.itemCode || undefined
      this.queryParams.itemCodeExact = query.itemCodeExact || undefined
      this.queryParams.batchNo = undefined
      this.queryParams.pageNum = 1
    },
    getList() {
      this.loading = true
      listTransaction(this.queryParams).then(res => {
        this.transactionList = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.queryParams.warehouseId = undefined
      this.queryParams.itemType = undefined
      this.queryParams.itemCodeExact = undefined
      this.queryParams.batchNo = undefined
      if (Object.keys(this.$route.query || {}).length > 0) {
        this.$router.replace({ name: this.$route.name, query: {} }).catch(() => {})
        return
      }
      this.handleQuery()
    },
    handleExport() {
      this.download("erp/transaction/export", this.queryParams, `transaction_${new Date().getTime()}.xlsx`)
    },
    numberValue(value) {
      const number = Number(value)
      return Number.isNaN(number) ? 0 : number
    },
    transactionTone(row) {
      const type = row.transactionType || ""
      if (type.indexOf("取消") === 0) {
        return "is-cancel"
      }
      if (this.numberValue(row.inQty) > 0) {
        return "is-in"
      }
      if (this.numberValue(row.outQty) > 0) {
        return "is-out"
      }
      return "is-normal"
    },
    transactionTagType(row) {
      const tone = this.transactionTone(row)
      if (tone === "is-in") return "success"
      if (tone === "is-out") return "warning"
      if (tone === "is-cancel") return "info"
      return ""
    },
    warehouseTone(name) {
      const tones = ["tone-green", "tone-blue", "tone-amber", "tone-violet", "tone-cyan"]
      const text = name || ""
      let hash = 0
      for (let i = 0; i < text.length; i++) {
        hash += text.charCodeAt(i)
      }
      return tones[hash % tones.length]
    },
    canOpenBusiness(businessNo) {
      return /^CK/.test(businessNo || "") || /^RK/.test(businessNo || "")
    },
    openBusiness(businessNo) {
      if (/^CK/.test(businessNo)) {
        this.openRouteByNameOrComponent("Outbound", "erp/outbound/index", {
          outboundOrderNo: businessNo,
          openDetail: "1"
        })
        return
      }
      if (/^RK/.test(businessNo)) {
        this.openRouteByNameOrComponent("Inbound", "erp/inbound/index", {
          inboundOrderNo: businessNo,
          openDetail: "1"
        })
      }
    },
    openRouteByNameOrComponent(routeName, componentPath, query) {
      const target = this.findRouteByComponent(this.$router.options.routes || [], componentPath)
      if (target && target.name) {
        this.$router.push({ name: target.name, query })
        return
      }
      this.$router.push({ name: routeName, query })
    },
    findRouteByComponent(routes, componentPath) {
      for (const route of routes || []) {
        const file = route.component && route.component.toString ? route.component.toString() : ""
        if (route.name && file.indexOf(componentPath) !== -1) {
          return route
        }
        const child = this.findRouteByComponent(route.children || [], componentPath)
        if (child) {
          return child
        }
      }
      return null
    }
  }
}
</script>

<style scoped>
.transaction-page ::v-deep .el-table .cell {
  line-height: 22px;
}

.transaction-type-tag {
  min-width: 72px;
  border-radius: 4px;
  font-weight: 600;
}

.transaction-type-tag.is-in {
  color: #1f7a4f;
  border-color: #9fd8bc;
  background: #edf8f2;
}

.transaction-type-tag.is-out {
  color: #9a5a00;
  border-color: #f0c36d;
  background: #fff7e6;
}

.transaction-type-tag.is-cancel {
  color: #5d6470;
  border-color: #c7ccd4;
  background: #f4f6f8;
}

.business-link {
  justify-content: center;
  font-weight: 600;
  white-space: nowrap;
}

.erp-warehouse-pill {
  display: inline-flex;
  align-items: center;
  max-width: 118px;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 4px;
  font-weight: 600;
  line-height: 22px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tone-green {
  color: #28684d;
  background: #eaf6ef;
  border: 1px solid #b8dcc8;
}

.tone-blue {
  color: #245d8f;
  background: #eaf3fb;
  border: 1px solid #b7d2ea;
}

.tone-amber {
  color: #845b18;
  background: #fff5df;
  border: 1px solid #e7c989;
}

.tone-violet {
  color: #5c4b8f;
  background: #f2effb;
  border: 1px solid #c9bee8;
}

.tone-cyan {
  color: #206a73;
  background: #e8f7f8;
  border: 1px solid #acdadd;
}

.qty-in {
  color: #1f7a4f;
  font-weight: 600;
}

.qty-out {
  color: #9a5a00;
  font-weight: 600;
}
</style>
