package botrino.command.menu;

import discord4j.core.object.reaction.ReactionEmoji;

import static java.util.Objects.requireNonNull;

public final class PaginationControls {
	
	public static final ReactionEmoji DEFAULT_PREVIOUS_EMOJI = ReactionEmoji.unicode("◀️");
	public static final ReactionEmoji DEFAULT_NEXT_EMOJI = ReactionEmoji.unicode("▶️");
	public static final ReactionEmoji DEFAULT_CLOSE_EMOJI = ReactionEmoji.unicode("🚫");

	private final ReactionEmoji previousEmoji;
	private final ReactionEmoji nextEmoji;
	private final ReactionEmoji closeEmoji;
	
	private PaginationControls(ReactionEmoji previousEmoji, ReactionEmoji nextEmoji, ReactionEmoji closeEmoji) {
		this.previousEmoji = requireNonNull(previousEmoji);
		this.nextEmoji = requireNonNull(nextEmoji);
		this.closeEmoji = requireNonNull(closeEmoji);
	}

	public static PaginationControls of(ReactionEmoji previousEmoji, ReactionEmoji nextEmoji, ReactionEmoji closeEmoji) {
	    return new PaginationControls(previousEmoji, nextEmoji, closeEmoji);
    }
	
	public ReactionEmoji getPreviousEmoji() {
		return previousEmoji;
	}
	
	public ReactionEmoji getNextEmoji() {
		return nextEmoji;
	}
	
	public ReactionEmoji getCloseEmoji() {
		return closeEmoji;
	}

	public static PaginationControls getDefault() {
		return new PaginationControls(DEFAULT_PREVIOUS_EMOJI, DEFAULT_NEXT_EMOJI, DEFAULT_CLOSE_EMOJI);
	}
}
