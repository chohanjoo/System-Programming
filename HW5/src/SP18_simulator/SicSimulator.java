package SP18_simulator;

import java.io.File;
import java.util.ArrayList;

import javax.sql.rowset.CachedRowSet;

import org.omg.PortableInterceptor.INACTIVE;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라 ResourceManager에 접근하여
 * 작업을 수행한다.
 * 
 * 작성중의 유의사항 : <br>
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 지양할 것.<br>
 * 2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br>
 * <br>
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;

	/* bit 조작의 가독성을 위한 선언 */
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
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행. 단, object code의 메모리 적재 및 해석은
	 * SicLoader에서 수행하도록 한다.
	 */
	public void load(File program) {
		/* 메모리 초기화, 레지스터 초기화 등 */
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다.
	 */
	public void oneStep() {
		char[] objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 3); // pc가 가리키는 메모리 주소에서 3 byte를
																							// 읽어 objectcode char배열에
																							// 저장한다.

		if ((objectCode[0] & 0xF0) == 0xB0 || (objectCode[0] & 0xF0) == 0xA0) { // 명령어가 레지스터 관련 명령어 일 경우 2 byte를 읽고 pc에
																				// +2를 해준다.
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 2);

			location += 2;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 2);
		}

		else if (objectCode[0] == 0x00 && objectCode[1] == 0x10 && objectCode[2] == 0x00) { // 명령어가 절대주소를 가리키고 있을 때
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 3);
			location += 3;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 3);
		} else if ((objectCode[1] & 0x10) == 0x10) { // 4형식 명령어인 경우 4 byte를 읽고 pc에 +4를 해준다.
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 4);
			location += 4;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 4);
		}

		else if (objectCode[0] == 0xF1) { // F1 디바이스
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 1);
			location += 1;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 1);
		}

		else if (objectCode[0] == 0x05) { // 05 디바이스
			objectCode = rMgr.getMemory(rMgr.getRegister(reg.searchRegister("PC")), 1);
			location += 1;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 1);
		}

		else { // 그 외의 명령어는 pc에 +3을 해준다.
			location += 3;
			rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("PC")) + 3);
		}

		String opcode = String.format("%X%X", (objectCode[0] & 0xF0) / 16, (objectCode[0] & 0x0C)); // opcode를 String으로
																									// 저장하여 명령어를 검색할 수
																									// 있도록 한다.
		String inst = "";
		inst = rMgr.instTab.search(opcode); // 명령어 이름을 저장한다.

		int targetAddress = 0;

		if ((inst = rMgr.instTab.search(opcode)) != "") { // 메모리에 있는 object의 opcode가 instTable에 있을 때
			ObjectCode instruction = new ObjectCode(objectCode);
			codeList.add(instruction);

			if (inst.contains("ST")) {

				switch (instruction.getFlag(bFlag | pFlag)) { // nixbpe에서 b,p flag 확인하여 target Address 설정

				case 0: // 확장된 명령어이거나 immediate 명령어 인 경우
					if (instruction.getFlag(eFlag) == 1) {
						targetAddress = Integer.decode("0X" + instruction.disp);
					} else {
						targetAddress = Integer.decode("0X" + instruction.disp);

					}
					break;

				case bFlag: // bFlag가 1인 경우
					targetAddress = rMgr.getRegister(reg.searchRegister("B")) + Integer.decode("0X" + instruction.disp);
					break;
				case pFlag: // pFlag가 1인 경우
					targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
							+ Integer.decode("0X" + instruction.disp);

					break;
				}

				switch (instruction.getFlag(nFlag | iFlag)) { // 해당 targetaddress로 가서 메모리 저장
				case 0:
				case nFlag | iFlag:
					if (instruction.getFlag(eFlag) == 1) { // 확장된 명령어인 경우
						if (inst.substring(2).trim().compareTo("CH") == 0) { // STCH 명령어인 경우 A레지스터의 오른쪽 1바이트를 메모리에 저장
							int reg_x = rMgr.getRegister(reg.searchRegister("X"));
							rMgr.setMemory(targetAddress + reg_x, rMgr.getRegister(reg.searchRegister("A")));
						} else { // 2byte씩 읽어 packing 한 다음 메모리에 저장한다.
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

									rMgr.setMemory(targetAddress + i / 2, packing); // char 2byte를 packing하여 메모리에 저장
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

			else if (inst.contains("LD")) { // LD관련 명령어인 경우 해당 메모리에서 값을 읽어 레지스터에 저장한다.

				switch (instruction.getFlag(bFlag | pFlag)) { // nixbpe에서 b,p flag 확인하여 target Address 설정

				case 0:
					if (instruction.getFlag(eFlag) == 1) {
						targetAddress = Integer.decode("0X" + instruction.disp);
					} else {
						targetAddress = Integer.decode("0X" + instruction.disp);

					}
					break;

				case bFlag:
					targetAddress = rMgr.getRegister(reg.searchRegister("B")) + Integer.decode("0X" + instruction.disp); // b레지스터
																															// 값이랑
																															// disp랑
																															// 더한다.
					break;
				case pFlag:
					targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
							+ Integer.decode("0X" + instruction.disp); // pc랑 disp랑 더한다.
					break;
				}

				switch (instruction.getFlag(nFlag | iFlag)) {
				case 0:
				case nFlag | iFlag: // n,i flag가 00이거나 11인 경우
					if (instruction.getFlag(eFlag) == 1) {
						if (inst.substring(2).trim().compareTo("CH") == 0) { // LDCH인 경우
							int reg_x = rMgr.getRegister(reg.searchRegister("X"));
							char[] mem = rMgr.getMemory(targetAddress + reg_x, 1);
							String mem_str = "";

							mem_str = mem_str.concat(String.format("%X", (mem[0] & 0xF0) >> 4));
							mem_str = mem_str.concat(String.format("%X", mem[0] & 0x0F));
							rMgr.setRegister(reg.searchRegister("A"), Integer.decode("0X" + mem_str));
						} else {
							char[] mem = rMgr.getMemory(targetAddress, 3);
							String mem_str = "";
							for (int j = 0; j < 3; ++j) { // 메모리에 있는 값을 unpacking하여 mem_str에 저장한다.
								mem_str = mem_str.concat(String.format("%X", (mem[j] & 0xF0) >> 4));
								mem_str = mem_str.concat(String.format("%X", mem[j] & 0x0F));
							}

							rMgr.setRegister(reg.searchRegister(inst.substring(2, 3)), Integer.decode("0X" + mem_str)); // mem_str에
																														// 있는
																														// 값을
																														// int로
																														// 바꿔
																														// 해당
																														// 레지스터에
																														// 저장한다.
						}
					} else {
						char[] mem = rMgr.getMemory(targetAddress, 3);
						String mem_str = "";

						for (int j = 0; j < 3; ++j) { // 메모리에 있는 값을 unpacking하여 mem_str에 저장한다.
							mem_str = mem_str.concat(String.format("%X", (mem[j] & 0xF0) >> 4));
							mem_str = mem_str.concat(String.format("%X", mem[j] & 0x0F));
						}

						rMgr.setRegister(reg.searchRegister(inst.substring(2, 3)), Integer.decode("0X" + mem_str));
					}

					break;

				case nFlag:
					break;

				case iFlag: // immediate addressing 방식인 경우

					rMgr.setRegister(reg.searchRegister(inst.substring(2, 3)), targetAddress);
					break;
				}
				instruction.setTarAdd(targetAddress);
			}

			else if (inst.charAt(0) == 'J') { // Jump 관련 명령어인 경우
				if (inst.compareTo("JSUB") == 0) { // JSUB인 경우 L레지스터에 PC값을 저장하고 pc값을 해당 disp값을 저장하여 jump 한다.
					rMgr.setRegister(reg.searchRegister("L"), rMgr.getRegister(reg.searchRegister("PC")));
					rMgr.setRegister(reg.searchRegister("PC"), Integer.decode("0X" + instruction.disp));
					instruction.setTarAdd(Integer.decode("0X" + instruction.disp));
				} else if (inst.compareTo("JEQ") == 0 || inst.compareTo("JLT") == 0) { // JEQ , JLT 명령어인 경우

					if (flag == true) { // COMP, COMPR, TIXR 명령어 수행 후 flag 상태 -> loop 발생

						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);
						String ta_str = String.format("%04X", targetAddress);
						if (ta_str.length() > 3) // disp가 음수이면 앞부분에서 1을 빼줘야한다.
							targetAddress = Integer.decode("0X" + ta_str.replace('2', '1'));

						rMgr.setRegister(reg.searchRegister("PC"), targetAddress);
						instruction.setTarAdd(targetAddress);

						flag = false;
					} else if (rMgr.flag == false) { // TD 명령어 수행 후 flag 상태 -> loop 발생

						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);
						String ta_str = String.format("%04X", targetAddress);
						if (ta_str.length() > 3)
							targetAddress = Integer.decode("0X" + ta_str.replace('2', '1'));

						rMgr.setRegister(reg.searchRegister("PC"), targetAddress);
						instruction.setTarAdd(targetAddress);
					}

				}

				else if (inst.compareTo("J") == 0) { // J 명령어
					switch (instruction.getFlag(nFlag | iFlag)) {
					case 0:
					case nFlag | iFlag: // n,i 비트가 00이거나 11인 경우 : direct addressing 인 경우
						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);

						String ta_str = String.format("%04X", targetAddress);
						if (ta_str.length() > 3)
							targetAddress = Integer.decode("0X" + ta_str.replace('1', '0'));

						rMgr.setRegister(reg.searchRegister("PC"), targetAddress);
						break;
					case nFlag: // indirect addressing 인 경우
						targetAddress = rMgr.getRegister(reg.searchRegister("PC"))
								+ Integer.decode("0X" + instruction.disp);
						char[] mem = rMgr.getMemory(targetAddress, 3);

						String mem_str = "";

						for (int j = 0; j < 3; ++j) { // 메모리를 unpacking 후 레지스터에 저장
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

			} else if (inst.compareTo("RSUB") == 0) { // RSUB인 경우 L 레지스터에 있는 값을 PC레지스터에 저장한다.
				rMgr.setRegister(reg.searchRegister("PC"), rMgr.getRegister(reg.searchRegister("L")));
			} else if (inst.compareTo("TD") == 0) {
				dev_flag = true;
				rMgr.testDevice("F1");
				instruction.setTarAdd(
						rMgr.getRegister(reg.searchRegister("PC")) + Integer.decode("0X" + instruction.disp));
			} else if (inst.compareTo("RD") == 0) {
				dev_flag = true;
				rMgr.setRegister(reg.searchRegister("A"), rMgr.readDevice("F1")); // F1 디바이스에 읽은 문자를 A 레지스터에 저장한다.
				instruction.setTarAdd(
						rMgr.getRegister(reg.searchRegister("PC")) + Integer.decode("0X" + instruction.disp));

			} else if (inst.compareTo("WD") == 0) {
				dev_flag = true;
				rMgr.writeDevice("05", (char) rMgr.getRegister(reg.searchRegister("A"))); // A레지스터의 오른쪽 1byte를 05디바이스에
																							// 출력한다.
				instruction.setTarAdd(
						rMgr.getRegister(reg.searchRegister("PC")) + Integer.decode("0X" + instruction.disp));
			}

		}

	}

	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {

	}

	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
	}
}
