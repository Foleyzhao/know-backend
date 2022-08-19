package com.cumulus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 发送邮件vo类
 *
 * @author zhaoff
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailVo {

    /**
     * 收件人，支持多个收件人
     */
    @NotEmpty
    private List<String> tos;

    /**
     * 邮件主题
     */
    @NotBlank
    private String subject;

    /**
     * 邮件内容
     */
    @NotBlank
    private String content;

}
