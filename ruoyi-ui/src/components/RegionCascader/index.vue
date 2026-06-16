<template>
  <el-cascader
    v-model="selectedCodes"
    :options="regionData"
    :props="cascaderProps"
    :placeholder="placeholder"
    :clearable="clearable"
    :filterable="filterable"
    :disabled="disabled"
    :style="{ width: width }"
    @change="handleChange"
  />
</template>

<script>
import { regionData, CodeToText, TextToCode } from 'element-china-area-data'

export default {
  name: 'RegionCascader',
  props: {
    province: {
      type: String,
      default: ''
    },
    city: {
      type: String,
      default: ''
    },
    county: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请选择省 / 市 / 区县'
    },
    width: {
      type: String,
      default: '100%'
    },
    clearable: {
      type: Boolean,
      default: true
    },
    filterable: {
      type: Boolean,
      default: true
    },
    disabled: {
      type: Boolean,
      default: false
    },
    checkStrictly: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      regionData,
      selectedCodes: [],
      syncing: false
    }
  },
  computed: {
    cascaderProps() {
      return {
        expandTrigger: 'hover',
        checkStrictly: this.checkStrictly
      }
    }
  },
  watch: {
    province: {
      immediate: true,
      handler() {
        this.syncSelectedCodes()
      }
    },
    city() {
      this.syncSelectedCodes()
    },
    county() {
      this.syncSelectedCodes()
    }
  },
  methods: {
    syncSelectedCodes() {
      if (this.syncing) {
        return
      }
      const nextCodes = this.getCodesByText(this.province, this.city, this.county)
      if (nextCodes.join(',') !== this.selectedCodes.join(',')) {
        this.selectedCodes = nextCodes
      }
    },
    getCodesByText(province, city, county) {
      if (!province || !TextToCode[province]) {
        return []
      }

      const provinceNode = TextToCode[province]
      const codes = [provinceNode.code]

      if (city && provinceNode[city]) {
        const cityNode = provinceNode[city]
        codes.push(cityNode.code)

        if (county && cityNode[county]) {
          codes.push(cityNode[county].code)
        }
      }

      return codes
    },
    handleChange(codes) {
      const names = codes.map(code => CodeToText[code]).filter(Boolean)
      const region = {
        province: names[0] || '',
        city: names[1] || '',
        county: names[2] || '',
        codes: codes || []
      }

      this.syncing = true
      this.$emit('update:province', region.province)
      this.$emit('update:city', region.city)
      this.$emit('update:county', region.county)
      this.$emit('change', region)
      this.$nextTick(() => {
        this.syncing = false
      })
    }
  }
}
</script>
