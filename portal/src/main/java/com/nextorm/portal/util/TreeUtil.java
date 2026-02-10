package com.nextorm.portal.util;

import com.nextorm.portal.dto.base.BaseTreeItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeUtil {
	public static List<BaseTreeItem> generateTreeHierarchy(List<BaseTreeItem> itemList) {
		Stream<BaseTreeItem> itemStream = itemList.stream();
		Map<String, BaseTreeItem> itemMap = itemStream.collect(Collectors.toMap(BaseTreeItem::getTreeId,
			Function.identity(),
			(x, y) -> y,
			LinkedHashMap::new));
		for (BaseTreeItem item : itemMap.values()) {
			if (item.getParentFinder() != null) {
				BaseTreeItem parent = item.getParentFinder()
										  .findParent(itemList, item);
				if (parent != null) {
					parent.getChildren()
						  .add(item);
				}
			} else if (item.getParentTree() != null) {
				BaseTreeItem parent = itemMap.get(item.getParentTree());
				if (parent != null) {
					parent.getChildren()
						  .add(item);
				}
			}
		}
		List<BaseTreeItem> result = itemMap.values()
										   .stream()
										   .filter(item -> item.getParent() == null)
										   .collect(Collectors.toList());
		return result;
	}
}
