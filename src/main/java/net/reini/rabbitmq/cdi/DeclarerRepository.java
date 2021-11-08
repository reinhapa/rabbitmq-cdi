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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.Channel;

class DeclarerRepository {
    private final Map<Class<?>, Declarer<? extends Declaration>> declarerMap;

  DeclarerRepository() {
    this.declarerMap = new HashMap<>();
    this.declarerMap.put(ExchangeDeclaration.class, new ExchangeDeclarer());
    this.declarerMap.put(QueueDeclaration.class, new QueueDeclarer());
    this.declarerMap.put(BindingDeclaration.class, new BindingDeclarer());
  }

  @SuppressWarnings("unchecked")
  void declare(Channel channel, List<? extends Declaration> declarations) throws IOException {
    for (Declaration declaration : declarations) {
      Class<? extends Declaration> aClass = declaration.getClass();
      @SuppressWarnings({"rawtypes"})
      Declarer declarer = declarerMap.get(aClass);
      declarer.declare(channel, declaration);
    }
  }

}