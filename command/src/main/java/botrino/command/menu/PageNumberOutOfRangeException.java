package botrino.command.menu;

public class PageNumberOutOfRangeException extends RuntimeException {

	private final int maxPage;
	private final int actualValue;
	
	public PageNumberOutOfRangeException(int actualValue, int maxPage) {
		super("must be between 0 and " + maxPage + ", but was " + actualValue);
		this.maxPage = maxPage;
		this.actualValue = actualValue;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public int getActualValue() {
		return actualValue;
	}
	
	public static void check(int value, int max) {
		if (value < 0 || value > max) {
			throw new PageNumberOutOfRangeException(value, max);
		}
	}
}
