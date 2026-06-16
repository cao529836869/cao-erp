const toneList = [
  "erp-chip-blue",
  "erp-chip-green",
  "erp-chip-orange",
  "erp-chip-purple",
  "erp-chip-cyan",
  "erp-chip-red"
]

const statusToneMap = {
  "正常": "erp-chip-blue",
  "启用": "erp-chip-blue",
  "草稿": "erp-chip-gray",
  "已审核": "erp-chip-blue",
  "已入库": "erp-chip-green",
  "已出库": "erp-chip-green",
  "已完成": "erp-chip-green",
  "已发货": "erp-chip-green",
  "已拣货": "erp-chip-cyan",
  "进行中": "erp-chip-cyan",
  "已取消": "erp-chip-orange",
  "已作废": "erp-chip-red",
  "停用": "erp-chip-gray",
  "超时": "erp-chip-red",
  "已超时": "erp-chip-red"
}

const typeToneMap = {
  "面料": "erp-chip-green",
  "辅料": "erp-chip-blue",
  "包装材料": "erp-chip-amber",
  "物料": "erp-chip-green",
  "成衣": "erp-chip-blue",
  "物料仓": "erp-chip-green",
  "成衣仓": "erp-chip-blue",
  "次品仓": "erp-chip-red",
  "样品仓": "erp-chip-purple",
  "采购入库": "erp-chip-blue",
  "生产入库": "erp-chip-green",
  "销售退货": "erp-chip-orange",
  "盘盈入库": "erp-chip-cyan",
  "销售出库": "erp-chip-blue",
  "生产领料": "erp-chip-green",
  "采购退货": "erp-chip-orange",
  "盘亏出库": "erp-chip-red"
}

function hashText(value) {
  const text = String(value || "")
  let hash = 0
  for (let i = 0; i < text.length; i++) {
    hash = text.charCodeAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

export function erpToneClass(value) {
  if (!value) {
    return "erp-chip-gray"
  }
  return typeToneMap[value] || toneList[hashText(value) % toneList.length]
}

export function erpStatusToneClass(value) {
  if (!value) {
    return "erp-chip-gray"
  }
  return statusToneMap[value] || erpToneClass(value)
}
