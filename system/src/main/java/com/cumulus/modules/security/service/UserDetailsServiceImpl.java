package com.cumulus.modules.security.service;

import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityNotFoundException;
import com.cumulus.modules.security.config.bean.LoginProperties;
import com.cumulus.modules.security.dto.JwtUserDto;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.service.DataService;
import com.cumulus.modules.system.service.RoleService;
import com.cumulus.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户详情服务
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 用户服务接口
     */
    @Autowired
    private UserService userService;

    /**
     * 角色服务接口
     */
    @Autowired
    private RoleService roleService;

    /**
     * 数据权限服务接口
     */
    @Autowired
    private DataService dataService;

    /**
     * 登录配置信息
     */
    @Autowired
    private LoginProperties loginProperties;

    /**
     * 用户信息缓存
     */
    final static Map<String, Future<JwtUserDto>> USER_DTO_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置用户登录信息是否缓存
     *
     * @param enableCache 用户登录信息是否缓存
     */
    public void setEnableCache(boolean enableCache) {
        this.loginProperties.setCacheEnable(enableCache);
    }

    /**
     * 获取JWT用户传输对象线程池
     */
    public static ExecutorService executor = newThreadPool();

    @Override
    public JwtUserDto loadUserByUsername(String username) {
        JwtUserDto jwtUserDto = null;
        Future<JwtUserDto> future = USER_DTO_CACHE.get(username);
        if (!loginProperties.isCacheEnable()) {
            UserDto user;
            try {
                user = userService.findByName(username);
            } catch (EntityNotFoundException e) {
                // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
                throw new UsernameNotFoundException("用户不存在", e);
            }
            if (null == user) {
                throw new UsernameNotFoundException("用户不存在");
            } else {
                if (!user.getEnabled()) {
                    throw new BadRequestException("账号未激活！");
                }
                jwtUserDto = new JwtUserDto(user, dataService.getDeptIds(user),
                        roleService.mapToGrantedAuthorities(user));
            }
            return jwtUserDto;
        }

        if (null == future) {
            Callable<JwtUserDto> call = () -> getJwtBySearchDb(username);
            FutureTask<JwtUserDto> ft = new FutureTask<>(call);
            future = USER_DTO_CACHE.putIfAbsent(username, ft);
            if (null == future) {
                future = ft;
                executor.submit(ft);
            }
            try {
                return future.get();
            } catch (CancellationException e) {
                USER_DTO_CACHE.remove(username);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getCause().getMessage());
            }
        } else {
            try {
                jwtUserDto = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e.getCause().getMessage());
            }
            // 检查dataScope是否修改
            List<Long> dataScopes = jwtUserDto.getDataScopes();
            dataScopes.clear();
            dataScopes.addAll(dataService.getDeptIds(jwtUserDto.getUser()));
        }
        return jwtUserDto;
    }

    /**
     * 根据用户名获取JWT用户传输对象
     *
     * @param username 用户名
     * @return JWT用户传输对象
     */
    private JwtUserDto getJwtBySearchDb(String username) {
        UserDto user;
        try {
            user = userService.findByName(username);
        } catch (EntityNotFoundException e) {
            // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
            throw new UsernameNotFoundException("用户不存在", e);
        }
        if (null == user) {
            throw new UsernameNotFoundException("用户不存在");
        } else {
            if (!user.getEnabled()) {
                throw new BadRequestException("账号未激活！");
            }
            return new JwtUserDto(user, dataService.getDeptIds(user), roleService.mapToGrantedAuthorities(user));
        }

    }

    /**
     * 初始化获取JWT用户传输对象线程池
     *
     * @return 获取JWT用户传输对象线程池
     */
    public static ExecutorService newThreadPool() {
        ThreadFactory namedThreadFactory = new ThreadFactory() {

            final AtomicInteger sequence = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                int seq = this.sequence.getAndIncrement();
                thread.setName("future-task-thread" + (seq > 1 ? "-" + seq : ""));
                if (!thread.isDaemon()) {
                    thread.setDaemon(true);
                }
                return thread;
            }
        };
        return new ThreadPoolExecutor(10, 200, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }
}
