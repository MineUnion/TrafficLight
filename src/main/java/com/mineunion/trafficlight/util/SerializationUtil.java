package com.mineunion.trafficlight.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.Map;

// 修复：泛型约束精准匹配ConfigurationSerializable
public class SerializationUtil {

    // 注册可序列化类
    public static void registerSerializables() {
        // 示例：注册自定义实体类（如需序列化TrafficLightEntity，取消注释）
        // ConfigurationSerialization.registerClass(TrafficLightEntity.class);
    }

    // 序列化对象
    public static Map<String, Object> serialize(ConfigurationSerializable obj) {
        return obj.serialize();
    }

    // 修复：泛型T必须继承ConfigurationSerializable，解决类型转换错误
    @SuppressWarnings("unchecked")
    public static <T extends ConfigurationSerializable> T deserialize(Class<T> clazz, Map<String, Object> data) {
        // 强制类型转换，适配Bukkit的反序列化方法
        return (T) ConfigurationSerialization.deserializeObject(data, clazz);
    }
}