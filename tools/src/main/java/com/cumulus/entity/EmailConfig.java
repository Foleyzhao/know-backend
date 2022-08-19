package com.cumulus.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

/**
 * 邮件配置类
 *
 * @author zhaoff
 */
@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "tool_email_config")
public class EmailConfig implements Serializable {

    private static final long serialVersionUID = -8574476350257416760L;

    /**
     * ID
     */
    @Id
    private Long id;

    /**
     * 邮件服务器SMTP地址
     */
    @NotBlank
    private String host;

    /**
     * 邮件服务器SMTP端口
     */
    @NotBlank
    private String port;

    /**
     * 发件者用户名
     */
    @NotBlank
    private String user;

    /**
     * 密码
     */
    @NotBlank
    private String pass;

    /**
     * 收件人
     */
    @NotBlank
    private String fromUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EmailConfig that = (EmailConfig) o;
        return null != id && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
