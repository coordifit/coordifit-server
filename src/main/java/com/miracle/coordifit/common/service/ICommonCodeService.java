package com.miracle.coordifit.common.service;

import java.util.List;
import java.util.Map;

import com.miracle.coordifit.common.model.CommonCode;

public interface ICommonCodeService {
	void createCommonCode(CommonCode commonCode, String userId);

	List<CommonCode> getCommonCodesByParentCodeId(String parentCodeId);

	Map<String, CommonCode> getCommonCodes();

	void updateCommonCode(CommonCode commonCode, String codeId, String userId);

	void deleteCommonCode(String codeId);
}
