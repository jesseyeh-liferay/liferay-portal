/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.item.selector.criteria.info.item.criterion;

import com.liferay.item.selector.BaseItemSelectorCriterion;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

/**
 * @author Eudaldo Alonso
 */
public class InfoItemItemSelectorCriterion extends BaseItemSelectorCriterion {

	public String getItemSubtype() {
		return _itemSubtype;
	}

	public String getItemType() {
		return _itemType;
	}

	public String[] getMimeTypes() {
		return _mimeTypes;
	}

	@Deprecated
	public int getStatus() {
		return _status;
	}

	public int[] getStatuses() {
		return _statuses;
	}

	public void setItemSubtype(String itemSubtype) {
		_itemSubtype = itemSubtype;
	}

	public void setItemType(String itemType) {
		_itemType = itemType;
	}

	public void setMimeTypes(String[] mimeTypes) {
		_mimeTypes = mimeTypes;
	}

	@Deprecated
	public void setStatus(int status) {
		_status = status;
	}

	public void setStatuses(int... statuses) {
		if ((statuses.length > 1) &&
			ArrayUtil.contains(statuses, WorkflowConstants.STATUS_ANY)) {

			_statuses = new int[] {WorkflowConstants.STATUS_ANY};
		}
		else {
			_statuses = statuses;
		}
	}

	private String _itemSubtype;
	private String _itemType;
	private String[] _mimeTypes;

	@Deprecated
	private int _status = WorkflowConstants.STATUS_APPROVED;

	private int[] _statuses = {getStatus()};

}