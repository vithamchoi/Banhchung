package com.quannhabaninh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySectionRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
    private String imageUrl;

    @Size(max = 200, message = "Mô tả hình ảnh không được vượt quá 200 ký tự")
    private String imageAlt;

    @NotNull(message = "Thứ tự hiển thị không được để trống")
    private Integer displayOrder;

    private Boolean isActive;

    @NotBlank(message = "Loại section không được để trống")
    @Size(max = 50, message = "Loại section không được vượt quá 50 ký tự")
    private String sectionType;

    @Size(max = 500, message = "Phụ đề không được vượt quá 500 ký tự")
    private String subtitle;

    private String highlightedText;

    @Size(max = 500, message = "URL hình ảnh thứ 2 không được vượt quá 500 ký tự")
    private String secondImageUrl;

    @Size(max = 200, message = "Mô tả hình ảnh thứ 2 không được vượt quá 200 ký tự")
    private String secondImageAlt;
}
