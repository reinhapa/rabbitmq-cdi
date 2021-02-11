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

/**
 * The Decoder is responsible to convert a raw bytes message to a message object of the given type.
 *
 * @author Patrick Reinhart
 */
public interface Decoder<T> {
  /**
   * Decode the given bytes into an message object of type M.
   *
   * @param bytes the bytes to be decoded.
   * @return the decoded message object.
   * @throws DecodeException if the message decoding fails
   */
  T decode(byte[] bytes) throws DecodeException;

  /**
   * Answer whether the given content type can be decoded into an object of type T.
   *
   * @param contentType the content type to be decoded.
   * @return whether or not the bytes can be decoded by this decoder.
   */
  boolean willDecode(String contentType);
}
