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

package com.liferay.users.admin.internal.search.spi.model.permission.contributor;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.UserBag;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.search.spi.model.permission.SearchPermissionFilterContributor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jesse Yeh
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.portal.kernel.model.User",
	service = SearchPermissionFilterContributor.class
)
public class UserSiteMembershipsSearchPermissionFilterContributor
	implements SearchPermissionFilterContributor {

	@Override
	public void contribute(
		BooleanFilter booleanFilter, long companyId, long[] groupIds,
		long userId, PermissionChecker permissionChecker, String className) {

		TermsFilter termsFilter = new TermsFilter(Field.ROLE_IDS);

		try {
			Set<Role> roles = new HashSet<>();

			roles.addAll(_getGroupRoles(userId, permissionChecker));
			roles.addAll(_getRoles(companyId, permissionChecker));

			for (Role role : roles) {
				termsFilter.addValue(String.valueOf(role.getRoleId()));
			}

			booleanFilter.add(termsFilter, BooleanClauseOccur.SHOULD);
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}
	}

	@Reference
	protected RoleLocalService roleLocalService;

	private Collection<? extends Role> _getGroupRoles(
			long userId, PermissionChecker permissionChecker)
		throws Exception {

		Set<Role> groupRoles = new HashSet<>();

		UserBag userBag = permissionChecker.getUserBag();

		for (Group group : userBag.getGroups()) {
			long[] groupRoleIds = permissionChecker.getRoleIds(
				userId, group.getGroupId());

			groupRoles.addAll(roleLocalService.getRoles(groupRoleIds));
		}

		return groupRoles;
	}

	private Collection<? extends Role> _getRoles(
			long companyId, PermissionChecker permissionChecker)
		throws Exception {

		Set<Role> roles = new HashSet<>();

		UserBag userBag = permissionChecker.getUserBag();

		if (permissionChecker.isSignedIn()) {
			roles.addAll(userBag.getRoles());

			roles.add(roleLocalService.getRole(companyId, RoleConstants.GUEST));
		}
		else {
			roles.addAll(
				roleLocalService.getRoles(
					permissionChecker.getGuestUserRoleIds()));
		}

		return roles;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UserSiteMembershipsSearchPermissionFilterContributor.class);

}