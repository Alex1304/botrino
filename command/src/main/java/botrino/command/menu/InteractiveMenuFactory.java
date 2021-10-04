package botrino.command.menu;

import botrino.api.util.MessageUtils;
import botrino.command.CommandContext;
import botrino.command.CommandService;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * Factory that is capable of creating {@link InteractiveMenu} instances.
 */
public final class InteractiveMenuFactory {

    private final PaginationControls controls;
    private final Duration defaultTimeout;

    private InteractiveMenuFactory(PaginationControls controls, Duration defaultTimeout) {
        this.controls = controls;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Creates a new {@link InteractiveMenuFactory} that will used the specified pagination controls and default timeout
     * to create interactive menus. It is generally recommended to use {@link CommandService#interactiveMenuFactory()}
     * to obtain an instance.
     *
     * @param controls       the controls to use when creating paginated menus
     * @param defaultTimeout the default timeout to apply when a user does not interact with the menu
     * @return a new {@link InteractiveMenuFactory}
     */
    public static InteractiveMenuFactory of(PaginationControls controls, Duration defaultTimeout) {
        return new InteractiveMenuFactory(controls, defaultTimeout);
    }

    /**
     * Creates a new empty {@link InteractiveMenu} with a given message that will serve as menu prompt.
     *
     * @param messageCreateSpec the spec to build the menu message
     * @return a new {@link InteractiveMenu}
     */
    public InteractiveMenu create(MessageCreateSpec messageCreateSpec) {
        Objects.requireNonNull(messageCreateSpec);
        return createAsync((CommandContext ctx) -> Mono.just(messageCreateSpec));
    }

    /**
     * Creates a new empty {@link InteractiveMenu} with a given message that will serve as menu prompt.
     *
     * @param message the menu message
     * @return a new {@link InteractiveMenu}
     */
    public InteractiveMenu create(String message) {
        Objects.requireNonNull(message);
        return create(MessageCreateSpec.create().withContent(message));
    }

    /**
     * Creates a new empty {@link InteractiveMenu} with a given message that will serve as menu prompt. The menu message
     * may be supplied from an asynchronous source.
     *
     * @param menuMessageFactory a function accepting the command context and asynchronously generating the message of
     *                           the menu
     * @return a new {@link InteractiveMenu}
     */
    public InteractiveMenu createAsync(MenuMessageFactory menuMessageFactory) {
        Objects.requireNonNull(menuMessageFactory);
        return new InteractiveMenu(menuMessageFactory, defaultTimeout);
    }

    /**
     * Creates a new {@link InteractiveMenu} prefilled with menu items useful for pagination.
     *
     * @param messagePaginator a function that asynchronously generates the message to display according to the current
     *                         page number and the command context. If the page number is out of range, the Mono
     *                         returned by this function may emit a {@link PageNumberOutOfRangeException} which is
     *                         handled by default to cover cases where the user inputs an invalid page number. Note that
     *                         if {@link PageNumberOutOfRangeException} is emitted with min/max values that aren't the
     *                         same depending on the current page number, the behavior of the {@link InteractiveMenu}
     *                         will be undefined.
     * @return a new {@link InteractiveMenu} prefilled with menu items useful for pagination.
     */
    public InteractiveMenu createPaginated(MessagePaginator messagePaginator) {
        Objects.requireNonNull(messagePaginator);
        return createAsync(ctx -> messagePaginator.renderPage(ctx, 0))
                .writeInteractionContext(context -> context.put("currentPage", 0))
                .addReactionItem(controls.getPreviousEmoji(), interaction -> Mono.fromCallable(
                        () -> interaction.update("currentPage", x -> x - 1, -1))
                        .flatMap(targetPage -> messagePaginator
                                .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                .map(MessageUtils::toMessageEditSpec))
                        .onErrorResume(PageNumberOutOfRangeException.class, e -> Mono.fromCallable(
                                () -> interaction.update("currentPage",
                                        x -> x + e.getMaxPage() + 1, 0))
                                .flatMap(targetPage -> messagePaginator
                                        .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                        .map(MessageUtils::toMessageEditSpec)))
                        .flatMap(interaction.getMenuMessage()::edit)
                        .then())
                .addReactionItem(controls.getNextEmoji(), interaction -> Mono.fromCallable(
                        () -> interaction.update("currentPage", x -> x + 1, 1))
                        .flatMap(targetPage -> messagePaginator
                                .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                .map(MessageUtils::toMessageEditSpec))
                        .onErrorResume(PageNumberOutOfRangeException.class, e -> Mono.fromCallable(
                                () -> interaction.update("currentPage",
                                        x -> x - e.getMaxPage() - 1, 0))
                                .flatMap(targetPage -> messagePaginator
                                        .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                        .map(MessageUtils::toMessageEditSpec)))
                        .flatMap(interaction.getMenuMessage()::edit)
                        .then())
                .addMessageItem("page", interaction -> Mono
                        .fromCallable(() -> Integer.parseInt(interaction.getInput().getArguments().get(1)))
                        .onErrorMap(IndexOutOfBoundsException.class, e -> new UnexpectedReplyException())
                        .onErrorMap(NumberFormatException.class, e -> new UnexpectedReplyException())
                        .map(p -> p - 1)
                        .doOnNext(targetPage -> {
                            interaction.set("oldPage", interaction.get("currentPage"));
                            interaction.set("currentPage", targetPage);
                        })
                        .flatMap(targetPage -> messagePaginator
                                .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                .map(MessageUtils::toMessageEditSpec)
                                .flatMap(interaction.getMenuMessage()::edit))
                        .onErrorMap(PageNumberOutOfRangeException.class, e -> {
                            interaction.set("currentPage", interaction.get("oldPage"));
                            return new UnexpectedReplyException();
                        })
                        .then(interaction.getEvent().getMessage().delete().onErrorResume(e -> Mono.empty())))
                .addReactionItem(controls.getCloseEmoji(), interaction -> Mono.fromRunnable(interaction::closeMenu))
                .closeAfterMessage(false)
                .closeAfterReaction(false);
    }

    /**
     * Gets the pagination controls used by the interactive menus created by this factory.
     *
     * @return the pagination controls
     */
    public PaginationControls getPaginationControls() {
        return controls;
    }

    /**
     * Gets the default timeout value applied to interactive menus created by this factory.
     *
     * @return the default timeout
     */
    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }
}
