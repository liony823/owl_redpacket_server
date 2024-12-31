package com.td.common.base;

import com.alibaba.fastjson.annotation.JSONField;
import com.td.common.pojo.SysUser;
import com.td.common.pojo.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Data
public class LoginUser implements UserDetails {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色对象
     */

    private SysUser sysUser;

    private User clientUser;

    private Integer userType; //用户类型 1:后台用户 0:客户端用户


    public LoginUser() {
    }

    public LoginUser(String userId, SysUser user, Set<String> permissions, Integer userType,User clientUser) {
        this.id = userId;
        this.sysUser = user;
        this.permissions = permissions;
        this.userType = userType;
        this.clientUser = clientUser;
    }

    public static LoginUser createSysUser(String userId, SysUser user, Set<String> permissions) {
        return new LoginUser(userId, user, permissions, 1,null);
    }

    public static LoginUser createClientUser(User clientUser) {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientUser(clientUser);
        loginUser.setUserType(0);
        return loginUser;
    }

    @JSONField(serialize = false)
    @Override
    public String getPassword() {
//        return sysUser == null ? clientUser.getPassword() : sysUser.getPassword();
        return null;
    }

    @Override
    public String getUsername() {
//        return sysUser == null ? clientUser.getUsername() : sysUser.getUsername();
        return null;
    }

    /**
     * 账户是否未过期,过期无法验证
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 指定用户是否解锁,锁定的用户无法进行身份验证
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 指示是否已过期的用户的凭据(密码),过期的凭据防止认证
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否可用 ,禁用的用户不能身份验证
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }
}
