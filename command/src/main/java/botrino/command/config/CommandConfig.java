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
package botrino.command.config;

import botrino.api.config.ConfigObject;
import botrino.api.config.ValidationFailure;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class CommandConfig implements ConfigObject {

    private String commandPrefix;
    private String flagPrefix;

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public String getFlagPrefix() {
        return flagPrefix;
    }

    @Override
    public List<ValidationFailure> validate() {
        var failures = new ArrayList<ValidationFailure>();
        if (commandPrefix == null) {
            failures.add(ValidationFailure.missingField("command_prefix"));
        }
        if (commandPrefix != null && commandPrefix.isBlank()) {
            failures.add(ValidationFailure.blankField("command_prefix"));
        }
        if (flagPrefix == null) {
            failures.add(ValidationFailure.missingField("flag_prefix"));
        }
        if (flagPrefix != null && flagPrefix.isBlank()) {
            failures.add(ValidationFailure.blankField("flag_prefix"));
        }
        return failures;
    }
}
