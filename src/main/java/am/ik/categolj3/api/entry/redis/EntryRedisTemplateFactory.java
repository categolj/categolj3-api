package am.ik.categolj3.api.entry.redis;

import am.ik.categolj3.api.entry.Entry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.util.Arrays;

@Data
public class EntryRedisTemplateFactory {
    final RedisConnectionFactory redisConnectionFactory;
    final ObjectMapper objectMapper;

    public RedisTemplate<Object, Object> create() {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new JdkSerializationRedisSerializer());
        template.setValueSerializer(new RedisSerializer<Entry>() {
            @Override
            public byte[] serialize(Entry entry) throws SerializationException {
                if (entry == null) {
                    return new byte[0];
                }
                try {
                    return objectMapper.writeValueAsBytes(entry);
                } catch (JsonProcessingException e) {
                    throw new SerializationException("Cannot serialize " + entry, e);
                }
            }

            @Override
            public Entry deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(bytes, Entry.class);
                } catch (IOException e) {
                    throw new SerializationException("Cannot deserialize " + Arrays.toString(bytes), e);
                }
            }
        });
        return template;
    }
}
