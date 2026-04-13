package com.sakura.boot_init.agreement.model.dto;

import com.sakura.boot_init.support.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 协议分页查询请求。
 *
 * @author Sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AgreementQueryRequest extends PageRequest implements Serializable {

    /**
     * 协议 id。
     */
    private Long id;

    /**
     * 协议类型编码。
     */
    private String agreementType;

    /**
     * 协议标题。
     */
    private String title;

    /**
     * 状态。
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
