package SP18_simulator;

import java.io.File;
import java.util.ArrayList;

import javax.sql.rowset.CachedRowSet;

import org.omg.PortableInterceptor.INACTIVE;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ���� ResourceManager�� �����Ͽ�
 * �۾��� �����Ѵ�.
 * 
 * �ۼ����� ���ǻ��� : <br>
 * 1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ����
 * ������ ��.<br>
 * 2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 * 3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 * 4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br>
 * <br>
 * + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� ��
 * �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;

	/* bit ������ �������� ���� ���� */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	int location = 0;
	int locctr = 0;
	int count = 0;

	ArrayList<ObjectCode> codeList = new ArrayList<>();
	Register reg = new Register();

	boolean flag = false;
	boolean dev_flag = false;

	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����. ��, object code�� �޸� ���� �� �ؼ���
	 * SicLoader���� �����ϵ��� �Ѵ�.
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ �� */
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�.
	 */
	public void oneStep() {
		char[] objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 3); // pc�� ����Ű�� �޸� �ּҿ��� 3 byte��
																							// �о� objectcode char�迭��
																							// �����Ѵ�.

		if ((objectCode[0] & 0xF0) == 0xB0 || (objectCode[0] & 0xF0) == 0xA0) { // ��ɾ �������� ���� ��ɾ� �� ��� 2 byte�� �а� pc��
																				// +2�� ���ش�.
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 2);

			location += 2;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 2);
		}

		else if (objectCode[0] == 0x00 && objectCode[1] == 0x10 && objectCode[2] == 0x00) { // ��ɾ �����ּҸ� ����Ű�� ���� ��
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 3);
			location += 3;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 3);
		} else if ((objectCode[1] & 0x10) == 0x10) { // 4���� ��ɾ��� ��� 4 byte�� �а� pc�� +4�� ���ش�.
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 4);
			location += 4;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 4);
		}

		else if (objectCode[0] == 0xF1) { // F1 ����̽�
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 1);
			location += 1;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 1);
		}

		else if (objectCode[0] == 0x05) { // 05 ����̽�
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 1);
			location += 1;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 1);
		}

		else { // �� ���� ��ɾ�� pc�� +3�� ���ش�.
			location += 3;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 3);
		}

		String opcode = String.format("%X%X", (objectCode[0] & 0xF0) / 16, (objectCode[0] & 0x0C)); // opcode�� String����
																									// �����Ͽ� ��ɾ �˻��� ��
																									// �ֵ��� �Ѵ�.
		String inst = "";
		inst = rMgr.instTab.search(opcode); // ��ɾ� �̸��� �����Ѵ�.

		int targetAddress = 0;

		if ((inst = rMgr.instTab.search(opcode)) != "") { // �޸𸮿� �ִ� object�� opcode�� instTable�� ���� ��
			ObjectCode instruction = new ObjectCode(objectCode);
			codeList.add(instruction);

			if (inst.contains("ST")) {

				switch (instruction.getFlag(bFlag | pFlag)) { // nixbpe���� b,p flag Ȯ���Ͽ� target Address ����

				case 0: // Ȯ��� ��ɾ��̰ų� immediate ��ɾ� �� ���
					if (instruction.getFlag(eFlag) == 1) {
						targetAddress = Integer.decode("0X" + instruction.disp);
					} else {
						targetAddress = Integer.decode("0X" + instruction.disp);

					}
					break;

				case bFlag: // bFlag�� 1�� ���
					targetAddress = rMgr.getRegister(reg.searchRegister("B")) + Integer.decode("0X" + instruction.disp);
					break;
				case pFlag: // pFlag�� 1�� ���
					targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
							+ Integer.decode("0X" + instruction.disp);

					break;
				}

				switch (instruction.getFlag(nFlag | iFlag)) { // �ش� targetaddress�� ���� �޸� ����
				case 0:
				case nFlag | iFlag:
					if (instruction.getFlag(eFlag) == 1) { // Ȯ��� ��ɾ��� ���
						if (inst.substring(2).trim().compareTo("CH") == 0) { // STCH ��ɾ��� ��� A���������� ������ 1����Ʈ�� �޸𸮿� ����
							int reg_x = rMgr.getRegister(reg.searchRegister("X"));
							rMgr.setMemory(targetAddress + reg_x, rMgr.getRegister(reg.searchRegister("A")));
						} else { // 2byte�� �о� packing �� ���� �޸𸮿� �����Ѵ�.
							String temp = String.format("%06X",
									rMgr.getRegister(reg.searchRegister(inst.substring(2))));
							int packing = 0;
							for (int i = 0; i < 6; ++i) {
								if ((i + 1) % 2 == 1) {

									if (temp.charAt(i) >= 0x30 && temp.charAt(i) <= 0x39)
										packing = (temp.charAt(i) - 0x30);

									else if (temp.charAt(i) >= 0x41 && temp.charAt(i) <= 0x46)
										packing = (temp.charAt(i) - 0x37);

									packing <<= 4;

								} else {
									if (temp.charAt(i) >= 0x30 && temp.charAt(i) <= 0x39)
										packing += (temp.charAt(i) - 0x30);

									else if (temp.charAt(i) >= 0x41 && temp.charAt(i) <= 0x46)
										packing += (temp.charAt(i) - 0x37);

									rMgr.setMemory(targetAddress + i / 2, packing); // char 2byte�� packing�Ͽ� �޸𸮿� ����
									packing = 0;
								}
							}
						}
					} else {
						String temp = String.format("%06X", rMgr.getRegister(reg.searchRegister(inst.substring(2))));
						int packing = 0;
						for (int i = 0; i < 6; ++i) {
							if ((i + 1) % 2 == 1) {

								if (temp.charAt(i) >= 0x30 && temp.charAt(i) <= 0x39)
									packing = (temp.charAt(i) - 0x30);

								else if (temp.charAt(i) >= 0x41 && temp.charAt(i) <= 0x46)
									packing = (temp.charAt(i) - 0x37);

								packing <<= 4;

							} else {
								if (temp.charAt(i) >= 0x30 && temp.charAt(i) <= 0x39)
									packing += (temp.charAt(i) - 0x30);

								else if (temp.charAt(i) >= 0x41 && temp.charAt(i) <= 0x46)
									packing += (temp.charAt(i) - 0x37);

								rMgr.setMemory(targetAddress + i / 2, packing);
								packing = 0;
							}
						}
					}

					break;

				case nFlag:
					break;
				}
				instruction.setTarAdd(targetAddress);
			}

			else if (inst.contains("LD")) { // LD���� ��ɾ��� ��� �ش� �޸𸮿��� ���� �о� �������Ϳ� �����Ѵ�.

				switch (instruction.getFlag(bFlag | pFlag)) { // nixbpe���� b,p flag Ȯ���Ͽ� target Address ����

				case 0:
					if (instruction.getFlag(eFlag) == 1) {
						targetAddress = Integer.decode("0X" + instruction.disp);
					} else {
						targetAddress = Integer.decode("0X" + instruction.disp);

					}
					break;

				case bFlag:
					targetAddress = rMgr.getRegister(reg.searchRegister("B")) + Integer.decode("0X" + instruction.disp); // b��������
																															// ���̶�
																															// disp��
																															// ���Ѵ�.
					break;
				case pFlag:
					targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
							+ Integer.decode("0X" + instruction.disp); // pc�� disp�� ���Ѵ�.
					break;
				}

				switch (instruction.getFlag(nFlag | iFlag)) {
				case 0:
				case nFlag | iFlag: // n,i flag�� 00�̰ų� 11�� ���
					if (instruction.getFlag(eFlag) == 1) {
						if (inst.substring(2).trim().compareTo("CH") == 0) { // LDCH�� ���
							int reg_x = rMgr.getRegister(reg.searchRegister("X"));
							char[] mem = rMgr.getMemory(targetAddress + reg_x, 1);
							String mem_str = "";

							mem_str = mem_str.concat(String.format("%X", (mem[0] & 0xF0) >> 4));
							mem_str = mem_str.concat(String.format("%X", mem[0] & 0x0F));
							rMgr.setRegister(reg.searchRegister("A"), Integer.decode("0X" + mem_str));
						} else {
							char[] mem = rMgr.getMemory(targetAddress, 3);
							String mem_str = "";
							for (int j = 0; j < 3; ++j) { // �޸𸮿� �ִ� ���� unpacking�Ͽ� mem_str�� �����Ѵ�.
								mem_str = mem_str.concat(String.format("%X", (mem[j] & 0xF0) >> 4));
								mem_str = mem_str.concat(String.format("%X", mem[j] & 0x0F));
							}

							rMgr.setRegister(reg.searchRegister(inst.substring(2, 3)), Integer.decode("0X" + mem_str)); // mem_str��
																														// �ִ�
																														// ����
																														// int��
																														// �ٲ�
																														// �ش�
																														// �������Ϳ�
																														// �����Ѵ�.
						}
					} else {
						char[] mem = rMgr.getMemory(targetAddress, 3);
						String mem_str = "";

						for (int j = 0; j < 3; ++j) { // �޸𸮿� �ִ� ���� unpacking�Ͽ� mem_str�� �����Ѵ�.
							mem_str = mem_str.concat(String.format("%X", (mem[j] & 0xF0) >> 4));
							mem_str = mem_str.concat(String.format("%X", mem[j] & 0x0F));
						}

						rMgr.setRegister(reg.searchRegister(inst.substring(2, 3)), Integer.decode("0X" + mem_str));
					}

					break;

				case nFlag:
					break;

				case iFlag: // immediate addressing ����� ���

					rMgr.setRegister(reg.searchRegister(inst.substring(2, 3)), targetAddress);
					break;
				}
				instruction.setTarAdd(targetAddress);
			}

			else if (inst.charAt(0) == 'J') { // Jump ���� ��ɾ��� ���
				if (inst.compareTo("JSUB") == 0) { // JSUB�� ��� L�������Ϳ� PC���� �����ϰ� pc���� �ش� disp���� �����Ͽ� jump �Ѵ�.
					rMgr.setRegister(reg.searchRegister("L"), rMgr.getRegister(reg.searchRegister("PC")));
					rMgr.setRegister(reg.searchRegister("PC"), Integer.decode("0X" + instruction.disp));
					instruction.setTarAdd(Integer.decode("0X" + instruction.disp));
				} else if (inst.compareTo("JEQ") == 0 || inst.compareTo("JLT") == 0) { // JEQ , JLT ��ɾ��� ���

					if (flag == true) { // COMP, COMPR, TIXR ��ɾ� ���� �� flag ���� -> loop �߻�

						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);
						String ta_str = String.format("%04X", targetAddress);
						if (ta_str.length() > 3) // disp�� �����̸� �պκп��� 1�� ������Ѵ�.
							targetAddress = Integer.decode("0X" + ta_str.replace('2', '1'));

						rMgr.setRegister(reg.searchRegister("PC"), targetAddress);
						instruction.setTarAdd(targetAddress);

						flag = false;
					} else if (rMgr.flag == false) { // TD ��ɾ� ���� �� flag ���� -> loop �߻�

						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);
						String ta_str = String.format("%04X", targetAddress);
						if (ta_str.length() > 3)
							targetAddress = Integer.decode("0X" + ta_str.replace('2', '1'));

						rMgr.setRegister(reg.searchRegister("PC"), targetAddress);
						instruction.setTarAdd(targetAddress);
					}

				}

				else if (inst.compareTo("J") == 0) { // J ��ɾ�
					switch (instruction.getFlag(nFlag | iFlag)) {
					case 0:
					case nFlag | iFlag: // n,i ��Ʈ�� 00�̰ų� 11�� ��� : direct addressing �� ���
						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);

						String ta_str = String.format("%04X", targetAddress);
						if (ta_str.length() > 3)
							targetAddress = Integer.decode("0X" + ta_str.replace('1', '0'));

						rMgr.setRegister(reg.searchRegister("PC"), targetAddress);
						break;
					case nFlag: // indirect addressing �� ���
						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);
						char[] mem = rMgr.getMemory(targetAddress, 3);

						String mem_str = "";

						for (int j = 0; j < 3; ++j) { // �޸𸮸� unpacking �� �������Ϳ� ����
							mem_str = mem_str.concat(String.format("%X", (mem[j] & 0xF0) >> 4));
							mem_str = mem_str.concat(String.format("%X", mem[j] & 0x0F));
						}
						rMgr.setRegister(reg.searchRegister("PC"), Integer.decode("0X" + mem_str));

					}
					instruction.setTarAdd(targetAddress);
				}

			} else if (inst.compareTo("CLEAR") == 0) {
				rMgr.setRegister((objectCode[1] & 0xF0) >> 4, 0);
			} else if (inst.compareTo("COMPR") == 0) {
				int reg1 = (objectCode[1] & 0xF0) >> 4;
				int reg2 = (objectCode[1] & 0x0F);

				if (rMgr.getRegister(reg1) == rMgr.getRegister(reg2))
					flag = true;
				else
					flag = false;
			} else if (inst.compareTo("TIXR") == 0) {
				rMgr.setRegister(reg.searchRegister("X"), rMgr.getRegister(reg.searchRegister("X")) + 1);
				if (rMgr.getRegister(reg.searchRegister("T")) > rMgr.getRegister(reg.searchRegister("X")))
					flag = true;
				else
					flag = false;
			} else if (inst.compareTo("COMP") == 0) {
				if (rMgr.getRegister(reg.searchRegister("A")) == Integer.decode("0X" + instruction.disp))
					flag = true;
				else
					flag = false;
				instruction.setTarAdd(Integer.decode("0X" + instruction.disp));

			} else if (inst.compareTo("RSUB") == 0) { // RSUB�� ��� L �������Ϳ� �ִ� ���� PC�������Ϳ� �����Ѵ�.
				rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("L")));
			} else if (inst.compareTo("TD") == 0) {
				dev_flag = true;
				rMgr.testDevice("F1");
				instruction.setTarAdd(
						rMgr.getRegister(reg.searchRegister("PC")) + Integer.decode("0X" + instruction.disp));
			} else if (inst.compareTo("RD") == 0) {
				dev_flag = true;
				rMgr.setRegister(reg.searchRegister("A"), rMgr.readDevice("F1")); // F1 ����̽��� ���� ���ڸ� A �������Ϳ� �����Ѵ�.
				instruction.setTarAdd(
						rMgr.getRegister(reg.searchRegister("PC")) + Integer.decode("0X" + instruction.disp));

			} else if (inst.compareTo("WD") == 0) {
				dev_flag = true;
				rMgr.writeDevice("05", (char) rMgr.getRegister(reg.searchRegister("A"))); // A���������� ������ 1byte�� 05����̽���
																							// ����Ѵ�.
				instruction.setTarAdd(
						rMgr.getRegister(reg.searchRegister("PC")) + Integer.decode("0X" + instruction.disp));
			}

		}

	}

	/**
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 */
	public void allStep() {

	}

	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
	}
}
