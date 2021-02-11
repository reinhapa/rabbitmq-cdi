/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reini.rabbitmq.cdi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Specialized encoder that encodes a event type into Json using Jackson {@link ObjectMapper}.
 * 
 * @author André Ignacio
 */
public final class JsonEncoder<T> implements Encoder<T> {
  private static final String CONTENT_TYPE = "application/json";
  private final ObjectMapper mapper;

  public JsonEncoder() {
    mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @Override
  public byte[] encode(T object) throws EncodeException {
    try {
      return mapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new EncodeException(e);
    }
  }

  @Override
  public String contentType() {
    return CONTENT_TYPE;
  }
}
