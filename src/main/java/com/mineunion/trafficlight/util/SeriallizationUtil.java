package com.mineunion.trafficlight.util;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.Map;

// 通用序列化工具
public class SerializationUtil {
    // 注册可序列化类
    public static void registerSerializables() {
        // 如需自定义序列化，可在此注册
        // ConfigurationSerialization.registerClass(TrafficLightEntity.class);
    }

    // 序列化对象
    public static Map<String, Object> serialize(ConfigurationSerializable obj) {
        return obj.serialize();
    }

    // 反序列化对象
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Class<T> clazz, Map<String, Object> data) {
        return (T) ConfigurationSerialization.deserializeObject(data, clazz);
    }
}