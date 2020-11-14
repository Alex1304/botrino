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
package botrino.api.config;

import com.google.gson.GsonBuilder;

/**
 * A service implementing this interface can define a new configuration entry that can be specified in the configuration
 * file of the bot.
 *
 * @param <C> the type of the object holding the configuration, used by gson to determine the target type
 */
@SuppressWarnings("unused")
public interface ConfigEntry<C extends ConfigObject> {

    /**
     * Defines a new configuration entry. It can mutate the given gsonBuilder by adding new adapters to construct the
     * configuration object, and returns the name of the key corresponding to this entry in the json file.
     *
     * @param gsonBuilder the gsonBuilder to mutate in order to add new adapters if necessary
     * @return the name of the key corresponding to this entry in the json file
     */
    String define(GsonBuilder gsonBuilder);
}
