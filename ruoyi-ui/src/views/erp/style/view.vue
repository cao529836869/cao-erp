<template>
  <el-drawer title="童装款式主详情" :visible.sync="visible" direction="rtl" size="72%" append-to-body :before-close="handleClose" custom-class="detail-drawer">
    <div v-loading="loading" class="drawer-content">
      <h4 class="section-header">基本信息</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">款号：</label>
            <span class="info-value plaintext">
              {{ info.styleNo }}
            </span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">款式名称：</label>
            <span class="info-value plaintext">
              {{ info.styleName }}
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">品类：</label>
            <span class="info-value plaintext">
              {{ info.categoryName }}
            </span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">品牌名称：</label>
            <span class="info-value plaintext">
              {{ info.brandName }}
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">季节：</label>
            <span class="info-value plaintext">
              {{ info.seasonName }}
            </span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">年份：</label>
            <span class="info-value plaintext">
              {{ info.yearName }}
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">性别类型：</label>
            <span class="info-value plaintext">
              {{ info.genderType }}
            </span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">设计师：</label>
            <span class="info-value plaintext">
              {{ info.designer }}
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">样衣状态：</label>
            <span class="info-value plaintext">
              {{ optionLabel(sampleStatusOptions, info.sampleStatus) }}
            </span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">生产状态：</label>
            <span class="info-value plaintext">
              {{ optionLabel(productionStatusOptions, info.productionStatus) }}
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">主图：</label>
            <span class="info-value plaintext">
              <div v-if="info.imageUrl" class="style-image-list">
                <image-preview
                  v-for="(image, index) in imageList(info.imageUrl)"
                  :key="index"
                  :src="image"
                  :width="80"
                  :height="80"
                />
              </div>
              <span v-else>-</span>
            </span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">状态：</label>
            <span class="info-value plaintext">
              {{ statusLabel(info.status) }}
            </span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">备注：</label>
            <span class="info-value plaintext">
              {{ info.remark }}
            </span>
          </div>
        </el-col>
      </el-row>
      <h4 class="section-header">SKU</h4>
      <el-table :data="info.skuList || []" size="small" border>
        <el-table-column label="SKU编码" prop="skuCode" min-width="150" />
        <el-table-column label="颜色" prop="colorName" min-width="100" />
        <el-table-column label="颜色编码" prop="colorCode" min-width="100" />
        <el-table-column label="尺码编码" prop="sizeCode" min-width="100" />
        <el-table-column label="尺码名称" prop="sizeName" min-width="100" />
        <el-table-column label="条码" prop="barcode" min-width="120" />
        <el-table-column label="零售价" prop="retailPrice" min-width="90" />
        <el-table-column label="状态" min-width="80">
          <template slot-scope="scope">
            {{ statusLabel(scope.row.status) }}
          </template>
        </el-table-column>
      </el-table>
      <h4 class="section-header">BOM</h4>
      <div v-for="(bom, index) in info.bomList || []" :key="index" class="bom-detail-card">
        <el-row :gutter="20">
          <el-col :span="8"><span class="summary-label">BOM编号：</span>{{ bom.bomNo }}</el-col>
          <el-col :span="6"><span class="summary-label">版本：</span>{{ bom.versionNo }}</el-col>
          <el-col :span="6"><span class="summary-label">BOM状态：</span>{{ optionLabel(bomStatusOptions, bom.bomStatus) }}</el-col>
          <el-col :span="4"><span class="summary-label">生效日期：</span>{{ bom.effectiveDate || '-' }}</el-col>
        </el-row>
        <el-table :data="bom.detailList || []" size="small" border class="bom-detail-table">
          <el-table-column label="物料编码" prop="materialCode" min-width="110" />
          <el-table-column label="物料名称" prop="materialName" min-width="140" />
          <el-table-column label="类型" prop="materialType" min-width="90" />
          <el-table-column label="颜色" prop="colorName" min-width="90" />
          <el-table-column label="规格" prop="specName" min-width="110" />
          <el-table-column label="用量" prop="usageQty" min-width="90" />
          <el-table-column label="损耗率" prop="lossRate" min-width="90" />
          <el-table-column label="单位" prop="unitName" min-width="80" />
          <el-table-column label="部位" prop="positionName" min-width="100" />
        </el-table>
      </div>
      <el-empty v-if="!info.bomList || !info.bomList.length" description="暂无BOM" :image-size="80" />
    </div>
  </el-drawer>
</template>

<script>
import { getStyle } from '@/api/erp/style'

export default {
  name: 'StyleViewDrawer',
  data() {
    return {
      visible: false,
      loading: false,
      info: {},
      sampleStatusOptions: [
        { label: '待打样', value: '0' },
        { label: '打样中', value: '1' },
        { label: '已确认', value: '2' }
      ],
      productionStatusOptions: [
        { label: '未投产', value: '0' },
        { label: '生产中', value: '1' },
        { label: '已完结', value: '2' }
      ],
      bomStatusOptions: [
        { label: '草稿', value: '0' },
        { label: '已审核', value: '1' },
        { label: '已停用', value: '2' }
      ]
    }
  },
  methods: {
    open(styleId) {
      this.visible = true
      this.loading = true
      getStyle(styleId).then(res => {
        this.info = res.data || {}
      }).finally(() => {
        this.loading = false
      })
    },
    statusLabel(status) {
      if (status === null || status === undefined || status === '') {
        return '-'
      }
      return String(status) === '0' ? '正常' : '停用'
    },
    optionLabel(options, value) {
      const option = options.find(item => item.value === String(value))
      return option ? option.label : '-'
    },
    imageList(imageUrl) {
      return imageUrl ? String(imageUrl).split(',').filter(Boolean).slice(0, 3) : []
    },
    handleClose() {
      this.visible = false
    }
  }
}
</script>

<style scoped>
.style-image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.bom-detail-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}

.bom-detail-table {
  margin-top: 10px;
}

.summary-label {
  color: #909399;
}
</style>
