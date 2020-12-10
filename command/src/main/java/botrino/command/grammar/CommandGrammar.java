/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2020 Alexandre Miranda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package botrino.command.grammar;

import botrino.api.util.ConfigUtils;
import botrino.command.CommandContext;
import botrino.command.InvalidSyntaxException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Allows to define a grammar for a command. A grammar defines the sequence of arguments expected by the command. A
 * sequence starts with zero, one or more <b>required</b> arguments, followed by zero, one or more <b>optional</b>
 * arguments. The last argument may be repeated if {@link Builder#setVarargs(boolean)} is set to {@code true}. The
 * grammar can be resolved when the command is run, the results of the resolution are stored in the fields of an
 * internal value class.
 *
 * @param <T> the type of the internal class that receives the resolved arguments
 */
public final class CommandGrammar<T> {

    private static final Object NULL = new Object();
    private final List<Token> tokens;
    private final boolean varargs;
    private final Function<CommandContext, Mono<T>> resolver;
    private String format;

    private CommandGrammar(List<Token> tokens, boolean varargs, Function<CommandContext, Mono<T>> resolver) {
        this.tokens = tokens;
        this.varargs = varargs;
        this.resolver = resolver;
    }

    /**
     * Creates a new builder to start composing a grammar.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Resolves this grammar against the given {@link CommandContext}. It will convert all arguments and construct an
     * instance of the internal class with the parsed arguments.
     *
     * @param ctx the command context
     * @return a {@link Mono} emitting the instance of the internal class with the parsed arguments
     */
    public Mono<T> resolve(CommandContext ctx) {
        return resolver.apply(ctx);
    }

    /**
     * Constructs a formatted string representing the sequence of arguments of this grammar, where each required
     * argument is wrapped in angle brackets, each optional argument is wrapped in square brackets, and three dots are
     * added after the last argument if varargs support is enabled for this grammar.
     *
     * @return a formatted string representation of this grammar
     */
    @Override
    public String toString() {
        if (format != null) {
            return format;
        }
        var sj = new StringJoiner(" ");
        var i = 0;
        for (var token : tokens) {
            var name = token.name;
            if (varargs && i == tokens.size() - 1) {
                name = "[" + name + "...]";
            } else if (token.optional) {
                name = "[" + name + "]";
            } else {
                name = "<" + name + ">";
            }
            sj.add(name);
            i++;
        }
        return format = sj.toString();
    }

    public static final class Builder {

        private final List<Token> tokens = new ArrayList<>();
        private boolean optionalFlag;
        private boolean varargs;

        private Builder() {}

        /**
         * Appends a new argument to the sequence with the given name and mapper. The argument will be optional if
         * {@link #beginOptional()} was invoked on this builder beforehand.
         *
         * @param argName the name of the argument, corresponding to a field in the receiver internal class
         * @param mapper  the mapper that defines the transformation of the argument to a target type
         * @return this builder
         */
        public Builder nextArgument(String argName, ArgumentMapper<?> mapper) {
            tokens.add(new Token(argName, mapper, optionalFlag));
            return this;
        }

        /**
         * Appends a new argument to the sequence with the given name, preserving the raw argument as-is. It is
         * equivalent to:
         * <pre>nextArgument(argName, ArgumentMapper.identity())</pre>
         *
         * @param argName the name of the argument, corresponding to a field in the receiver internal class
         * @return this builder
         */
        public Builder nextArgument(String argName) {
            return nextArgument(argName, ArgumentMapper.identity());
        }

        /**
         * Modifies the state of this builder in a such way that all future invocations of {@link #nextArgument(String,
         * ArgumentMapper)} or {@link #nextArgument(String)} on this builder will be considered as optional arguments,
         * meaning they may not be specified by the user and result in null fields in the resulting internal class
         * instance.
         *
         * @return this builder
         */
        public Builder beginOptional() {
            this.optionalFlag = true;
            return this;
        }

        /**
         * Sets whether the last argument of the grammar is able to be repeated multiple times. If set to {@code true},
         * the last argument will always be optional even if {@link #beginOptional()} was never called.
         *
         * @param varargs whether to enable varargs support
         * @return this builder
         */
        public Builder setVarargs(boolean varargs) {
            this.varargs = varargs;
            return this;
        }

        /**
         * Finalizes the construction of the {@link CommandGrammar} based on the current state of this builder.
         *
         * @param valueClass the internal class to be instantiated when the constructed grammar will be resolved
         * @param <T>        the type of the internal class
         * @return this builder
         */
        public <T> CommandGrammar<T> build(Class<T> valueClass) {
            return new CommandGrammar<>(tokens, varargs, ctx -> {
                var args = ctx.input().getArguments();
                var monos = new ArrayList<Mono<?>>();
                for (var i = 0; i < tokens.size(); i++) {
                    var token = tokens.get(i);
                    if (i == tokens.size() - 1) {
                        if (varargs) {
                            var vars = new ArrayList<Mono<?>>();
                            for (var a = i; args.size() - vars.size() >= tokens.size(); a++) {
                                var arg = args.get(a);
                                vars.add(token.mapper.map(ctx, arg)
                                        .onErrorMap(e -> new InvalidSyntaxException(token.name, arg, e)));
                            }
                            monos.add(Flux.concat(vars).collectList().defaultIfEmpty(List.of()));
                            break;
                        } else {
                            args = ctx.input().getArguments(tokens.size());
                        }
                    }
                    if (i < args.size()) {
                        var arg = args.get(i);
                        monos.add(tokens.get(i).mapper.map(ctx, arg)
                                .onErrorMap(e -> new InvalidSyntaxException(token.name, arg, e)));
                    } else {
                        if (!token.optional) {
                            return Mono.error(new InvalidSyntaxException(token.name, null, null));
                        }
                        monos.add(Mono.just(NULL));
                    }
                }
                return Mono.zip(monos, List::of).map(list -> {
                    try {
                        var value = ConfigUtils.instantiate(valueClass);
                        for (var i = 0; i < tokens.size(); i++) {
                            var token = tokens.get(i);
                            var o = list.get(i);
                            var field = valueClass.getDeclaredField(token.name);
                            field.setAccessible(true);
                            field.set(value, o == NULL ? null : o);
                        }
                        return value;
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
    }

    private static final class Token {
        private final String name;
        private final ArgumentMapper<?> mapper;
        private final boolean optional;

        private Token(String name, ArgumentMapper<?> mapper, boolean optional) {
            this.name = name;
            this.mapper = mapper;
            this.optional = optional;
        }
    }
}
