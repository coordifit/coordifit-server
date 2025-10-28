package com.miracle.coordifit.coordi.service;

import java.util.List;

import com.miracle.coordifit.coordi.dto.CoordiResponse;
import com.miracle.coordifit.coordi.model.Coordi;

public interface ICoordiService {

	List<CoordiResponse> getAllCoordisByUser(String userId);

	CoordiResponse getCoordiById(String coordiId);

	Coordi insertCoordi(String userId, String canvasJson, String coordiName, String description, int fileId);

	Coordi updateCoordi(String userId, String canvasJson, String coordiName, String description, int fileId,
		String coordiId);

	int updateAiFileId(String coordiId, Integer aiFileId, String updatedBy);

	void insertCoordiItem(String canvasJson, Coordi coordi);

	Coordi deleteCoordi(String coordiId, String userId);

	void deleteCoordis(List<String> coordiIds, String userId);
}
