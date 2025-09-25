package com.miracle.coordifit.common.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface IS3Service {
    String uploadFile(MultipartFile file) throws IOException;
}
