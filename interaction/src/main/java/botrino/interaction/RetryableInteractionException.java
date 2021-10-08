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

import botrino.interaction.context.ComponentInteractionContext;
import botrino.interaction.context.InteractionContext;
import botrino.interaction.listener.ComponentInteractionListener;

/**
 * Exception that may be thrown inside a {@link ComponentInteractionListener#run(ComponentInteractionContext)} to
 * indicate that the user may retry a new interaction with the component. If the listener is passed into
 * {@link InteractionContext#awaitComponentInteraction(ComponentInteractionListener)}, the listener will be
 * registered again upon catching this exception.
 */
public class RetryableInteractionException extends RuntimeException {

    public RetryableInteractionException() {
    }

    public RetryableInteractionException(String message) {
        super(message);
    }

    public RetryableInteractionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryableInteractionException(Throwable cause) {
        super(cause);
    }

    public RetryableInteractionException(String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
