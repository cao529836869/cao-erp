<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="款号" prop="styleNo">
        <el-input
          v-model="queryParams.styleNo"
          placeholder="请输入款号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="款式名称" prop="styleName">
        <el-input
          v-model="queryParams.styleName"
          placeholder="请输入款式名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="品类" prop="categoryName">
        <el-input
          v-model="queryParams.categoryName"
          placeholder="请输入品类"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="品牌名称" prop="brandName">
        <el-input
          v-model="queryParams.brandName"
          placeholder="请输入品牌名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="季节" prop="seasonName">
        <el-input
          v-model="queryParams.seasonName"
          placeholder="请输入季节，如春夏、秋冬"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="年份" prop="yearName">
        <el-input
          v-model="queryParams.yearName"
          placeholder="请输入年份"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别类型" prop="genderType">
        <el-select v-model="queryParams.genderType" placeholder="请选择性别类型" clearable>
          <el-option
            v-for="item in genderOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="样衣状态" prop="sampleStatus">
        <el-select v-model="queryParams.sampleStatus" placeholder="请选择样衣状态" clearable>
          <el-option
            v-for="item in sampleStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="生产状态" prop="productionStatus">
        <el-select v-model="queryParams.productionStatus" placeholder="请选择生产状态" clearable>
          <el-option
            v-for="item in productionStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
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
          v-hasPermi="['erp:style:add']"
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
          v-hasPermi="['erp:style:edit']"
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
          v-hasPermi="['erp:style:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['erp:style:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table class="erp-table" v-loading="loading" :data="styleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="款号" align="center" prop="styleNo" width="170">
        <template slot-scope="scope"><span class="erp-code-text">{{ scope.row.styleNo || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="款式名称" align="left" prop="styleName" width="220" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-name-text">{{ scope.row.styleName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="品类" align="center" prop="categoryName" width="120">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.categoryName)]">{{ scope.row.categoryName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="品牌名称" align="left" prop="brandName" min-width="140" show-overflow-tooltip>
        <template slot-scope="scope"><span class="erp-muted-strong">{{ scope.row.brandName || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="季节" align="center" prop="seasonName" width="90" />
      <el-table-column label="年份" align="center" prop="yearName" width="90" />
      <el-table-column label="性别类型" align="center" prop="genderType" width="100">
        <template slot-scope="scope"><span :class="['erp-chip', $erpToneClass(scope.row.genderType)]">{{ scope.row.genderType || '-' }}</span></template>
      </el-table-column>
      <el-table-column label="样衣状态" align="center" prop="sampleStatus" width="100">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpStatusToneClass(optionLabel(sampleStatusOptions, scope.row.sampleStatus))]">
            {{ optionLabel(sampleStatusOptions, scope.row.sampleStatus) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="生产状态" align="center" prop="productionStatus" width="100">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpStatusToneClass(optionLabel(productionStatusOptions, scope.row.productionStatus))]">
            {{ optionLabel(productionStatusOptions, scope.row.productionStatus) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="主图" align="center" prop="imageUrl" width="180">
        <template slot-scope="scope">
          <div v-if="scope.row.imageUrl" class="style-image-list">
            <image-preview
              v-for="(image, index) in imageList(scope.row.imageUrl)"
              :key="index"
              :src="image"
              :width="46"
              :height="46"
            />
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <span :class="['erp-chip', $erpStatusToneClass(statusLabel(scope.row.status))]">
            {{ statusLabel(scope.row.status) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-popover
            v-if="scope.row.remark"
            placement="top-start"
            width="320"
            trigger="click"
            :content="scope.row.remark"
          >
            <el-link slot="reference" type="primary" :underline="false" class="remark-link">
              {{ scope.row.remark }}
            </el-link>
          </el-popover>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleViewData(scope.row)"
            v-hasPermi="['erp:style:query']"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['erp:style:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['erp:style:remove']"
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

    <!-- 童装款式主详情抽屉 -->
    <style-view-drawer ref="styleViewRef" />
    <!-- 添加或修改童装款式主对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1080px" append-to-body class="style-form-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px" class="style-form">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <el-row :gutter="18">
              <el-col :xs="24" :sm="12">
                <el-form-item label="款号" prop="styleNo">
                  <el-input v-model="form.styleNo" placeholder="请输入款号" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="款式名称" prop="styleName">
                  <el-input v-model="form.styleName" placeholder="请输入款式名称" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="品类" prop="categoryName">
                  <el-input v-model="form.categoryName" placeholder="请输入品类" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="品牌名称" prop="brandName">
                  <el-input v-model="form.brandName" placeholder="请输入品牌名称" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="季节" prop="seasonName">
                  <el-input v-model="form.seasonName" placeholder="如春夏、秋冬" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="年份" prop="yearName">
                  <el-input v-model="form.yearName" placeholder="请输入年份" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="性别类型" prop="genderType">
                  <el-select v-model="form.genderType" placeholder="请选择性别类型" clearable style="width: 100%">
                    <el-option
                      v-for="item in genderOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="设计师" prop="designer">
                  <el-input v-model="form.designer" placeholder="请输入设计师" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="样衣状态" prop="sampleStatus">
                  <el-select v-model="form.sampleStatus" placeholder="请选择样衣状态" style="width: 100%">
                    <el-option
                      v-for="item in sampleStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="生产状态" prop="productionStatus">
                  <el-select v-model="form.productionStatus" placeholder="请选择生产状态" style="width: 100%">
                    <el-option
                      v-for="item in productionStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="状态" prop="status">
                  <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                    <el-option
                      v-for="item in statusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="主图" prop="imageUrl">
                  <image-upload
                    v-model="form.imageUrl"
                    action="/common/minio/upload/image"
                    :data="{ folder: 'style' }"
                    :limit="3"
                    :file-size="5"
                    :is-show-tip="false"
                  />
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
              <el-table-column label="SKU编码" min-width="150">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.skuCode" placeholder="不填自动生成" />
                </template>
              </el-table-column>
              <el-table-column label="颜色" min-width="120">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.colorName" placeholder="颜色名称" />
                </template>
              </el-table-column>
              <el-table-column label="颜色编码" min-width="110">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.colorCode" placeholder="颜色编码" />
                </template>
              </el-table-column>
              <el-table-column label="尺码编码" min-width="110">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.sizeCode" placeholder="如120" />
                </template>
              </el-table-column>
              <el-table-column label="尺码名称" min-width="110">
                <template slot-scope="scope">
                  <el-input v-model="scope.row.sizeName" placeholder="如120码" />
                </template>
              </el-table-column>
              <el-table-column label="零售价" min-width="110">
                <template slot-scope="scope">
                  <el-input-number v-model="scope.row.retailPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                </template>
              </el-table-column>
              <el-table-column label="状态" min-width="100">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.status" style="width: 100%">
                    <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <el-tab-pane label="BOM" name="bom">
            <div class="sub-toolbar">
              <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addBomRow">新增BOM</el-button>
            </div>
            <div v-for="(bom, bomIndex) in form.bomList" :key="bomIndex" class="bom-card">
              <el-row :gutter="12">
                <el-col :xs="24" :sm="8">
                  <el-form-item label="BOM编号">
                    <el-input v-model="bom.bomNo" placeholder="不填自动生成" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="6">
                  <el-form-item label="版本">
                    <el-input v-model="bom.versionNo" placeholder="如V1" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="6">
                  <el-form-item label="BOM状态">
                    <el-select v-model="bom.bomStatus" style="width: 100%">
                      <el-option v-for="item in bomStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :sm="4" class="bom-card-actions">
                  <el-button type="danger" plain size="mini" icon="el-icon-delete" @click="removeBomRow(bomIndex)">删除BOM</el-button>
                </el-col>
                <el-col :xs="24" :sm="8">
                  <el-form-item label="生效日期">
                    <el-date-picker v-model="bom.effectiveDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注">
                    <el-input v-model="bom.remark" placeholder="请输入BOM备注" />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="sub-toolbar">
                <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addBomDetailRow(bomIndex)">新增物料</el-button>
              </div>
              <el-table :data="bom.detailList" size="mini" border class="sub-table">
                <el-table-column label="物料" min-width="190">
                  <template slot-scope="scope">
                    <el-select
                      v-model="scope.row.materialId"
                      placeholder="选择物料"
                      filterable
                      clearable
                      style="width: 100%"
                      @change="handleBomMaterialChange(scope.row)"
                    >
                      <el-option
                        v-for="item in materialOptions"
                        :key="item.materialId"
                        :label="item.materialCode + ' / ' + item.materialName"
                        :value="item.materialId"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="物料编码" min-width="180">
                  <template slot-scope="scope">
                    <el-input v-model="scope.row.materialCode" placeholder="编码">
                      <el-button slot="append" @click="handleGenerateMaterialCode(scope.row)">生成</el-button>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column label="物料名称" min-width="140">
                  <template slot-scope="scope"><el-input v-model="scope.row.materialName" placeholder="名称" /></template>
                </el-table-column>
                <el-table-column label="类型" min-width="100">
                  <template slot-scope="scope"><el-input v-model="scope.row.materialType" placeholder="面料/辅料" /></template>
                </el-table-column>
                <el-table-column label="SKU" min-width="170">
                  <template slot-scope="scope">
                    <el-select
                      v-model="scope.row.materialSkuId"
                      placeholder="选择SKU"
                      filterable
                      clearable
                      style="width: 100%"
                      @change="handleBomMaterialSkuChange(scope.row)"
                    >
                      <el-option
                        v-for="item in scope.row.materialSkuOptions || []"
                        :key="item.materialSkuId"
                        :label="item.materialSkuCode"
                        :value="item.materialSkuId"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="颜色" min-width="100">
                  <template slot-scope="scope"><el-input v-model="scope.row.colorName" placeholder="颜色" /></template>
                </el-table-column>
                <el-table-column label="规格" min-width="120">
                  <template slot-scope="scope"><el-input v-model="scope.row.specName" placeholder="规格" /></template>
                </el-table-column>
                <el-table-column label="用量/单位" min-width="180">
                  <template slot-scope="scope">
                    <div class="usage-unit-cell">
                      <el-input-number v-model="scope.row.usageQty" :min="0" :precision="4" controls-position="right" class="usage-input" />
                      <el-input v-model="scope.row.unitName" placeholder="单位" class="unit-input" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="损耗率" min-width="110">
                  <template slot-scope="scope">
                    <el-input-number v-model="scope.row.lossRate" :min="0" :precision="4" controls-position="right" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="部位" min-width="100">
                  <template slot-scope="scope"><el-input v-model="scope.row.positionName" placeholder="部位" /></template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template slot-scope="scope">
                    <el-button type="text" size="mini" icon="el-icon-delete" @click="removeBomDetailRow(bomIndex, scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-if="!form.bomList || !form.bomList.length" description="暂无BOM" :image-size="80" />
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
import { listStyle, getStyle, delStyle, addStyle, updateStyle } from "@/api/erp/style"
import { generateMaterialCode, getMaterial, optionselectMaterial } from "@/api/erp/material"
import StyleViewDrawer from "./view"

export default {
  name: "Style",
  components: { StyleViewDrawer },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 童装款式主表格数据
      styleList: [],
      // 物料选择数据
      materialOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 当前编辑页签
      activeTab: "basic",
      // 性别类型选项
      genderOptions: [
        { label: "男童", value: "男童" },
        { label: "女童", value: "女童" },
        { label: "中性", value: "中性" }
      ],
      // 状态选项
      statusOptions: [
        { label: "正常", value: "0" },
        { label: "停用", value: "1" }
      ],
      // 样衣状态选项
      sampleStatusOptions: [
        { label: "待打样", value: "0" },
        { label: "打样中", value: "1" },
        { label: "已确认", value: "2" }
      ],
      // 生产状态选项
      productionStatusOptions: [
        { label: "未投产", value: "0" },
        { label: "生产中", value: "1" },
        { label: "已完结", value: "2" }
      ],
      // BOM状态选项
      bomStatusOptions: [
        { label: "草稿", value: "0" },
        { label: "已审核", value: "1" },
        { label: "已停用", value: "2" }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        styleNo: null,
        styleName: null,
        categoryName: null,
        brandName: null,
        seasonName: null,
        yearName: null,
        genderType: null,
        designer: null,
        sampleStatus: null,
        productionStatus: null,
        imageUrl: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        styleNo: [
          { required: true, message: "款号不能为空", trigger: "blur" }
        ],
        styleName: [
          { required: true, message: "款式名称不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
    this.getMaterialOptions()
  },
  activated() {
    this.getMaterialOptions()
  },
  methods: {
    /** 查询童装款式主列表 */
    getList() {
      this.loading = true
      listStyle(this.queryParams).then(response => {
        this.styleList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getMaterialOptions() {
      optionselectMaterial().then(response => {
        this.materialOptions = response.data || []
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        styleId: null,
        styleNo: null,
        styleName: null,
        categoryName: null,
        brandName: null,
        seasonName: null,
        yearName: null,
        genderType: null,
        designer: null,
        sampleStatus: "0",
        productionStatus: "0",
        imageUrl: null,
        status: "0",
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        skuList: [],
        bomList: []
      }
      this.activeTab = "basic"
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 状态中文显示
    statusLabel(status) {
      const option = this.statusOptions.find(item => item.value === String(status))
      return option ? option.label : "-"
    },
    // 通用选项中文显示
    optionLabel(options, value) {
      const option = options.find(item => item.value === String(value))
      return option ? option.label : "-"
    },
    // 状态标签样式
    statusTagType(status) {
      if (status === null || status === undefined || status === "") {
        return "info"
      }
      return String(status) === "0" ? "success" : "danger"
    },
    // 三态业务状态标签样式
    tripleStatusTagType(status) {
      const value = String(status)
      if (value === "0") return "info"
      if (value === "1") return "warning"
      if (value === "2") return "success"
      return "info"
    },
    // 多图字符串转数组
    imageList(imageUrl) {
      return imageUrl ? String(imageUrl).split(",").filter(Boolean).slice(0, 3) : []
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.styleId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.getMaterialOptions()
      this.reset()
      this.open = true
      this.title = "添加童装款式主"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.getMaterialOptions()
      this.reset()
      const styleId = row.styleId || this.ids
      getStyle(styleId).then(response => {
        const data = response.data || {}
        this.form = {
          ...data,
          status: this.normalizeStatus(data.status, "0"),
          sampleStatus: this.normalizeStatus(data.sampleStatus, "0"),
          productionStatus: this.normalizeStatus(data.productionStatus, "0"),
          skuList: (data.skuList || []).map(item => ({
            ...item,
            status: this.normalizeStatus(item.status, "0")
          })),
          bomList: (data.bomList || []).map(item => ({
            ...item,
            bomStatus: this.normalizeStatus(item.bomStatus, "0"),
            detailList: (item.detailList || []).map(detail => ({
              ...detail,
              materialSkuOptions: []
            }))
          }))
        }
        this.loadBomDetailSkuOptions()
        this.activeTab = "basic"
        this.open = true
        this.title = "修改童装款式主"
      })
    },
    normalizeStatus(value, defaultValue) {
      return value === null || value === undefined || value === "" ? defaultValue : String(value)
    },
    addSkuRow() {
      if (!this.form.skuList) {
        this.$set(this.form, "skuList", [])
      }
      this.form.skuList.push({
        skuCode: null,
        colorCode: null,
        colorName: null,
        sizeCode: null,
        sizeName: null,
        barcode: null,
        retailPrice: 0,
        status: "0",
        remark: null
      })
    },
    removeSkuRow(index) {
      this.form.skuList.splice(index, 1)
    },
    addBomRow() {
      if (!this.form.bomList) {
        this.$set(this.form, "bomList", [])
      }
      this.form.bomList.push({
        bomNo: null,
        versionNo: "V1",
        bomStatus: "0",
        effectiveDate: null,
        remark: null,
        detailList: []
      })
    },
    removeBomRow(index) {
      this.form.bomList.splice(index, 1)
    },
    addBomDetailRow(bomIndex) {
      this.getMaterialOptions()
      const bom = this.form.bomList[bomIndex]
      if (!bom.detailList) {
        this.$set(bom, "detailList", [])
      }
      bom.detailList.push({
        materialId: null,
        materialSkuId: null,
        materialCode: null,
        materialName: null,
        materialType: null,
        materialSkuOptions: [],
        colorName: null,
        specName: null,
        usageQty: 0,
        lossRate: 0,
        unitName: null,
        positionName: null,
        remark: null
      })
    },
    removeBomDetailRow(bomIndex, detailIndex) {
      this.form.bomList[bomIndex].detailList.splice(detailIndex, 1)
    },
    loadBomDetailSkuOptions() {
      ;(this.form.bomList || []).forEach(bom => {
        ;(bom.detailList || []).forEach(detail => {
          if (!detail.materialId) {
            return
          }
          getMaterial(detail.materialId).then(response => {
            this.$set(detail, "materialSkuOptions", (response.data || {}).skuList || [])
          })
        })
      })
    },
    handleBomMaterialChange(row) {
      const materialId = row.materialId
      row.materialSkuId = null
      this.$set(row, "materialSkuOptions", [])
      if (!materialId) {
        row.materialCode = null
        row.materialName = null
        row.materialType = null
        row.unitName = null
        return
      }
      getMaterial(materialId).then(response => {
        const material = response.data || {}
        this.$set(row, "materialCode", material.materialCode)
        this.$set(row, "materialName", material.materialName)
        this.$set(row, "materialType", material.materialType)
        this.$set(row, "unitName", material.unitName)
        this.$set(row, "materialSkuOptions", material.skuList || [])
      })
    },
    handleBomMaterialSkuChange(row) {
      const sku = (row.materialSkuOptions || []).find(item => item.materialSkuId === row.materialSkuId)
      if (!sku) {
        return
      }
      this.$set(row, "colorName", sku.colorName)
      this.$set(row, "specName", sku.specName)
      this.$set(row, "unitName", sku.unitName || row.unitName)
    },
    handleGenerateMaterialCode(row) {
      generateMaterialCode({
        materialType: row.materialType,
        categoryName: row.materialName
      }).then(response => {
        this.$set(row, "materialCode", response.materialCode)
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.styleId != null) {
            updateStyle(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStyle(this.form).then(response => {
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
      const styleIds = row.styleId || this.ids
      this.$modal.confirm('是否确认删除童装款式主编号为"' + styleIds + '"的数据项？').then(function() {
        return delStyle(styleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 详情按钮操作 */
    handleViewData(row) {
      this.$refs["styleViewRef"].open(row.styleId)
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('erp/style/export', {
        ...this.queryParams
      }, `style_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.remark-link {
  display: inline-block;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.style-image-list {
  display: flex;
  justify-content: center;
  gap: 6px;
}

::v-deep .style-form-dialog .el-dialog__body {
  padding: 18px 24px 4px !important;
}

.style-form {
  padding-right: 6px;
}

.style-form ::v-deep .el-form-item {
  margin-bottom: 18px;
}

.style-form ::v-deep .component-upload-image .el-upload-list--picture-card .el-upload-list__item,
.style-form ::v-deep .component-upload-image .el-upload--picture-card {
  width: 86px;
  height: 86px;
  line-height: 86px;
}

.sub-toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}

.sub-table {
  margin-bottom: 12px;
}

.sub-table ::v-deep .el-input__inner {
  height: 28px;
  line-height: 28px;
  padding: 0 8px;
}

.sub-table ::v-deep .el-table__body-wrapper::-webkit-scrollbar {
  height: 14px;
}

.sub-table ::v-deep .el-table__body-wrapper::-webkit-scrollbar-thumb {
  background-color: #b8c0cc;
  border-radius: 8px;
  border: 3px solid #f4f6f8;
}

.sub-table ::v-deep .el-table__body-wrapper::-webkit-scrollbar-track {
  background-color: #f4f6f8;
  border-radius: 8px;
}

.usage-unit-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.usage-unit-cell .usage-input {
  flex: 1 1 108px;
  min-width: 108px;
}

.usage-unit-cell .unit-input {
  flex: 0 0 54px;
  width: 54px;
}

.bom-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 14px 14px 12px;
  margin-bottom: 14px;
  background: #fff;
}

.bom-card-actions {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
}
</style>
