
// Copyright 2018 Schibsted Marketplaces Products & Technology As
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.schibsted.spt.data.jslt.impl;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.JsltException;

public class FunctionWrapper implements Function {
  private String name;
  private Method method;
  private ToJavaConverter[] converters;
  private ToJsonConverter returnConverter;

  public FunctionWrapper(String name, Method method) {
    this.name = name;
    this.method = method;
    this.returnConverter = makeJsonConverter(method.getReturnType());

    Class[] paramTypes = method.getParameterTypes();
    this.converters = new ToJavaConverter[paramTypes.length];
    for (int ix = 0; ix < paramTypes.length; ix++)
      converters[ix] = makeJavaConverter(paramTypes[ix]);
  }

  public String getName() {
    return name;
  }

  public int getMinArguments() {
    return method.getParameterCount();
  }

  public int getMaxArguments() {
    return method.getParameterCount();
  }

  public JsonNode call(JsonNode input, JsonNode[] arguments) {
    Object[] args = new Object[arguments.length];
    for (int ix = 0; ix < arguments.length; ix++)
      args[ix] = converters[ix].convert(arguments[ix]);

    try {
      Object result = method.invoke(null, args);
      return returnConverter.convert(result);
    } catch (IllegalAccessException e) {
      throw new JsltException("Couldn't call " + method, e);
    } catch (InvocationTargetException e) {
      throw new JsltException("Couldn't call " + method, e);
    }
  }

  // ===== TO JAVA

  interface ToJavaConverter {
    public Object convert(JsonNode node);
  }

  private static Map<Class, ToJavaConverter> toJava = new HashMap();
  static {
    toJava.put(String.class, new StringJavaConverter());
  }

  private static ToJavaConverter makeJavaConverter(Class type) {
    ToJavaConverter converter = toJava.get(type);
    if (converter == null)
      throw new JsltException("Cannot build converter to " + type);
    return converter;
  }

  static class StringJavaConverter implements ToJavaConverter {
    public Object convert(JsonNode node) {
      if (node.isNull())
        return null;
      else if (node.isTextual())
        return node.asText();
      else
        throw new JsltException("Could not convert " + node + " to string");
    }
  }

  // ===== TO JSON

  interface ToJsonConverter {
    public JsonNode convert(Object node);
  }

  private static Map<Class, ToJsonConverter> toJson = new HashMap();
  static {
    toJson.put(String.class, new StringJsonConverter());
  }

  static private ToJsonConverter makeJsonConverter(Class type) {
    ToJsonConverter converter = toJson.get(type);
    if (converter == null)
      throw new JsltException("Cannot build converter from " + type);
    return converter;
  }

  static class StringJsonConverter implements ToJsonConverter {
    public JsonNode convert(Object node) {
      if (node == null)
        return NullNode.instance;
      else
        return new TextNode((String) node);
    }
  }
}
