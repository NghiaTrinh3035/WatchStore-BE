package com.example.demo.core.services;


import com.example.demo.features.communications.controllers.*;
import com.example.demo.features.warranty.dtos.request.*;
import com.example.demo.features.communications.entities.*;
import com.example.demo.features.auth.dtos.response.*;
import com.example.demo.features.inventory.services.*;
import com.example.demo.features.orders.dtos.request.*;
import com.example.demo.features.auth.dtos.request.*;
import com.example.demo.features.reports.dtos.response.*;
import com.example.demo.features.reports.services.*;
import com.example.demo.features.orders.entities.*;
import com.example.demo.features.inventory.controllers.*;
import com.example.demo.features.vouchers.dtos.response.*;
import com.example.demo.features.communications.repositories.*;
import com.example.demo.features.inventory.dtos.request.*;
import com.example.demo.core.enums.*;
import com.example.demo.core.dtos.response.*;
import com.example.demo.features.auth.controllers.*;
import com.example.demo.features.reports.repositories.*;
import com.example.demo.core.services.*;
import com.example.demo.features.vouchers.controllers.*;
import com.example.demo.features.warranty.services.*;
import com.example.demo.features.communications.dtos.response.*;
import com.example.demo.features.orders.dtos.response.*;
import com.example.demo.core.exceptions.*;
import com.example.demo.features.reports.dtos.request.*;
import com.example.demo.features.auth.services.*;
import com.example.demo.features.users.dtos.response.*;
import com.example.demo.features.users.services.*;
import com.example.demo.features.users.controllers.*;
import com.example.demo.features.products.dtos.response.*;
import com.example.demo.core.config.*;
import com.example.demo.features.orders.services.payment.*;
import com.example.demo.features.vouchers.dtos.request.*;
import com.example.demo.features.products.services.*;
import com.example.demo.features.vouchers.services.*;
import com.example.demo.core.entities.*;
import com.example.demo.features.warranty.entities.*;
import com.example.demo.features.inventory.dtos.response.*;
import com.example.demo.features.warranty.controllers.*;
import com.example.demo.features.users.entities.*;
import com.example.demo.features.products.dtos.request.*;
import com.example.demo.features.warranty.repositories.*;
import com.example.demo.features.inventory.repositories.*;
import com.example.demo.features.communications.dtos.request.*;
import com.example.demo.features.warranty.dtos.response.*;
import com.example.demo.features.orders.controllers.*;
import com.example.demo.features.products.entities.*;
import com.example.demo.features.vouchers.entities.*;
import com.example.demo.features.products.controllers.*;
import com.example.demo.features.reports.controllers.*;
import com.example.demo.features.inventory.entities.*;
import com.example.demo.features.communications.services.*;
import com.example.demo.features.orders.services.*;
import com.example.demo.features.users.dtos.request.*;
import com.example.demo.features.reports.entities.*;
import com.example.demo.core.common.*;
import com.example.demo.features.products.repositories.*;
import com.example.demo.features.orders.repositories.*;
import com.example.demo.features.users.repositories.*;
import com.example.demo.features.vouchers.repositories.*;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    public record CloudinaryUploadResult(String imageUrl, String publicId) {
    }

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    @Value("${cloudinary.folder:project-cnpm/products}")
    private String cloudinaryFolder;

    public CloudinaryUploadResult uploadProductImage(MultipartFile file, String productId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        if (!StringUtils.hasText(cloudinaryUrl)) {
            throw new IllegalStateException("Cloudinary is not configured. Please set CLOUDINARY_URL");
        }

        try {
            Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
            cloudinary.config.secure = true;

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", cloudinaryFolder + "/" + productId,
                            "resource_type", "image",
                            "overwrite", false,
                            "unique_filename", true
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            Object url = uploadResult.get("url");
            Object publicId = uploadResult.get("public_id");

            String imageUrl = secureUrl != null ? secureUrl.toString() : (url != null ? url.toString() : null);
            if (StringUtils.hasText(imageUrl) && publicId != null) {
                return new CloudinaryUploadResult(imageUrl, publicId.toString());
            }

            throw new IllegalStateException("Cloudinary upload succeeded but no URL/public_id was returned");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to upload image to Cloudinary", ex);
        }
    }

    public void deleteProductImage(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            throw new IllegalArgumentException("Cloudinary public id is required");
        }
        if (!StringUtils.hasText(cloudinaryUrl)) {
            throw new IllegalStateException("Cloudinary is not configured. Please set CLOUDINARY_URL");
        }

        try {
            Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
            cloudinary.config.secure = true;

            Map<?, ?> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            Object result = deleteResult.get("result");
            if (result == null) {
                throw new IllegalStateException("Cloudinary delete returned no result");
            }

            String normalized = result.toString();
            if (!"ok".equalsIgnoreCase(normalized) && !"not found".equalsIgnoreCase(normalized)) {
                throw new IllegalStateException("Cloudinary delete failed: " + normalized);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete image from Cloudinary", ex);
        }
    }
}
