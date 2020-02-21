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

package com.liferay.document.library.web.internal.upload;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.util.DLValidator;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONDeserializer;
import com.liferay.portal.kernel.json.JSONDeserializerTransformer;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermissionHelper;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.upload.UniqueFileNameProvider;
import com.liferay.upload.UploadFileEntryHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Roberto Díaz
 * @author Sergio González
 * @author Alejandro Tardín
 */
@Component(service = DLUploadFileEntryHandler.class)
public class DLUploadFileEntryHandler implements UploadFileEntryHandler {

	@Override
	public FileEntry upload(UploadPortletRequest uploadPortletRequest)
		throws IOException, PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)uploadPortletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		long folderId = ParamUtil.getLong(uploadPortletRequest, "folderId");

		ModelResourcePermissionHelper.check(
			_folderModelResourcePermission, themeDisplay.getPermissionChecker(),
			themeDisplay.getScopeGroupId(), folderId, ActionKeys.ADD_DOCUMENT);

		String fileName = uploadPortletRequest.getFileName(_PARAMETER_NAME);
		long size = uploadPortletRequest.getSize(_PARAMETER_NAME);

		_dlValidator.validateFileSize(fileName, size);

		String contentType = uploadPortletRequest.getContentType(
			_PARAMETER_NAME);
		String customFieldsKeys = uploadPortletRequest.getParameter("customFieldsKeys");
		String customFieldsValues = uploadPortletRequest.getParameter("customFieldsValues");
		String customFieldsTypes = uploadPortletRequest.getParameter("customFieldsTypes");

		Map<String, Serializable> customFieldsMap = new HashMap<>();

		String[] customFieldsKeysArray = StringUtil.split(customFieldsKeys);
		List<String> customFieldsValuesList = (List) JSONFactoryUtil.looseDeserialize(customFieldsValues);
		try {
			List<Class<?>> customFieldsTypesList = Arrays.stream(StringUtil.split(customFieldsTypes)).map(x -> _getClassFromString(x)).collect(
				Collectors.toList());

			for (int i = 0; i < customFieldsValuesList.size(); i++) {
				Serializable value = (Serializable) JSONFactoryUtil.looseDeserialize(customFieldsValuesList.get(i), customFieldsTypesList.get(i));
				customFieldsMap.put(customFieldsKeysArray[i], value);
			}
		}
		catch (Exception e) {

		}

		String description = uploadPortletRequest.getParameter("description");

		try (InputStream inputStream = uploadPortletRequest.getFileAsStream(
				_PARAMETER_NAME)) {

			String uniqueFileName = _uniqueFileNameProvider.provide(
				fileName,
				curFileName -> _exists(
					themeDisplay.getScopeGroupId(), folderId, curFileName));

			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				DLFileEntry.class.getName(), uploadPortletRequest);

			serviceContext.setExpandoBridgeAttributes(customFieldsMap);

			return _dlAppService.addFileEntry(
				themeDisplay.getScopeGroupId(), folderId, uniqueFileName,
				contentType, uniqueFileName, description, StringPool.BLANK,
				inputStream, size, serviceContext);
		}
	}

	private boolean _exists(long groupId, long folderId, String fileName) {
		try {
			if (_dlAppService.getFileEntry(groupId, folderId, fileName) !=
					null) {

				return true;
			}

			return false;
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return false;
		}
	}

	private Class<?> _getClassFromString(String className) {
		try {
			return Class.forName(className);
		}
		catch (Exception e) {

		}
		return null;
	}

	private static final String _PARAMETER_NAME = "imageSelectorFileName";

	private static final Log _log = LogFactoryUtil.getLog(
		DLUploadFileEntryHandler.class);

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private DLValidator _dlValidator;

	@Reference(
		target = "(model.class.name=com.liferay.portal.kernel.repository.model.Folder)"
	)
	private ModelResourcePermission<Folder> _folderModelResourcePermission;

	@Reference
	private UniqueFileNameProvider _uniqueFileNameProvider;

}