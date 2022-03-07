public class Example10 {
	private Example10 x = new Example10();
	
	public Example10() throws Exception{
		throw new Exception("I am in constructor");
	}
	
	public static void main(String[] args) throws Exception{
		try {
			Example10 x= new Example10();
			// with x someting we will do but later
			System.out.println("I am in try block");
		}
		catch(Exception e) {
			System.out.println("I am in catch block");
		}
	}

	
}
