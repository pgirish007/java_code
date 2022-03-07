
@CustomAnnotation(name="Girish", address="New York")
public class Example8 extends AbstractCustomClass{

	public void generate(Object o) {
		// does some magic in here
	}
	
}
********************

@CustomAnnotation(name="Sunil", address="New Jersey")
public class Example7 extends AbstractCustomClass {

	public void generate(Object o) {
		// does some magic in here
	}
	
}

********************

@CustomAnnotation(name="AllTech", address="New Jersey")
public class Example9 extends AbstractCustomClass {

	public void generate(Object o) {
		// does some magic in here
	}
	
}

Question: What would be your approach to identify all those classes that has CustomAnnotation and extends AbstractCustomClass