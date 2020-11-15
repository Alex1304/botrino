package botrino.command.menu;

public class PageNumberOutOfRangeException extends RuntimeException {

	private final int minPage;
	private final int maxPage;
	private final int actualValue;
	
	public PageNumberOutOfRangeException(int actualValue, int minPage, int maxPage) {
		super("must be between " + minPage + " and " + maxPage + ", but was " + actualValue);
		if (minPage > maxPage) {
			throw new IllegalArgumentException("minPage > maxPage");
		}
		this.minPage = minPage;
		this.maxPage = maxPage;
		this.actualValue = actualValue;
	}
	
	public int getMinPage() {
		return minPage;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public int getActualValue() {
		return actualValue;
	}
	
	public static void check(int value, int min, int max) {
		if (value < min || value > max) {
			throw new PageNumberOutOfRangeException(value, min, max);
		}
	}
}
