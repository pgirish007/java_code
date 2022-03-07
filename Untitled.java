public class Example1 {
	public static boolean isOdd(int i){
		return i % 2 == 1;
	}
	
	public static void main(String[] args){
		System.out.println(isOdd(1));
		System.out.println(isOdd(0));
		System.out.println(isOdd(-1));
	}
}
