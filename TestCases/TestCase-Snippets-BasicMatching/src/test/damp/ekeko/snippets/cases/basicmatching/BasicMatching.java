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
	
	public void methodA1() {
		this.methodM();
		this.methodC();
	}

	public void methodA2() {
		this.methodC();
		this.methodM();
	}

	public void methodA3() {
		this.methodM();
		this.methodC();
		this.methodD();
		this.methodC();
		this.methodE();
	}

	public void methodA4() {
		this.methodM();
		this.methodE();
		this.methodD();
	}

	public void methodA5() {
		this.methodM();
		this.methodE();
		this.methodD();
		this.methodD();
	}

	public void methodAA() {
		this.methodM();
		this.methodD();
		this.methodE();
	}

	public void methodAAA() {
		this.methodM();
		this.methodD();
		this.methodD();
	}

	public Object methodM() {
		return o.f;
	}
	
	public void methodC() {
		o.f = x.m();
	}
	
	public int methodD() {
		int foo = 0;
		return foo;
	}

	public int methodE() {
		int foo = 0;
		return foo;
	}

	public static void runTest() {
		BasicMatching test = new BasicMatching();
		test.methodA();
		test.methodA1();
		test.methodA2();
		System.out.println(test.o.f);
	}
	
	
}
