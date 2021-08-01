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
package botrino.interaction;

import botrino.interaction.context.ButtonContext;
import botrino.interaction.context.InteractionContext;
import botrino.interaction.cooldown.Cooldown;
import botrino.interaction.privilege.Privilege;
import org.reactivestreams.Publisher;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.Function;

public interface ButtonInteraction<R> extends ComponentInteraction<ButtonContext, R> {

    static <R> ButtonInteraction<R> of(String customId, Function<? super ButtonContext, ? extends Publisher<R>> run) {
        return builder(customId, run).build();
    }

    static <R> Builder<R>  builder(String customId, Function<? super ButtonContext, ? extends Publisher<R>> run) {
        return new Builder<>(customId, run);
    }

    String customId();

    @Override
    default void register(InteractionService interactionService) {
        interactionService.register(this);
    }

    @Override
    default void registerSingleUse(InteractionService interactionService, InteractionContext parentContext) {
        interactionService.registerSingleUse(this, parentContext);
    }

    final class Builder<R>  {

        private final String customId;
        private final Function<? super ButtonContext, ? extends Publisher<R>> run;
        private Privilege privilege;
        private Cooldown cooldown;

        private Builder(String customId, Function<? super ButtonContext, ? extends Publisher<R>> run) {
            this.customId = customId;
            this.run = run;
        }

        public Builder<R> setPrivilege(@Nullable Privilege privilege) {
            this.privilege = privilege;
            return this;
        }

        public Builder<R> setCooldown(@Nullable Cooldown cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public ButtonInteraction<R> build() {
            return new ButtonInteraction<>() {
                @Override
                public Publisher<R> run(ButtonContext ctx) {
                    return run.apply(ctx);
                }

                @Override
                public String customId() {
                    return customId;
                }

                @Override
                public Privilege privilege() {
                    return Objects.requireNonNullElse(privilege, ButtonInteraction.super.privilege());
                }

                @Override
                public Cooldown cooldown() {
                    return Objects.requireNonNullElse(cooldown, ButtonInteraction.super.cooldown());
                }

                @Override
                public String toString() {
                    return "ButtonInteraction{customId=" + customId + "}";
                }
            };
        }
    }
}