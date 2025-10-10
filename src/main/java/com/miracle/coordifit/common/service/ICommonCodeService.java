package com.miracle.coordifit.common.service;

import java.util.List;
import java.util.Map;

import com.miracle.coordifit.common.model.CommonCode;

public interface ICommonCodeService {
	CommonCode createCommonCode(CommonCode commonCode);

	List<CommonCode> getCommonCodesByParentCodeId(String parentCodeId);

	Map<String, CommonCode> getCommonCodes();

	CommonCode updateCommonCode(CommonCode commonCode);

	void deleteCommonCode(String codeId);
}
