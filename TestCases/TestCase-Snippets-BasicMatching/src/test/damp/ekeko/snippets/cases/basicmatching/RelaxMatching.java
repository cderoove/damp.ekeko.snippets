package test.damp.ekeko.snippets.cases.basicmatching;


public class RelaxMatching {

	public RelaxMatching() {
	}

	public int rmethodA() {
		int x = 0;
		int y = 0;
		int z = x + y;
		return z;
	}
	
	public int rmethodB() {
		int x = 0, y = 0;
		int z = x + y;
		return z;
	}

	public int rmethodC() {
		int i = 0;
		int x = 0, y = 0;
		int z = x + y;
		return z;
	}

	public int rmethodD() {
		int i = 0;
		i = 1;
		int x = 0, y = 0;
		int z = x + y;
		return z;
	}

	public int rmethodE(int val) {
		int r = 0;
		if (val == 0) {
			r = val;
		} else if (val < 0) {
			r = val * -1;
		} else {
			r = val;
		}
		return r;
	}
	
	public int rmethodF(int val) {
		int r = 0;
		if (val == 0) {
			r = val;
		} 
		return r;
	}

	public int rmethodG(int val) {
		int r = 0;
		if (val == 0) {
			r = val;
		} else if (val < 0) {
			r = val * -1;
		} 
		return r;
	}
	
	public int rmethodH(int val) {
		int r = 1;
		if (val == 0) {
			r = val;
		} 
		return r;
	}
}
