package SP18_simulator;

import java.util.HashMap;

public class Register {

	HashMap<String, Integer> register;

	public Register() {
		register = new HashMap<String, Integer>();
		initRegister();
	}

	private void initRegister() {
		register.put("A", 0);
		register.put("X", 1);
		register.put("L", 2);
		register.put("B", 3);
		register.put("S", 4);
		register.put("T", 5);
		register.put("F", 6);
		register.put("PC", 8);
		register.put("SW", 9);
	}

	/**
	 * reg�� �������� ���̺� �ִ��� �˻��ϴ� �޼ҵ��̴�. �ش� ���̺� �����ϸ� �ּҰ��� �����ϰ� , ������ -1�� �����Ѵ�.
	 * 
	 * @param reg
	 * @return
	 */
	public int searchRegister(String reg) {

		if (register.containsKey(reg))
			return register.get(reg);
		else
			return -1;
	}
}
