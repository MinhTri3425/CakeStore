package com.cakestore.cakestore.service.admin;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload một file ảnh lên Cloudinary.
     * @param file File ảnh MultipartFile
     * @return URL của ảnh sau khi upload
     * @throws IOException Nếu có lỗi trong quá trình upload
     */
    @SuppressWarnings("rawtypes")
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
            "folder", "cakestore_products" // Tùy chọn đặt trong folder riêng
        ));

        return uploadResult.get("url").toString();
    }
    
    /**
     * Xóa một file ảnh khỏi Cloudinary (cần public ID)
     * @param publicId Public ID của ảnh (ví dụ: cakestore_products/image_name)
     * @throws IOException Nếu có lỗi trong quá trình xóa
     */
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
    
    // Helper: Trích xuất Public ID từ URL (để phục vụ chức năng xóa)
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        // Logic trích xuất: tìm chuỗi "cakestore_products/" cho đến dấu "." cuối cùng
        try {
            String path = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            return "cakestore_products/" + path.substring(0, path.lastIndexOf("."));
        } catch (Exception e) {
            return null;
        }
    }
}