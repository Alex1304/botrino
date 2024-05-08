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
package botrino.interaction.annotation;

import botrino.interaction.listener.ChatInputInteractionListener;
import discord4j.rest.util.Permission;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides meta-information on a chat input-based command (also called "slash command"). If there are no subcommands,
 * the annotated class is expected to implement {@link ChatInputInteractionListener}. Otherwise, listeners are specified
 * for each subcommand via the @{@link Subcommand} annotation, and the class annotated with this annotation is not
 * required to implement any interface.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ChatInputCommand {

    /**
     * The name of the command.
     *
     * @return the name
     */
    String name();

    /**
     * The description of the command.
     *
     * @return the description
     */
    String description();

    /**
     * The default permissions guild members should have in order to use this command.
     *
     * @return the permissions
     */
    Permission[] defaultMemberPermissions() default {};

    /**
     * Whether to allow the use of this command in DMs. If false, the command can only be used in guilds.
     *
     * @return true if command is allowed in DMs
     */
    boolean allowInDMs() default true;

    /**
     * The list of subcommands for this command, if any.
     *
     * @return an array of subcommand annotations
     */
    Subcommand[] subcommands() default {};

    /**
     * The list of subcommand groups for this command, if any.
     *
     * @return an array of subcommand group annotations
     */
    SubcommandGroup[] subcommandGroups() default {};
}
