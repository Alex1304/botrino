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
package botrino.command;

import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

class CommandTree {

    private final Map<String, Node> rootCommands = new ConcurrentHashMap<>();

    void addCommand(Command command) {
        if (command.subcommands().isEmpty()) {
            putAllCheckDuplicates(rootCommands, Node.leaf(command).explode());
            return;
        }
        var parentLifo = Collections.asLifoQueue(new ArrayDeque<Command>());
        var childrenLifo = Collections.asLifoQueue(new ArrayDeque<Command>());
        var nodeAssembly = new HashMap<Integer, List<Node>>(); // key = depth, value = children of current node
        parentLifo.add(command);
        childrenLifo.add(command);
        childrenLifo.addAll(command.subcommands());
        while (!parentLifo.isEmpty()) {
            Command head;
            while ((head = childrenLifo.element()) != parentLifo.element()) {
                parentLifo.add(head);
                childrenLifo.addAll(head.subcommands());
            }
            parentLifo.remove();
            childrenLifo.remove();
            var depth = parentLifo.size();
            var children = nodeAssembly.remove(depth + 1);
            nodeAssembly.computeIfAbsent(depth, k -> new ArrayList<>()).add(Node.of(head, children));
        }
        putAllCheckDuplicates(rootCommands, nodeAssembly.get(0).get(0).explode());
    }

    @Nullable
    Command findForInput(TokenizedInput input) {
        Node found = null;
        Map<String, Node> map = rootCommands;
        var args = input.getMutableArgs();
        while (!args.isEmpty() && map.containsKey(args.element())) {
            found = map.get(args.remove());
            map = found.subcommands;
        }
        return found == null ? null : found.command;
    }

    private static Map<String, Node> putAllCheckDuplicates(Map<String, Node> src, Map<String, Node> dest) {
        dest.forEach((k, v) -> {
            Node old;
            if ((old = src.putIfAbsent(k, v)) != null) {
                throw new IllegalStateException("Alias conflict: the command " + old.command + " and " + v.command +
                        " both define the same alias '" + k + "'.");
            }
        });
        return src;
    }

    private static class Node {

        private final Command command;
        private final Map<String, Node> subcommands;

        private Node(Command command, Map<String, Node> subcommands) {
            this.command = command;
            this.subcommands = subcommands;
        }

        private static Node of(Command command, @Nullable List<Node> children) {
            if (children == null) {
                return leaf(command);
            }
            return new Node(command, children.stream()
                    .map(Node::explode)
                    .reduce(new HashMap<>(), CommandTree::putAllCheckDuplicates));
        }

        private static Node leaf(Command command) {
            return new Node(command, new HashMap<>());
        }

        private Map<String, Node> explode() {
            return new HashMap<>(command.aliases().stream().collect(toMap(Function.identity(), v -> this)));
        }
    }
}
