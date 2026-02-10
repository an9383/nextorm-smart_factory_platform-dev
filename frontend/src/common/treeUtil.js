const expandTreeData = (items, options = { children: 'children' }) => {
  const expanded = []
  items.forEach((item) => {
    expanded.push(item)
    if (item[options.children]) {
      expanded.push(...expandTreeData(item[options.children]))
    }
  })
  return expanded
}
const findTreeItem = (items, id, options = { id: 'id' }) => {
  return expandTreeData(items).find((v) => v[options.id] === id)
}
const filterTreeItem = (items, id, options = { id: 'id' }) => {
  return expandTreeData(items).filter((v) => v[options.id] === id)
}

export { expandTreeData, findTreeItem, filterTreeItem }
