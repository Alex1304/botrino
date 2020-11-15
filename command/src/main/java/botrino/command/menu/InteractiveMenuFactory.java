package botrino.command.menu;

import botrino.api.util.MessageTemplate;
import botrino.command.CommandContext;
import botrino.command.CommandService;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

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
     * Creates a new empty InteractiveMenu with a given message that will serve as menu prompt.
     *
     * @param messageCreateSpec the spec to build the menu message
     * @return a new InteractiveMenu
     */
    public InteractiveMenu create(Consumer<MessageCreateSpec> messageCreateSpec) {
        Objects.requireNonNull(messageCreateSpec);
        return create((CommandContext ctx) -> Mono.just(messageCreateSpec));
    }

    /**
     * Creates a new empty InteractiveMenu with a given message that will serve as menu prompt.
     *
     * @param message the menu message
     * @return a new InteractiveMenu
     */
    public InteractiveMenu create(String message) {
        Objects.requireNonNull(message);
        return create((MessageCreateSpec mcs) -> mcs.setContent(message));
    }

    /**
     * Creates a new empty InteractiveMenu with a given message that will serve as menu prompt. The menu message may be
     * supplied from an asynchronous source.
     *
     * @param menuMessageFactory a function accepting the command context and asynchronously generating the message of
     *                           the menu
     * @return a new InteractiveMenu
     */
    public InteractiveMenu create(MenuMessageFactory menuMessageFactory) {
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
     *                         same depending on the current page number, the behavior of the InteractiveMenu will be
     *                         undefined.
     * @return a new InteractiveMenu prefilled with menu items useful for pagination.
     */
    public InteractiveMenu createAsyncPaginated(MessagePaginator messagePaginator) {
        Objects.requireNonNull(messagePaginator);
        return create((CommandContext ctx) -> messagePaginator.renderPage(ctx, 0).map(MessageTemplate::toCreateSpec))
                .writeInteractionContext(context -> context.put("currentPage", 0))
                .addReactionItem(controls.getPreviousEmoji(), interaction -> Mono.fromCallable(
                        () -> interaction.update("currentPage", x -> x - 1, -1))
                        .flatMap(targetPage -> messagePaginator
                                .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                .map(MessageTemplate::toEditSpec))
                        .onErrorResume(PageNumberOutOfRangeException.class, e -> Mono.fromCallable(
                                () -> interaction.update("currentPage",
                                        x -> x + e.getMaxPage() - e.getMinPage() + 1, 0))
                                .flatMap(targetPage -> messagePaginator
                                        .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                        .map(MessageTemplate::toEditSpec)))
                        .flatMap(interaction.getMenuMessage()::edit)
                        .then())
                .addReactionItem(controls.getNextEmoji(), interaction -> Mono.fromCallable(
                        () -> interaction.update("currentPage", x -> x + 1, 1))
                        .flatMap(targetPage -> messagePaginator
                                .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                .map(MessageTemplate::toEditSpec))
                        .onErrorResume(PageNumberOutOfRangeException.class, e -> Mono.fromCallable(
                                () -> interaction.update("currentPage",
                                        x -> x - e.getMaxPage() + e.getMinPage() - 1, 0))
                                .flatMap(targetPage -> messagePaginator
                                        .renderPage(interaction.getOriginalCommandContext(), targetPage)
                                        .map(MessageTemplate::toEditSpec)))
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
                                .map(MessageTemplate::toEditSpec)
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
}
