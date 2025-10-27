package com.cakestore.cakestore.mapper;

import com.cakestore.cakestore.dto.user.ProductCardDto;
import com.cakestore.cakestore.dto.user.ProductDetailDto;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.ProductImage;
import com.cakestore.cakestore.entity.ProductStats;
// import com.cakestore.cakestore.entity.ProductVariant;
import org.hibernate.Hibernate;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ProductMapper {

        private static final Comparator<ProductImage> IMG_ORDER = Comparator.comparing(ProductImage::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo));

        public static ProductCardDto toCard(Product p, ProductStats s) {
                String thumb = null;

                // CHỈ chạm vào images nếu đã được nạp sẵn (vd: @EntityGraph trong repo hoặc
                // đang ở trong @Transactional)
                if (p.getImages() != null && Hibernate.isInitialized(p.getImages()) && !p.getImages().isEmpty()) {
                        thumb = p.getImages().stream()
                                        .filter(Objects::nonNull)
                                        .sorted(IMG_ORDER)
                                        .map(ProductImage::getUrl)
                                        .filter(Objects::nonNull)
                                        .findFirst()
                                        .orElse(null);
                }

                return new ProductCardDto(
                                p.getId(),
                                p.getName(),
                                p.getSlug(),
                                p.getPrice(),
                                thumb,
                                s != null ? s.getSoldCount() : null,
                                s != null ? s.getRatingAvg() : null,
                                s != null ? s.getRatingCount() : null);
        }

        public static ProductDetailDto toDetail(Product p, ProductStats s) {
                List<String> imgs = List.of();
                if (p.getImages() != null && Hibernate.isInitialized(p.getImages()) && !p.getImages().isEmpty()) {
                        imgs = p.getImages().stream()
                                        .filter(Objects::nonNull)
                                        .sorted(IMG_ORDER)
                                        .map(ProductImage::getUrl)
                                        .filter(Objects::nonNull)
                                        .toList();
                }

                List<ProductDetailDto.VariantDto> variants = List.of();
                if (p.getVariants() != null && Hibernate.isInitialized(p.getVariants()) && !p.getVariants().isEmpty()) {
                        variants = p.getVariants().stream()
                                        .filter(Objects::nonNull)
                                        .map(v -> new ProductDetailDto.VariantDto(
                                                        v.getId(),
                                                        v.getAttrName(),
                                                        v.getAttrValue(),
                                                        v.getPriceAdj()))
                                        .toList();
                }

                return new ProductDetailDto(
                                p.getId(),
                                p.getName(),
                                p.getSlug(),
                                p.getPrice(),
                                p.getShortDesc(),
                                p.getDescription(),
                                imgs,
                                variants,
                                s != null ? s.getRatingAvg() : null,
                                s != null ? s.getRatingCount() : null);
        }
}
