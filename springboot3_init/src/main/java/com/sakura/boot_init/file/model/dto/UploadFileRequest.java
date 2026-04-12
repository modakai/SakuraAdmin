package com.sakura.boot_init.file.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 鏂囦欢涓婁紶璇锋眰
 *
 * @author sakura
 * @from sakura
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 涓氬姟
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}

