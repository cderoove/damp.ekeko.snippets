package test.damp.ekeko.snippets.cases.basicmatching;

public class BasicMatching {
	
	public BasicMatching() {
		x = new X();
		o = new O();	
	}

	private class O {
		public Object f;
	}
	
	private class X {
		public Integer m() {
			return new Integer(111);
		}
	}
	
	private O o;
	private X x;
	
	public void methodA() {
		this.methodM();
		this.methodC();
	}
	
	public Object methodM() {
		return o.f;
	}
	
	public void methodC() {
		o.f = x.m();
	}
	
	public static void runTest() {
		BasicMatching test = new BasicMatching();
		test.methodA();
		System.out.println(test.o.f);
	}
	
	
}
