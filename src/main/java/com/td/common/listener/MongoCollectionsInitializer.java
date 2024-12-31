package com.td.common.listener;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MongoCollectionsInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("初始化 MongoDB 集合");
        Reflections reflections = new Reflections("com.td.common.pojo"); // 替换为你的包名
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Document.class);

        for (Class<?> clazz : annotatedClasses) {
            Document document = clazz.getAnnotation(Document.class);
            String collectionName = document.collection();
            if (!mongoTemplate.collectionExists(collectionName)) {
                System.out.println("创建集合：" + collectionName);
                mongoTemplate.createCollection(collectionName);
            }
        }
    }
}
