package com.cakestore.cakestore.mapper;

import com.cakestore.cakestore.dto.ProductCardDto;
import com.cakestore.cakestore.dto.ProductDetailDto;
import com.cakestore.cakestore.entity.*;

import java.util.List;

public class ProductMapper {

    public static ProductCardDto toCard(Product p, ProductStats s) {
        String thumb = (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().stream().sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .findFirst().map(ProductImage::getUrl).orElse(null)
                : null;

        return new ProductCardDto(
                p.getId(), p.getName(), p.getSlug(), p.getPrice(), thumb,
                s != null ? s.getSoldCount() : null,
                s != null ? s.getRatingAvg() : null,
                s != null ? s.getRatingCount() : null);
    }

    public static ProductDetailDto toDetail(Product p, ProductStats s) {
        List<String> imgs = p.getImages() == null ? List.of()
                : p.getImages().stream().sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .map(ProductImage::getUrl).toList();

        List<ProductDetailDto.VariantDto> variants = p.getVariants() == null ? List.of()
                : p.getVariants().stream()
                        .map(v -> new ProductDetailDto.VariantDto(
                                v.getId(), v.getAttrName(), v.getAttrValue(), v.getPriceAdj()))
                        .toList();

        return new ProductDetailDto(
                p.getId(), p.getName(), p.getSlug(), p.getPrice(),
                p.getShortDesc(), p.getDescription(), imgs, variants,
                s != null ? s.getRatingAvg() : null,
                s != null ? s.getRatingCount() : null);
    }
}
