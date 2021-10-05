/*
 * This file is part of the Botrino project and is licensed under the MIT license.
 *
 * Copyright (c) 2021 Alexandre Miranda
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
package botrino.interaction.listener;

import botrino.interaction.annotation.ComponentCommand;
import botrino.interaction.context.ButtonInteractionContext;
import botrino.interaction.context.ComponentInteractionContext;
import botrino.interaction.context.SelectMenuInteractionContext;
import botrino.interaction.cooldown.Cooldown;
import botrino.interaction.privilege.Privilege;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.Function;

public interface ComponentInteractionListener<R> extends InteractionListener {

    static <R>
    ComponentInteractionListener<R> button(String customId,
                                           Function<? super ButtonInteractionContext, ? extends Publisher<R>> run) {
        return ComponentInteractionListener.<R>builder(customId).setRunButton(run).build();
    }

    static <R>
    ComponentInteractionListener<R> selectMenu(String customId,
                                               Function<? super SelectMenuInteractionContext,
                                                       ? extends Publisher<R>> run) {
        return ComponentInteractionListener.<R>builder(customId).setRunSelectMenu(run).build();
    }

    static <R> Builder<R> builder(String customId) {
        return new Builder<>(customId);
    }

    default String customId() {
        final var annot = getClass().getAnnotation(ComponentCommand.class);
        if (annot == null) {
            throw new IllegalStateException("Missing customId. Either add @ComponentCommand annotation or override " +
                    "the customId() method.");
        }
        return annot.value();
    }

    default Publisher<R> run(ComponentInteractionContext ctx) {
        if (ctx instanceof ButtonInteractionContext) {
            return run((ButtonInteractionContext) ctx);
        }
        if (ctx instanceof SelectMenuInteractionContext) {
            return run((SelectMenuInteractionContext) ctx);
        }
        return Mono.empty();
    }

    default Publisher<R> run(ButtonInteractionContext ctx) {
        return Mono.empty();
    }

    default Publisher<R> run(SelectMenuInteractionContext ctx) {
        return Mono.empty();
    }

    final class Builder<R> {

        private final String customId;
        private Function<? super ButtonInteractionContext, ? extends Publisher<R>> runButton;
        private Function<? super SelectMenuInteractionContext, ? extends Publisher<R>> runSelectMenu;
        private Privilege privilege;
        private Cooldown cooldown;

        private Builder(String customId) {
            this.customId = customId;
        }

        public Builder<R> setRunButton(@Nullable Function<? super ButtonInteractionContext,
                ? extends Publisher<R>> runButton) {
            this.runButton = runButton;
            return this;
        }

        public Builder<R> setRunSelectMenu(@Nullable Function<? super SelectMenuInteractionContext,
                ? extends Publisher<R>> runSelectMenu) {
            this.runSelectMenu = runSelectMenu;
            return this;
        }

        public Builder<R> setPrivilege(@Nullable Privilege privilege) {
            this.privilege = privilege;
            return this;
        }

        public Builder<R> setCooldown(@Nullable Cooldown cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public ComponentInteractionListener<R> build() {
            return new ComponentInteractionListener<>() {
                @Override
                public String customId() {
                    return customId;
                }

                @Override
                public Publisher<R> run(ButtonInteractionContext ctx) {
                    return runButton == null ? ComponentInteractionListener.super.run(ctx) : runButton.apply(ctx);
                }

                @Override
                public Publisher<R> run(SelectMenuInteractionContext ctx) {
                    return runSelectMenu == null ? ComponentInteractionListener.super.run(ctx) :
                            runSelectMenu.apply(ctx);
                }

                @Override
                public Privilege privilege() {
                    return Objects.requireNonNullElse(privilege, ComponentInteractionListener.super.privilege());
                }

                @Override
                public Cooldown cooldown() {
                    return Objects.requireNonNullElse(cooldown, ComponentInteractionListener.super.cooldown());
                }

                @Override
                public String toString() {
                    return "ComponentInteractionListener{customId=" + customId + "}";
                }
            };
        }
    }
}