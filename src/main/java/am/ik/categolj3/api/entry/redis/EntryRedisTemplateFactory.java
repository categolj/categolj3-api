/*
 * Copyright (C) 2015 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
