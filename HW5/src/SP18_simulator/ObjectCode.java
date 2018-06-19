package SP18_simulator;

public class ObjectCode {

	char[] objectCode;
	String opcode;
	char nixbpe = 0;
	String disp = "";
	String inst_name;
	InstTable instTab = new InstTable("C:\\Users\\한주\\Downloads\\SP18_simulator\\src\\SP18_simulator\\inst.data");
	int targetAddress = 0;

	public ObjectCode(char[] objectCode) {
		this.objectCode = objectCode;
		setOpcode();
		if (objectCode.length > 1) { // objectcode에 디바이스 정보일 경우 nixbpe를 설정하지 않는다.
			setNixbpe();
			setDisp();
		}
		inst_name = instTab.search(opcode);
	}

	private void setOpcode() { // 명령어의 opcode를 저장한다.
		opcode = String.format("%X%X", (objectCode[0] & 0xF0) / 16, objectCode[0] & 0x0C);
	}

	private void setNixbpe() { // objectcode의 nixbpe를 저장한다.

		nixbpe = (char) (objectCode[0] & 0x03);
		nixbpe <<= 4;
		char temp = (char) (objectCode[1] & 0xF0);
		temp >>= 4;
		nixbpe += temp;

	}

	private void setDisp() { // objectcode의 disp를 저장한다.
		disp = String.format("%X", objectCode[1] & 0x0F);
		for (int i = 2; i < objectCode.length; ++i) {
			disp = disp.concat(String.format("%X", ((objectCode[i] >> 4))));
			disp = disp.concat(String.format("%X", ((objectCode[i] & 0x0F))));
		}
	}

	public int getFlag(int flags) {
		return nixbpe & flags;
	}

	public void setTarAdd(int targetAddress) { // targetaddress를 셋팅한다.
		this.targetAddress = targetAddress;
	}
}
