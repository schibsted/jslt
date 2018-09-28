
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
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
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
    toJava.put(int.class, new IntJavaConverter());
    toJava.put(long.class, new LongJavaConverter());
    toJava.put(boolean.class, new BooleanJavaConverter());
    toJava.put(double.class, new DoubleJavaConverter());
    toJava.put(float.class, new DoubleJavaConverter());
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

  static class LongJavaConverter implements ToJavaConverter {
    public Object convert(JsonNode node) {
      if (!node.isNumber())
        throw new JsltException("Cannot convert " + node + " to long");
      else
        return node.asLong();
    }
  }

  static class IntJavaConverter implements ToJavaConverter {
    public Object convert(JsonNode node) {
      if (!node.isNumber())
        throw new JsltException("Cannot convert " + node + " to int");
      else
        return node.asInt();
    }
  }

  static class BooleanJavaConverter implements ToJavaConverter {
    public Object convert(JsonNode node) {
      if (!node.isBoolean())
        throw new JsltException("Cannot convert " + node + " to boolean");
      else
        return node.asBoolean();
    }
  }

  static class DoubleJavaConverter implements ToJavaConverter {
    public Object convert(JsonNode node) {
      if (!node.isNumber())
        throw new JsltException("Cannot convert " + node + " to double");
      else
        return node.asDouble();
    }
  }

  // ===== TO JSON

  interface ToJsonConverter {
    public JsonNode convert(Object node);
  }

  private static Map<Class, ToJsonConverter> toJson = new HashMap();
  static {
    toJson.put(String.class, new StringJsonConverter());
    toJson.put(long.class, new LongJsonConverter());
    toJson.put(int.class, new IntJsonConverter());
    toJson.put(boolean.class, new BooleanJsonConverter());
    toJson.put(double.class, new DoubleJsonConverter());
    toJson.put(float.class, new FloatJsonConverter());
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

  static class LongJsonConverter implements ToJsonConverter {
    public JsonNode convert(Object node) {
      if (node == null)
        return NullNode.instance;
      else
        return new LongNode((Long) node);
    }
  }

  static class IntJsonConverter implements ToJsonConverter {
    public JsonNode convert(Object node) {
      if (node == null)
        return NullNode.instance;
      else
        return new IntNode((Integer) node);
    }
  }

  static class BooleanJsonConverter implements ToJsonConverter {
    public JsonNode convert(Object node) {
      if (node == null)
        return NullNode.instance;
      else if ((Boolean) node)
        return BooleanNode.TRUE;
      else
        return BooleanNode.FALSE;
    }
  }

  static class DoubleJsonConverter implements ToJsonConverter {
    public JsonNode convert(Object node) {
      if (node == null)
        return NullNode.instance;
      else
        return new DoubleNode((Double) node);
    }
  }

  static class FloatJsonConverter implements ToJsonConverter {
    public JsonNode convert(Object node) {
      if (node == null)
        return NullNode.instance;
      else
        return new FloatNode((Float) node);
    }
  }
}
