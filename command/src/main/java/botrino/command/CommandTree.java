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
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

class CommandTree {

    private final Map<String, Node> rootCommands = new HashMap<>();

    void addCommand(Command command) {
        if (command.getSubcommands().isEmpty()) {
            rootCommands.putAll(Node.leaf(command).explode());
            return;
        }
        var parentLifo = Collections.asLifoQueue(new ArrayDeque<Command>());
        var childrenLifo = Collections.asLifoQueue(new ArrayDeque<Command>());
        var nodeAssembly = new HashMap<Integer, List<Node>>(); // key = depth, value = children of current node
        parentLifo.add(command);
        childrenLifo.add(command);
        childrenLifo.addAll(command.getSubcommands());
        while (!parentLifo.isEmpty()) {
            Command head;
            while ((head = childrenLifo.element()) != parentLifo.element()) {
                parentLifo.add(head);
                childrenLifo.addAll(head.getSubcommands());
            }
            parentLifo.remove();
            childrenLifo.remove();
            var depth = parentLifo.size();
            var children = nodeAssembly.remove(depth + 1);
            nodeAssembly.computeIfAbsent(depth, k -> new ArrayList<>()).add(Node.of(head, children));
        }
        rootCommands.putAll(nodeAssembly.get(0).get(0).explode());
    }

    @Nullable
    Command findForInput(CommandInput input) {
        Node found = null;
        Map<String, Node> map = rootCommands;
        var args = input.getMutableArgs();
        while (!args.isEmpty() && (found = map.get(args.element())) != null) {
            map = found.subcommands;
            args.remove();
        }
        return found == null ? null : found.command;
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
                    .reduce(new HashMap<>(), (a, b) -> {
                        a.putAll(b);
                        return a;
                    }));
        }

        private static Node leaf(Command command) {
            return new Node(command, Map.of());
        }

        private Map<String, Node> explode() {
            return command.getAliases().stream().collect(toMap(Function.identity(), v -> this));
        }
    }
}
