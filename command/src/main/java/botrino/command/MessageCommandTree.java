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
import static java.util.stream.Collectors.toUnmodifiableSet;

class MessageCommandTree {

    private final Map<String, Node> rootCommands = new ConcurrentHashMap<>();

    private static Map<String, Node> putAllCheckDuplicates(Map<String, Node> in, Map<String, Node> out) {
        out.forEach((k, v) -> {
            Node old;
            if ((old = in.putIfAbsent(k, v)) != null) {
                throw new IllegalStateException("Alias conflict: the command " + old.command + " and " + v.command +
                        " both define the same alias '" + k + "'.");
            }
        });
        return in;
    }

    void addCommand(MessageCommand command) {
        if (command.subcommands().isEmpty()) {
            putAllCheckDuplicates(rootCommands, Node.leaf(command).explode());
            return;
        }
        var parentLifo = Collections.asLifoQueue(new ArrayDeque<MessageCommand>());
        var childrenLifo = Collections.asLifoQueue(new ArrayDeque<MessageCommand>());
        var nodeAssembly = new HashMap<Integer, List<Node>>(); // key = depth, value = children of current node
        parentLifo.add(command);
        childrenLifo.add(command);
        childrenLifo.addAll(command.subcommands());
        while (!parentLifo.isEmpty()) {
            MessageCommand head;
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

    Optional<MessageCommand> getCommandAt(String topLevelAlias, String... subcommandAliases) {
        var args = new ArrayDeque<String>();
        args.add(topLevelAlias);
        args.addAll(Arrays.asList(subcommandAliases));
        return Optional.ofNullable(getCommandAt(args)).filter(__ -> args.isEmpty());
    }

    Set<MessageCommand> listCommands(String... path) {
        var map = rootCommands;
        for (var p : path) {
            Node n = map.get(p);
            if (n == null) {
                throw new InvalidSyntaxException(null, p, null);
            }
            map = n.subcommands;
        }
        return map.values().stream()
                .distinct()
                .map(n -> n.command)
                .collect(toUnmodifiableSet());
    }

    @Nullable
    MessageCommand getCommandAt(ArrayDeque<String> args) {
        Node found = null;
        var map = rootCommands;
        while (!args.isEmpty() && map.containsKey(args.element())) {
            found = map.get(args.remove());
            map = found.subcommands;
        }
        return found == null ? null : found.command;
    }

    private static class Node {

        private final MessageCommand command;
        private final Map<String, Node> subcommands;

        private Node(MessageCommand command, Map<String, Node> subcommands) {
            this.command = command;
            this.subcommands = subcommands;
        }

        private static Node of(MessageCommand command, @Nullable List<Node> children) {
            if (children == null) {
                return leaf(command);
            }
            return new Node(command, children.stream()
                    .map(Node::explode)
                    .reduce(new HashMap<>(), MessageCommandTree::putAllCheckDuplicates));
        }

        private static Node leaf(MessageCommand command) {
            return new Node(command, new HashMap<>());
        }

        private Map<String, Node> explode() {
            return new HashMap<>(command.aliases().stream().collect(toMap(Function.identity(), v -> this)));
        }
    }
}
