package com.miracle.coordifit.fitting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FittingAnalysisErrorResponse {

	private final String code;
	private final String message;
	private final int status;
}
