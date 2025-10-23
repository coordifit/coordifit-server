package com.miracle.coordifit.coordi.service;

import java.util.List;

import com.miracle.coordifit.coordi.dto.CoordiResponse;
import com.miracle.coordifit.coordi.model.Coordi;

public interface ICoordiService {

	List<CoordiResponse> getAllCoordisByUser(String userId);

	CoordiResponse getCoordiById(String coordiId);

	int upsertCoordi(Coordi coordi);

	int updateAiFileId(String coordiId, Integer aiFileId, String updatedBy);

	void insertCoordiItem(String canvasJson, Coordi coordi);

	void deleteCoordi(String coordiId);

	void deleteCoordis(List<String> coordiIds);
}
