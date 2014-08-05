package test.damp.ekekox.template.matching;

import java.util.List;


public class EmptyCollection {
	
	private int type = 1;
	private List<Room> rooms;
	private List<Person> students;
	
	public java.util.List<Person> getChildren() {
		  return null;
	}
	
	public List getParents() {
		  return null;
	}

	protected List<Room> getRooms() {
		  if (type == 0)  
		     return null;
		  return rooms;
	}
	
	public List<Person> getStudents() {
		  return students;
	}
	
	public void setAge(int x) {
		
	}
	
	public void firstMethod() {
		this.setAge(3);
	}
	
	public void secondMethod() {
		setAge(new Integer(3));
	}
	
	public void thirdMethod() {
		setAge(33);
	}

	
	

}
