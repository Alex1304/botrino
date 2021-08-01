package botrino.command.menu;

import botrino.command.CommandContext;
import botrino.command.TokenizedInput;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility to set up interactive menus in Discord. An interactive menu first sends a message as a prompt and waits for a
 * interaction from the user. The said interaction can be either a message or a reaction. This is a non-thread-safe
 * mutable object which is activated by the {@link #open(CommandContext)} method. As open does not modify state, it may
 * be invoked several times on the same {@link InteractiveMenu} instance.
 *
 * @see InteractiveMenuFactory
 */
public final class InteractiveMenu {

    private final MenuMessageFactory menuMessageFactory;
    private final Map<String, Function<MessageMenuInteraction, Mono<Void>>> messageItems = new LinkedHashMap<>();
    private final Map<ReactionEmoji, Function<ReactionMenuInteraction, Mono<Void>>> reactionItems =
            new LinkedHashMap<>();
    private final ConcurrentHashMap<String, Object> interactionContext = new ConcurrentHashMap<>();
    private boolean deleteMenuOnClose;
    private boolean deleteMenuOnTimeout;
    private boolean closeAfterMessage = true;
    private boolean closeAfterReaction = true;
    private Duration timeout;

    InteractiveMenu(MenuMessageFactory menuMessageFactory, Duration defaultTimeout) {
        this.menuMessageFactory = menuMessageFactory;
        this.timeout = defaultTimeout;
    }

    private static Mono<Void> handleTermination(Message menuMessage, boolean shouldDelete) {
        return (shouldDelete
                ? menuMessage.delete()
                : menuMessage.removeAllReactions())
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Initializes the interaction context that will be passed to all interaction instances happening while this menu is
     * open.
     *
     * @param contextConsumer the consumer that populates the context map
     * @return this menu
     */
    public InteractiveMenu writeInteractionContext(Consumer<Map<String, Object>> contextConsumer) {
        Objects.requireNonNull(contextConsumer);
        contextConsumer.accept(interactionContext);
        return this;
    }

    /**
     * Adds an item to this menu that is triggered when replying with a specific message. An empty string will make the
     * action execute when the message does not match any of the other message items.
     *
     * @param message the text the message must start with in order to trigger this item, or empty string to trigger the
     *                action when no other message items are matched
     * @param action  the action associated to this item
     * @return this menu
     */
    public InteractiveMenu addMessageItem(String message, Function<MessageMenuInteraction, Mono<Void>> action) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(action);
        messageItems.put(message, action);
        return this;
    }

    /**
     * Adds an item to this menu that is triggered when adding or removing a reaction to the menu message.
     *
     * @param emoji  the reaction emoji identifying this item
     * @param action the action associated to this item
     * @return this menu
     */
    public InteractiveMenu addReactionItem(ReactionEmoji emoji, Function<ReactionMenuInteraction, Mono<Void>> action) {
        Objects.requireNonNull(emoji);
        Objects.requireNonNull(action);
        reactionItems.put(emoji, action);
        return this;
    }

    /**
     * Sets whether to delete the menu message when the menu is closed by user.
     *
     * @param deleteMenuOnClose a boolean
     * @return this menu
     */
    public InteractiveMenu deleteMenuOnClose(boolean deleteMenuOnClose) {
        this.deleteMenuOnClose = deleteMenuOnClose;
        return this;
    }

    /**
     * Sets whether to delete the menu message when the menu is closed automatically by timeout
     *
     * @param deleteMenuOnTimeout a boolean
     * @return this menu
     */
    public InteractiveMenu deleteMenuOnTimeout(boolean deleteMenuOnTimeout) {
        this.deleteMenuOnTimeout = deleteMenuOnTimeout;
        return this;
    }

    /**
     * Sets whether to close this menu after a message item is triggered.
     *
     * @param closeAfterMessage a boolean
     * @return this menu
     */
    public InteractiveMenu closeAfterMessage(boolean closeAfterMessage) {
        this.closeAfterMessage = closeAfterMessage;
        return this;
    }

    /**
     * Sets whether to close this menu after a reaction item is triggered.
     *
     * @param closeAfterReaction a boolean
     * @return this menu
     */
    public InteractiveMenu closeAfterReaction(boolean closeAfterReaction) {
        this.closeAfterReaction = closeAfterReaction;
        return this;
    }

    /**
     * Sets a timeout after which the menu automatically closes when the user does not interact.
     *
     * @param timeout the timeout value
     * @return this menu
     */
    public InteractiveMenu setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Opens the interactive menu, that is, sends the menu message over Discord and starts listening for user's
     * interaction. The returned Mono completes once the menu closes or times out.
     *
     * @param ctx the context of the command invoking this menu
     * @return a Mono completing when the menu closes or timeouts. Any error happening while the menu is open will be
     * forwarded through the Mono
     */
    public Mono<MenuTermination> open(CommandContext ctx) {
        Objects.requireNonNull(ctx);
        if (messageItems.isEmpty() && reactionItems.isEmpty()) {
            return menuMessageFactory.create(ctx)
                    .flatMap(ctx.channel()::createMessage)
                    .thenReturn(MenuTermination.COMPLETE);
        }
        var gateway = ctx.event().getClient();
        var closeNotifier = Sinks.<MenuTermination>one();
        // Signals onNext each time user interacts with the menu
        var onInteraction = Sinks.many().replay().latestOrDefault(0);
        return menuMessageFactory.create(ctx)
                .flatMap(ctx.channel()::createMessage)
                .flatMap(this::addReactionsToMenu)
                .flatMap(menuMessage -> {
                    var messageInteractionHandler = gateway.on(MessageCreateEvent.class)
                            .takeUntilOther(closeNotifier.asMono())
                            .filter(event -> event.getMessage().getAuthor().equals(ctx.event().getMessage().getAuthor())
                                    && event.getMessage().getChannelId().equals(ctx.event().getMessage().getChannelId()))
                            .flatMap(event -> {
                                var tokens = TokenizedInput.tokenize(event.getMessage().getContent());
                                var args = tokens.getArguments();
                                if (args.isEmpty()) {
                                    return Mono.empty();
                                }
                                var action = messageItems.get(args.get(0));
                                if (action == null) {
                                    action = messageItems.get("");
                                    if (action == null) {
                                        return Mono.empty();
                                    }
                                }
                                var interaction = new MessageMenuInteraction(ctx, menuMessage, interactionContext,
                                        closeNotifier, event, tokens);
                                return action.apply(interaction)
                                        .doOnSubscribe(__ -> onInteraction.tryEmitNext(0))
                                        .thenReturn(0);
                            })
                            .takeUntil(__ -> closeAfterMessage)
                            .retryWhen(Retry.indefinitely().filter(UnexpectedReplyException.class::isInstance))
                            .doFinally(__ -> closeNotifier.tryEmitValue(MenuTermination.COMPLETE));
                    var reactionInteractionHandler = Flux.merge(
                            gateway.on(ReactionAddEvent.class),
                            gateway.on(ReactionRemoveEvent.class))
                            .takeUntilOther(closeNotifier.asMono())
                            .map(event -> event instanceof ReactionAddEvent
                                    ? ReactionToggleEvent.add((ReactionAddEvent) event)
                                    : ReactionToggleEvent.remove((ReactionRemoveEvent) event))
                            .filter(event -> event.getMessageId().equals(menuMessage.getId())
                                    && event.getUserId().equals(ctx.event().getMessage().getAuthor().map(User::getId).orElse(null)))
                            .flatMap(event -> {
                                var action = reactionItems.get(event.getEmoji());
                                if (action == null) {
                                    return Mono.empty();
                                }
                                var interaction = new ReactionMenuInteraction(ctx, menuMessage, interactionContext,
                                        closeNotifier, event);
                                return action.apply(interaction)
                                        .doOnSubscribe(__ -> onInteraction.tryEmitNext(0))
                                        .thenReturn(0);
                            })
                            .takeUntil(__ -> closeAfterReaction)
                            .doFinally(__ -> closeNotifier.tryEmitValue(MenuTermination.COMPLETE));
                    var menuMono = Mono.when(messageInteractionHandler, reactionInteractionHandler);
                    var timeoutNotifier = timeout.isZero()
                            ? Mono.never()
                            : onInteraction.asFlux().timeout(timeout, Flux.empty())
                            .then(Mono.fromRunnable(() -> closeNotifier.tryEmitValue(MenuTermination.TIMEOUT)));
                    var terminationHandler = closeNotifier.asMono().flatMap(termination -> {
                        switch (termination) {
                            case TIMEOUT:
                                return handleTermination(menuMessage, deleteMenuOnTimeout).thenReturn(termination);
                            case COMPLETE:
                                return handleTermination(menuMessage, deleteMenuOnClose).thenReturn(termination);
                            default:
                                return Mono.error(new AssertionError());
                        }
                    });
                    return Mono.when(menuMono, timeoutNotifier.takeUntilOther(closeNotifier.asMono()))
                            .then(terminationHandler)
                            .onErrorResume(e -> terminationHandler
                                    .onErrorResume(e2 -> Mono.fromRunnable(() -> e.addSuppressed(e2)))
                                    .then(Mono.error(e)));
                });
    }

    private Mono<Message> addReactionsToMenu(Message menuMessage) {
        return Flux.fromIterable(reactionItems.keySet())
                .concatMap(reaction -> menuMessage.addReaction(reaction)
                        .onErrorResume(ClientException.isStatusCode(403).negate(), e -> Mono.empty()))
                .then()
                .thenReturn(menuMessage);
    }

    public enum MenuTermination {
        TIMEOUT, COMPLETE
    }

}
