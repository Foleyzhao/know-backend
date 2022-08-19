package com.cumulus.modules.security.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.cumulus.modules.system.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT用户传输对象
 */
@Getter
@AllArgsConstructor
public class JwtUserDto implements UserDetails {

    private static final long serialVersionUID = -215978805302946709L;

    /**
     * 用户
     */
    private final UserDto user;

    /**
     * 数据权限列表
     */
    private final List<Long> dataScopes;

    /**
     * 权限列表
     */
    @JSONField(serialize = false)
    private final List<GrantedAuthority> authorities;

    /**
     * 获取用户权限
     *
     * @return 权限列表
     */
    public Set<String> getRoles() {
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @Override
    @JSONField(serialize = false)
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @JSONField(serialize = false)
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    @JSONField(serialize = false)
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JSONField(serialize = false)
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JSONField(serialize = false)
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JSONField(serialize = false)
    public boolean isEnabled() {
        return user.getEnabled();
    }

}
