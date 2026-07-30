package logic_core.domain.service;

import logic_core.app.dto.media.UploadFile;

import java.io.File;

public interface MediaStorageService
{

    String uploadAvatar(UploadFile file);

    String uploadBanner(
            UploadFile file
    );

    void delete(String path);

}