package com.td.common.base;

/**
 * 缓存的key 常量
 * 
 * @author Td
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 手机验证码
     */
    public static final String PHONE_CODE_KEY = "phone_verify:";

    /**
     * 通道参数配置
     */
    public static final String UPSTREAM_CHANNEL_CONF_CACHE_KEY = "upstream_channel_conf_value";

    /**
     * 关注二维码链接过期key
     */
    public static final String QRCODE_REGARD_URL_KEY = "qrcode_regard_url:";
}