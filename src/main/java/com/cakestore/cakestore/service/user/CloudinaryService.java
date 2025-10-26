package com.cakestore.cakestore.service.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary c) {
        this.cloudinary = c;
    }

    /** Trả về secure_url + public_id */
    public UploadResult uploadImage(MultipartFile file, String folder) throws IOException {
        Map<?, ?> r = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", folder, "overwrite", true, "resource_type", "image"));
        return new UploadResult((String) r.get("secure_url"), (String) r.get("public_id"));
    }

    public void deleteByPublicId(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank())
            return;
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    public record UploadResult(String url, String publicId) {
    }
}
