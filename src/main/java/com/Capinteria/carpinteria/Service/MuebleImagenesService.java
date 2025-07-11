package com.Capinteria.carpinteria.Service;

import com.Capinteria.carpinteria.Entity.MuebleImagenes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MuebleImagenesService extends BaseService<MuebleImagenes, Long> {
    ResponseEntity<String> uploadImages(List<MultipartFile> files);
}
