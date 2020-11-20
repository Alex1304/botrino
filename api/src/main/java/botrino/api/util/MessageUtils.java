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
package botrino.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MessageUtils {

    private MessageUtils() {
        throw new AssertionError();
    }

    /**
     * Splits a message into several chunks which size is specified. If the chunk ends while the text is inside a
     * codeblock or a blockquote, proper markdown is added to make the message continuous across chunks. This does not
     * apply to inline markdown such as bold, italic or spoilers.
     *
     * @param superLongMessage the message to split
     * @param maxCharacters    the max characters that a single chunk may have
     * @return a List which elements are the chunks in the correct order
     */
    public static List<String> chunk(String superLongMessage, int maxCharacters) {
        var chunks = new ArrayList<String>();
        var currentChunk = new StringBuilder();
        var inCodeblock = false;
        for (var line : superLongMessage.lines().collect(Collectors.toList())) {
            inCodeblock = (line.startsWith("```") && !line.substring(3).contains("```")) != inCodeblock;
            if (currentChunk.length() + line.length() + 1 >= maxCharacters) {
                if (inCodeblock) {
                    currentChunk.append("```\n");
                }
                chunks.add(currentChunk.substring(0, Math.min(currentChunk.length(), maxCharacters)));
                currentChunk.delete(0, currentChunk.length());
            } else {
                if (!chunks.isEmpty() && currentChunk.length() == 0) {
                    if (inCodeblock) {
                        currentChunk.append("```\n");
                    }
                }
            }
            currentChunk.append(line);
            currentChunk.append('\n');
        }
        chunks.add(currentChunk.toString());
        return chunks;
    }

    /**
     * Splits a message into several chunks. Each chunk can have a max size of 1000.
     *
     * @param superLongMessage the message to split
     * @return a List which elements are the chunks in the correct order
     */
    public static List<String> chunk(String superLongMessage) {
        return chunk(superLongMessage, 1000);
    }
}
