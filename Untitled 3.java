public class Example3 {
	public static void main(String[] args) {
		final String x = "length: 10";
		final String z = "length: 10";
		final String y = "length: " + pig.length();
		System.out.println("equal: " + x  == y);
		System.out.println("equal: " + x  == z);
		System.out.println("equal: " + x.equals(y));
	}
}