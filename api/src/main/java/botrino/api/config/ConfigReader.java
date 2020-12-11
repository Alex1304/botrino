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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Allows to customize the way Botrino reads the configuration.
 */
public interface ConfigReader {

    /**
     * Loads the configuration from a certain JSON source, suggested by the provided {@link Path} which corresponds to
     * the bot's home directory. The implementation of this method is expected to be synchronous and blocking: the bot
     * hasn't started at this point, loading the configuration is one of the very first things that are executed when
     * the main method is launched, as a result this method will execute on the main thread of the program.
     *
     * <p>
     * By default, reads a file named config.json at the root of the bot's directory.
     *
     * @param botDirectory the bot's home directory
     * @return the raw JSON data of the configuration
     */
    default String loadConfigJson(Path botDirectory) throws IOException {
        return Files.readString(botDirectory.resolve("config.json"));
    }

    /**
     * Creates the object mapper instance to use to parse the configuration JSON.
     *
     * <p>
     * By default, creates an empty {@link ObjectMapper} with only the {@link Jdk8Module} registered.
     *
     * @return a new {@link ObjectMapper}
     */
    default ObjectMapper createConfigObjectMapper() {
        return new ObjectMapper().registerModule(new Jdk8Module());
    }
}
