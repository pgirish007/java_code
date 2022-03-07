public class Example5 {

	public static void main(String[] args) {
		method(null);
	}
	
	
	public static void method(String s) {
		System.out.println("I am in String param");
	}

	public static void method(Object s) {
		System.out.println("I am in Object param");
	}

}
