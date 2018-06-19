package SP18_simulator;

public class ObjectCode {

	char[] objectCode;
	String opcode;
	char nixbpe = 0;
	String disp = "";
	String inst_name;
	InstTable instTab = new InstTable("C:\\Users\\����\\Downloads\\SP18_simulator\\src\\SP18_simulator\\inst.data");
	int targetAddress = 0;

	public ObjectCode(char[] objectCode) {
		this.objectCode = objectCode;
		setOpcode();
		if (objectCode.length > 1) { // objectcode�� ����̽� ������ ��� nixbpe�� �������� �ʴ´�.
			setNixbpe();
			setDisp();
		}
		inst_name = instTab.search(opcode);
	}

	private void setOpcode() { // ��ɾ��� opcode�� �����Ѵ�.
		opcode = String.format("%X%X", (objectCode[0] & 0xF0) / 16, objectCode[0] & 0x0C);
	}

	private void setNixbpe() { // objectcode�� nixbpe�� �����Ѵ�.

		nixbpe = (char) (objectCode[0] & 0x03);
		nixbpe <<= 4;
		char temp = (char) (objectCode[1] & 0xF0);
		temp >>= 4;
		nixbpe += temp;

	}

	private void setDisp() { // objectcode�� disp�� �����Ѵ�.
		disp = String.format("%X", objectCode[1] & 0x0F);
		for (int i = 2; i < objectCode.length; ++i) {
			disp = disp.concat(String.format("%X", ((objectCode[i] >> 4))));
			disp = disp.concat(String.format("%X", ((objectCode[i] & 0x0F))));
		}
	}

	public int getFlag(int flags) {
		return nixbpe & flags;
	}

	public void setTarAdd(int targetAddress) { // targetaddress�� �����Ѵ�.
		this.targetAddress = targetAddress;
	}
}
