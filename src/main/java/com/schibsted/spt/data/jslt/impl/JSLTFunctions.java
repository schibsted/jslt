package com.schibsted.spt.data.jslt.impl;

import com.schibsted.spt.data.jslt.Function;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface JSLTFunctions extends Module{

    default JSLTNamespace getNamespace() throws URISyntaxException {
        return new JSLTNamespace("", new URI("http://jslt.schibsted.com/2018/builtin"));
    }

    default String getPrefix() throws URISyntaxException {
        return getNamespace().getPrefix();
    }

    default URI getURI() throws URISyntaxException {
        return getNamespace().getURI();
    }

    Map<String, Function> functions();
    Map<String, Macro> macros();



    public static <T> Stream<T> iteratorToStream(final Iterator<T> iterator, final boolean parallell) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), parallell);
    }

    static class JSLTNamespace implements Serializable {
        final private String prefix;
        final private URI URI;

        public JSLTNamespace(String prefix, URI URI) {
            this.prefix = prefix;
            this.URI = URI;
        }

        public String getPrefix() {
            return prefix;
        }

        public URI getURI() {
            return URI;
        }
    }

    static abstract class AbstractCallable implements Callable {
        private String name;
        private int min;
        private int max;

        public AbstractCallable(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public String getName() {
            return name;
        }

        public int getMinArguments() {
            return min;
        }

        public int getMaxArguments() {
            return max;
        }
    }

    static abstract class AbstractFunction extends JSLTFunctions.AbstractCallable implements Function {

        public AbstractFunction(String name, int min, int max) {
            super(name, min, max);
        }
    }

    static abstract class AbstractMacro extends JSLTFunctions.AbstractCallable implements Macro {

        public AbstractMacro(String name, int min, int max) {
            super(name, min, max);
        }
    }
}
