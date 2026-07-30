package logic_core.app.dto.media;

public record UploadFile(
        String fileName,
        String contentType,
        byte[] data
) {}