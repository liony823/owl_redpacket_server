package com.td.common.config;

import com.td.common.annotations.Anonymous;
import lombok.NonNull;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Td
 * @description
 * 设置Anonymous注解允许匿名访问的url
 * 实现 InitializingBean 接口： 这意味着在 Spring Bean 实例化后，afterPropertiesSet 方法将被自动调用。
 * 在这个方法中，该 Bean 将从应用程序上下文中获取 RequestMappingHandlerMapping 实例，
 * 并通过遍历所有的 RequestMappingInfo 来收集 URL。
 * 实现 ApplicationContextAware 接口： 这使得该 Bean 可以意识到它所在的应用程序上下文。
 * 通过实现这个接口，可以在 setApplicationContext 方法中将应用程序上下文的引用保存在实例变量 applicationContext 中。

 * 收集允许匿名访问的 URL： 通过遍历应用程序上下文中的所有 RequestMappingInfo，
 * 该 Bean 获取每个 URL 对应的 HandlerMethod，然后检查该方法或其所在的控制器类是否标有 @Anonymous 注解。如
 * 果标有 @Anonymous 注解，则将该 URL 添加到允许匿名访问的 URL 列表中。在添加 URL 之前，还使用正则表达式将路径变量替换为 *。
 * 提供 getUrls 方法： 这个方法允许其他组件获取允许匿名访问的 URL 列表。
 * 总体来说，这段代码的目的是根据 @Anonymous 注解的配置，动态地收集允许匿名访问的 URL 列表，
 * 这些 URL 列表后续可以在其他地方使用，比如在 Spring Security 的配置中设置匿名访问权限。
 */
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware {
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private ApplicationContext applicationContext;

    private List<String> urls = new ArrayList<>();

    public final String ASTERISK = "*";

    @Override
    public void afterPropertiesSet() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

        map.keySet().forEach(info -> {
            HandlerMethod handlerMethod = map.get(info);

            // 获取方法上边的注解 替代path variable 为 *
            Anonymous method = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
            Optional.ofNullable(method).ifPresent(anonymous -> info.getPatternsCondition().getPatterns()
                    .forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK))));

            // 获取类上边的注解, 替代path variable 为 *
            Anonymous controller = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
            Optional.ofNullable(controller).ifPresent(anonymous -> info.getPatternsCondition().getPatterns()
                    .forEach(url -> urls.add(RegExUtils.replaceAll(url, PATTERN, ASTERISK))));
        });
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
