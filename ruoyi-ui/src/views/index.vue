<template>
  <div class="erp-home">
    <section class="hero-panel">
      <div>
        <p class="eyebrow">一丫一 ERP 业务看板</p>
        <h1>订单、生产、出入库与库存的实时总览</h1>
        <p class="hero-copy">
          数据来自当前系统业务单据和库存流水，按今日发生、未完结任务、库存风险和近期计划汇总。
        </p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" icon="el-icon-refresh" :loading="loading" @click="getOverview">刷新数据</el-button>
      </div>
    </section>

    <el-row :gutter="16" class="metric-grid">
      <el-col v-for="item in metrics" :key="item.title" :xs="24" :sm="12" :lg="6">
        <div class="metric-card" :class="item.tone" v-loading="loading">
          <div class="metric-icon">
            <i :class="item.icon" />
          </div>
          <div class="metric-body">
            <span>{{ item.title }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.note }}</small>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <div class="panel" v-loading="loading">
          <div class="panel-header">
            <div>
              <h2>近 7 天出入库趋势</h2>
              <p>按库存流水发生时间统计入库数量和出库数量</p>
            </div>
          </div>
          <el-empty v-if="!weeklyFlow.length" description="暂无库存流水数据" />
          <template v-else>
            <div class="trend-chart">
              <div v-for="day in weeklyFlow" :key="day.name" class="trend-column">
                <div class="bars">
                  <span class="bar inbound" :style="{ height: day.inboundHeight + '%' }" />
                  <span class="bar outbound" :style="{ height: day.outboundHeight + '%' }" />
                </div>
                <span class="day">{{ day.name }}</span>
                <small>{{ formatQty(day.inboundQty) }}/{{ formatQty(day.outboundQty) }}</small>
              </div>
            </div>
            <div class="chart-legend">
              <span><i class="inbound" />入库</span>
              <span><i class="outbound" />出库</span>
            </div>
          </template>
        </div>
      </el-col>

      <el-col :xs="24" :lg="8">
        <div class="panel" v-loading="loading">
          <div class="panel-header compact">
            <div>
              <h2>待处理事项</h2>
              <p>根据逾期裁剪和待过账单据生成</p>
            </div>
          </div>
          <div class="todo-list">
            <div v-for="todo in todos" :key="todo.title" class="todo-item">
              <el-tag :type="todo.type" size="mini">{{ todo.level }}</el-tag>
              <div>
                <strong>{{ todo.title }}</strong>
                <span>{{ todo.desc }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <div class="panel" v-loading="loading">
          <div class="panel-header compact">
            <div>
              <h2>物料库存预警</h2>
              <p>低于安全库存的物料</p>
            </div>
          </div>
          <el-empty v-if="!materialAlerts.length" description="暂无低库存物料" />
          <div v-else class="stock-list">
            <div v-for="stock in materialAlerts" :key="stock.materialCode" class="stock-item">
              <div class="stock-title">
                <span>{{ stock.materialName }}</span>
                <strong>{{ formatQty(stock.currentQty) }}/{{ formatQty(stock.safeQty) }} {{ stock.unitName }}</strong>
              </div>
              <el-progress :percentage="stock.percent" :color="stockColor(stock.percent)" :show-text="false" />
              <small>{{ stock.materialCode }}</small>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="8">
        <div class="panel" v-loading="loading">
          <div class="panel-header compact">
            <div>
              <h2>近期销售订单</h2>
              <p>按订单日期倒序展示</p>
            </div>
          </div>
          <el-table :data="recentSalesOrders" size="mini" class="erp-table">
            <el-table-column prop="salesOrderNo" label="销售单号" min-width="130" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户" min-width="130" show-overflow-tooltip />
            <el-table-column prop="totalQty" label="数量" width="80" align="right">
              <template slot-scope="scope">{{ formatQty(scope.row.totalQty) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="86">
              <template slot-scope="scope">
                <el-tag :type="statusType(scope.row.orderStatus)" size="mini">{{ scope.row.orderStatus || '-' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :xs="24" :lg="8">
        <div class="panel" v-loading="loading">
          <div class="panel-header compact">
            <div>
              <h2>生产状态分布</h2>
              <p>当前生产订单状态数量</p>
            </div>
          </div>
          <el-empty v-if="!productionStatus.length" description="暂无生产订单" />
          <div v-else class="status-list">
            <div v-for="item in productionStatus" :key="item.name" class="status-item">
              <span>{{ item.name || '未设置' }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div class="panel" v-loading="loading">
          <div class="panel-header compact">
            <div>
              <h2>仓库库存概览</h2>
              <p>按仓库汇总可用、锁定和库存金额</p>
            </div>
          </div>
          <el-table :data="warehouseStock" size="small" class="erp-table">
            <el-table-column prop="warehouseName" label="仓库" min-width="140" />
            <el-table-column prop="availableQty" label="可用" width="100" align="right">
              <template slot-scope="scope">{{ formatQty(scope.row.availableQty) }}</template>
            </el-table-column>
            <el-table-column prop="lockedQty" label="锁定" width="100" align="right">
              <template slot-scope="scope">{{ formatQty(scope.row.lockedQty) }}</template>
            </el-table-column>
            <el-table-column prop="stockAmount" label="金额" width="120" align="right">
              <template slot-scope="scope">{{ formatMoney(scope.row.stockAmount) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :xs="24" :lg="12">
        <div class="panel" v-loading="loading">
          <div class="panel-header compact">
            <div>
              <h2>生产与裁剪计划</h2>
              <p>最近需要关注的未完成任务</p>
            </div>
          </div>
          <el-table :data="schedule" size="small" class="erp-table">
            <el-table-column prop="planDate" label="日期" width="100" />
            <el-table-column prop="module" label="环节" width="80" />
            <el-table-column prop="orderNo" label="单号" min-width="140" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户" min-width="120" show-overflow-tooltip />
            <el-table-column prop="progress" label="进度" width="140">
              <template slot-scope="scope">
                <el-progress :percentage="numberValue(scope.row.progress)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template slot-scope="scope">
                <el-tag :type="statusType(scope.row.status)" size="mini">{{ scope.row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboardOverview } from "@/api/erp/dashboard"

export default {
  name: "Index",
  data() {
    return {
      loading: false,
      summary: {},
      weeklyFlow: [],
      productionStatus: [],
      materialAlerts: [],
      recentSalesOrders: [],
      schedule: [],
      warehouseStock: []
    }
  },
  computed: {
    metrics() {
      return [
        {
          title: "今日入库",
          value: `${this.formatQty(this.summary.todayInboundQty)} 件`,
          note: `今日出库 ${this.formatQty(this.summary.todayOutboundQty)} 件`,
          icon: "el-icon-box",
          tone: "blue"
        },
        {
          title: "在产订单",
          value: `${this.formatQty(this.summary.activeProductionCount)} 单`,
          note: `逾期裁剪 ${this.formatQty(this.summary.overdueCutCount)} 单`,
          icon: "el-icon-s-order",
          tone: "green"
        },
        {
          title: "库存金额",
          value: this.formatMoney(this.summary.inventoryValue),
          note: `成衣库存 ${this.formatQty(this.summary.finishedGoodsQty)} 件`,
          icon: "el-icon-coin",
          tone: "amber"
        },
        {
          title: "待过账单据",
          value: `${this.formatQty(this.pendingDocumentCount)} 单`,
          note: `入库 ${this.formatQty(this.summary.pendingInboundCount)} / 出库 ${this.formatQty(this.summary.pendingOutboundCount)}`,
          icon: "el-icon-warning-outline",
          tone: "red"
        }
      ]
    },
    pendingDocumentCount() {
      return this.numberValue(this.summary.pendingInboundCount) + this.numberValue(this.summary.pendingOutboundCount)
    },
    todos() {
      return [
        {
          level: "逾期",
          title: `${this.formatQty(this.summary.overdueCutCount)} 张裁剪单已超计划`,
          desc: "请到裁剪单列表按超时排序处理",
          type: this.numberValue(this.summary.overdueCutCount) > 0 ? "danger" : "success"
        },
        {
          level: "入库",
          title: `${this.formatQty(this.summary.pendingInboundCount)} 张入库单待确认`,
          desc: "草稿或已审核入库单需要人工确认过账",
          type: this.numberValue(this.summary.pendingInboundCount) > 0 ? "warning" : "success"
        },
        {
          level: "出库",
          title: `${this.formatQty(this.summary.pendingOutboundCount)} 张出库单待处理`,
          desc: "草稿、已审核或已拣货出库单需要继续流转",
          type: this.numberValue(this.summary.pendingOutboundCount) > 0 ? "warning" : "success"
        },
        {
          level: "库存",
          title: `${this.materialAlerts.length} 项物料低于安全库存`,
          desc: "建议优先查看低库存物料和采购补货",
          type: this.materialAlerts.length > 0 ? "danger" : "success"
        }
      ]
    }
  },
  created() {
    this.getOverview()
  },
  methods: {
    getOverview() {
      this.loading = true
      getDashboardOverview().then(res => {
        const data = res.data || {}
        this.summary = data.summary || {}
        this.productionStatus = data.productionStatus || []
        this.materialAlerts = (data.materialAlerts || []).map(item => ({
          ...item,
          percent: Math.max(0, Math.min(100, this.numberValue(item.percent)))
        }))
        this.recentSalesOrders = data.recentSalesOrders || []
        this.schedule = (data.schedule || []).map(item => ({
          ...item,
          planDate: this.parseTime(item.planDate, "{m}-{d}")
        }))
        this.warehouseStock = data.warehouseStock || []
        this.weeklyFlow = this.normalizeWeeklyFlow(data.weeklyFlow || [])
      }).finally(() => {
        this.loading = false
      })
    },
    normalizeWeeklyFlow(list) {
      const max = Math.max(
        1,
        ...list.map(item => this.numberValue(item.inboundQty)),
        ...list.map(item => this.numberValue(item.outboundQty))
      )
      return list.map(item => ({
        ...item,
        inboundQty: this.numberValue(item.inboundQty),
        outboundQty: this.numberValue(item.outboundQty),
        inboundHeight: Math.max(8, Math.round(this.numberValue(item.inboundQty) / max * 100)),
        outboundHeight: Math.max(8, Math.round(this.numberValue(item.outboundQty) / max * 100))
      }))
    },
    numberValue(value) {
      const number = Number(value)
      return Number.isNaN(number) ? 0 : number
    },
    formatQty(value) {
      return this.numberValue(value).toLocaleString("zh-CN", { maximumFractionDigits: 3 })
    },
    formatMoney(value) {
      return `¥${this.numberValue(value).toLocaleString("zh-CN", { maximumFractionDigits: 2 })}`
    },
    stockColor(percent) {
      if (percent < 50) return "#d9576a"
      if (percent < 80) return "#c47a00"
      return "#238e82"
    },
    statusType(status) {
      if (!status) return "info"
      if (status.indexOf("完成") !== -1 || status.indexOf("入库") !== -1 || status.indexOf("出库") !== -1) return "success"
      if (status.indexOf("取消") !== -1 || status.indexOf("关闭") !== -1 || status.indexOf("作废") !== -1) return "info"
      if (status.indexOf("草稿") !== -1 || status.indexOf("待") !== -1) return "warning"
      return ""
    }
  }
}
</script>

<style lang="scss" scoped>
.erp-home {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: #f6f8fb;
  color: #1f2d3d;
}

.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 156px;
  margin-bottom: 16px;
  padding: 28px 32px;
  border-radius: 8px;
  background: linear-gradient(120deg, #1c5b93, #238e82);
  color: #fff;
  box-shadow: 0 12px 28px rgba(31, 45, 61, 0.12);

  h1 {
    margin: 0;
    font-size: 28px;
    line-height: 1.35;
    font-weight: 700;
    letter-spacing: 0;
  }
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
}

.hero-copy {
  max-width: 760px;
  margin: 12px 0 0;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.hero-actions {
  flex-shrink: 0;
}

.metric-card,
.panel {
  margin-bottom: 16px;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(31, 45, 61, 0.05);
}

.metric-card {
  display: flex;
  align-items: center;
  min-height: 118px;
  padding: 18px;
}

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-right: 16px;
  border-radius: 8px;
  font-size: 24px;
}

.metric-card.blue .metric-icon {
  background: #e8f2ff;
  color: #1c5b93;
}

.metric-card.green .metric-icon {
  background: #e6f7f2;
  color: #238e82;
}

.metric-card.amber .metric-icon {
  background: #fff4df;
  color: #c47a00;
}

.metric-card.red .metric-icon {
  background: #fff0f2;
  color: #d9576a;
}

.metric-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;

  span {
    color: #6b778c;
    font-size: 13px;
  }

  strong {
    font-size: 24px;
    color: #17233d;
    white-space: nowrap;
  }

  small {
    color: #909399;
  }
}

.panel {
  padding: 20px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  h2 {
    margin: 0 0 6px;
    font-size: 18px;
    line-height: 1.4;
    color: #17233d;
  }

  p {
    margin: 0;
    color: #8a96a8;
    font-size: 13px;
  }
}

.panel-header.compact {
  margin-bottom: 14px;
}

.trend-chart {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 14px;
  height: 270px;
  padding: 18px 10px 4px;
  border-radius: 8px;
  background: linear-gradient(180deg, #f8fafc 0, #fff 100%);
}

.trend-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  min-width: 0;

  small {
    margin-top: 4px;
    color: #8a96a8;
    font-size: 12px;
  }
}

.bars {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  width: 100%;
  height: 198px;
}

.bar {
  width: 16px;
  min-height: 10px;
  border-radius: 6px 6px 2px 2px;
}

.bar.inbound,
.chart-legend i.inbound {
  background: #238e82;
}

.bar.outbound,
.chart-legend i.outbound {
  background: #c47a00;
}

.day {
  margin-top: 12px;
  color: #6b778c;
  font-size: 12px;
}

.chart-legend {
  display: flex;
  gap: 18px;
  margin-top: 12px;
  color: #6b778c;
  font-size: 13px;

  span {
    display: inline-flex;
    align-items: center;
  }

  i {
    width: 10px;
    height: 10px;
    margin-right: 6px;
    border-radius: 50%;
  }
}

.todo-list,
.stock-list,
.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #edf1f7;

  &:last-child {
    border-bottom: 0;
  }

  strong,
  span {
    display: block;
  }

  strong {
    margin-bottom: 6px;
    color: #17233d;
  }

  span {
    color: #8a96a8;
    font-size: 13px;
  }
}

.stock-item {
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;

  small {
    display: block;
    margin-top: 8px;
    color: #8a96a8;
  }
}

.stock-title,
.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stock-title {
  margin-bottom: 10px;

  span {
    color: #17233d;
  }

  strong {
    color: #6b778c;
    font-weight: 600;
  }
}

.status-item {
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;

  span {
    color: #6b778c;
  }

  strong {
    font-size: 20px;
    color: #17233d;
  }
}

.erp-table {
  width: 100%;

  ::v-deep .el-table__header th {
    background: #f8fafc;
    color: #6b778c;
    font-weight: 600;
  }
}

@media (max-width: 768px) {
  .erp-home {
    padding: 12px;
  }

  .hero-panel,
  .panel-header {
    flex-direction: column;
  }

  .hero-panel {
    align-items: flex-start;
    padding: 22px;

    h1 {
      font-size: 23px;
    }
  }

  .trend-chart {
    gap: 8px;
    overflow-x: auto;
  }

  .trend-column {
    min-width: 58px;
  }
}
</style>
