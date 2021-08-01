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
package botrino.command;

import botrino.command.context.ButtonCommandContext;
import botrino.command.context.CommandContext;
import botrino.command.cooldown.Cooldown;
import botrino.command.privilege.Privilege;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Objects;
import java.util.function.Function;

public interface ButtonCommand extends InteractionCommand {

    static ButtonCommand of(String customId, Function<ButtonCommandContext, Mono<Void>> run) {
        return builder(customId, run).build();
    }

    static Builder builder(String customId, Function<ButtonCommandContext, Mono<Void>> run) {
        return new Builder(customId, run);
    }

    Mono<Void> run(ButtonCommandContext ctx);

    String customId();

    @Override
    default void register(CommandService commandService) {
        commandService.register(this);
    }

    @Override
    default void registerAsInteraction(CommandService commandService, CommandContext parentContext) {
        commandService.registerAsInteraction(this, parentContext);
    }

    final class Builder {

        private final String customId;
        private final Function<ButtonCommandContext, Mono<Void>> run;
        private Privilege privilege;
        private Cooldown cooldown;

        private Builder(String customId, Function<ButtonCommandContext, Mono<Void>> run) {
            this.customId = customId;
            this.run = run;
        }

        public Builder setPrivilege(@Nullable Privilege privilege) {
            this.privilege = privilege;
            return this;
        }

        public Builder setCooldown(@Nullable Cooldown cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public ButtonCommand build() {
            return new ButtonCommand() {
                @Override
                public Mono<Void> run(ButtonCommandContext ctx) {
                    return run.apply(ctx);
                }

                @Override
                public String customId() {
                    return customId;
                }

                @Override
                public Privilege privilege() {
                    return Objects.requireNonNullElse(privilege, ButtonCommand.super.privilege());
                }

                @Override
                public Cooldown cooldown() {
                    return Objects.requireNonNullElse(cooldown, ButtonCommand.super.cooldown());
                }

                @Override
                public String toString() {
                    return "ButtonCommand{customId=" + customId + "}";
                }
            };
        }
    }
}