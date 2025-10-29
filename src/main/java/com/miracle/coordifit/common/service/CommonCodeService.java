package com.miracle.coordifit.common.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miracle.coordifit.common.model.CommonCode;
import com.miracle.coordifit.common.repository.CommonCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommonCodeService implements ICommonCodeService {
	private final CommonCodeRepository commonCodeRepository;

	@Override
	public void createCommonCode(CommonCode commonCode, String userId) {
		String codeId = generateCodeId(commonCode.getParentCodeId());
		commonCode.setCodeId(codeId);
		commonCode.setCreatedBy(userId);
		int result = commonCodeRepository.insertCommonCode(commonCode);
		if (result <= 0) {
			throw new RuntimeException("공통 코드 생성 실패");
		}
	}

	private String generateCodeId(String parentCodeId) {
		String header;

		if (parentCodeId == null) {
			header = selectLastCodeHeader();
		} else {
			char firstChar = parentCodeId.charAt(0);
			header = String.valueOf(firstChar) + (Integer.parseInt(parentCodeId.substring(1, 2)) + 1);
		}

		return createLastCodeId(header);
	}

	private String selectLastCodeHeader() {
		String lastCodeId = commonCodeRepository.selectLastCodeId();
		String codeHeader = "A0";

		if (lastCodeId != null && !lastCodeId.isEmpty()) {
			char firstChar = lastCodeId.charAt(0);
			char nextChar = (char)(firstChar + 1);
			codeHeader = String.valueOf(nextChar) + "0";
		}

		return codeHeader;
	}

	private String createLastCodeId(String header) {
		String lastCodeId = commonCodeRepository.selectLastCodeIdByHeader(header + "%");

		String codeId = header + "0001";

		if (lastCodeId != null && !lastCodeId.isEmpty()) {
			String lastNumberStr = lastCodeId.substring(header.length());
			int lastNumber = Integer.parseInt(lastNumberStr);
			codeId = header + String.format("%04d", lastNumber + 1);
		}

		return codeId;
	}

	@Override
	public Map<String, CommonCode> getCommonCodes() {
		List<CommonCode> commonCodes = commonCodeRepository.selectCommonCodes();
		return createCommonCodeMap(commonCodes);
	}

	@Override
	public void updateCommonCode(CommonCode commonCode, String codeId, String userId) {
		commonCode.setCodeId(codeId);
		commonCode.setUpdatedBy(userId);
		int result = commonCodeRepository.updateCommonCode(commonCode);
		if (result <= 0) {
			throw new RuntimeException("공통 코드 수정 실패");
		}
	}

	@Override
	public void deleteCommonCode(String codeId) {
		int result = commonCodeRepository.deleteCommonCode(codeId);
		if (result <= 0) {
			throw new RuntimeException("공통 코드 삭제 실패");
		}
	}

	private Map<String, CommonCode> createCommonCodeMap(List<CommonCode> commonCodes) {
		Map<String, CommonCode> commonCodeMap = new LinkedHashMap<>();

		for (CommonCode commonCode : commonCodes) {
			if (commonCode.getLevel() == 1) {
				if (!commonCodeMap.containsKey(commonCode.getCodeId())) {
					commonCodeMap.put(commonCode.getCodeId(), commonCode);
				}
			} else {
				createChildCodeMap(commonCodeMap, commonCode);
			}
		}

		return commonCodeMap;
	}

	private void createChildCodeMap(Map<String, CommonCode> commonCodeMap, CommonCode childCode) {
		for (CommonCode rootCode : commonCodeMap.values()) {
			if (rootCode.getCodeId().equals(childCode.getParentCodeId())) {
				if (rootCode.getChildren().containsKey(childCode.getParentCodeId())) {
					rootCode.getChildren().get(childCode.getParentCodeId())
						.getChildren().put(childCode.getCodeId(), childCode);
				} else {
					rootCode.getChildren().put(childCode.getCodeId(), childCode);
				}
			} else {
				createChildCodeMap(rootCode.getChildren(), childCode);
			}
		}
	}

	@Override
	public List<CommonCode> getCommonCodesByParentCodeId(String parentCodeId) {
		return commonCodeRepository.selectCommonCodesByParentCodeId(parentCodeId);
	}
}
