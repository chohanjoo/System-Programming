package SP18_simulator;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. <br>
 * <br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;

	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * 
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr = resourceManager;
	}

	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록
	 * 한다. load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * 
	 * @param objectCode
	 *            읽어들인 파일
	 */
	public void load(File objectCode) {

		try {
			FileReader filereader = new FileReader(objectCode);

			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			ArrayList<String> temp = new ArrayList<>();
			int currPosition = 0;

			while ((line = bufReader.readLine()) != null) { // 한 라인씩 읽는다.

				if (line.compareTo("") != 0)
					switch (line.charAt(0)) {
					case 'H': // H record인 경우
						String progName = line.substring(1, 7).trim();

						if (rMgr.symtabList.extref.containsKey(progName))
							rMgr.symtabList.putSymbol(progName, rMgr.length); // RDREC, WRREC 인 경우 symbol 테이블에 저장한다
						else {
							rMgr.progName = line.substring(1, 7).trim(); // 프로그램 이름 copy 저장
							rMgr.startAddress = line.substring(7, 13); // 프로그램 시작주소 저장
						}

						rMgr.length += Integer.decode("0X" + line.substring(13, 19)); // section 마다 프로그램 길이를 더해줘 전체 프로그램
																						// 길이를 구한다.
						currPosition = rMgr.length - Integer.decode("0X" + line.substring(13, 19)); // 현재 section의 시작주소
						break;
					case 'D': // D record 인 경우
						for (int i = 0; i < line.length() / 12; ++i) {
							String symbol = line.substring(i * 12 + 1, i * 12 + 7).trim();
							int address = Integer.decode("0X" + line.substring(i * 12 + 7, i * 12 + 13).trim());
							rMgr.symtabList.putSymbol(symbol, address); // 심볼테이블에 저장
						}
						break;
					case 'R': // R record인 경우
						for (int i = 0; i < line.length() / 6; ++i) {
							String symbol = line.substring(1 + i * 6, 7 + i * 6).trim();
							if (rMgr.symtabList.search(symbol) == -1) // 심볼테이블에 없으면 저장
								rMgr.symtabList.extref.put(symbol, -1);
						}
						break;
					case 'T': // T record인 경우
						int start = currPosition + Integer.decode("0X" + line.substring(1, 7));
						String tline = line.substring(9).trim(); // tline은 실질적인 objectcode 라인을 의미한다.
						int k = 0;
						int packing = 0;
						while (k < tline.length()) {
							if ((k + 1) % 2 == 1) {

								if (tline.charAt(k) >= 0x30 && tline.charAt(k) <= 0x39)
									packing = (tline.charAt(k) - 0x30);

								else if (tline.charAt(k) >= 0x41 && tline.charAt(k) <= 0x46)
									packing = (tline.charAt(k) - 0x37);

								packing <<= 4;

							} else {
								if (tline.charAt(k) >= 0x30 && tline.charAt(k) <= 0x39)
									packing += (tline.charAt(k) - 0x30);

								else if (tline.charAt(k) >= 0x41 && tline.charAt(k) <= 0x46)
									packing += (tline.charAt(k) - 0x37);

								rMgr.setMemory(start + k / 2, packing); // char 2byte를 읽어 packing하여 메모리에 저장한다.
								packing = 0;
							}
							++k;

						}

						break;
					case 'M': // M record인 경우
						int index = currPosition + Integer.decode("0X" + line.substring(1, 7)); // 수정할 메모리 주소
						int number = Integer.decode("0X" + line.substring(7, 9)); // 수정할 메모리 개수
						String symbol = line.substring(10).trim();
						int symIndex = rMgr.symtabList.symbolList.indexOf(symbol);
						if (symIndex != -1) {
							int symAddr = rMgr.symtabList.addressList.get(symIndex); // symbol 주소

							if (number % 2 == 0) { // 수정할 메모리 개수가 06인 경우
								String operator = line.substring(9, 10);
								String symAddr_str = String.format("%06X", symAddr);
								int mPacking = 0;
								if (operator.compareTo("+") == 0) { // symbol 주소를 packing하여 수정할 메모리에 더해준다.
									for (int i = 0; i < number; ++i) {
										if ((i + 1) % 2 == 1) {
											if (symAddr_str.charAt(i) >= 0x30 && symAddr_str.charAt(i) <= 0x39)
												mPacking = symAddr_str.charAt(i) - 0x30;

											else if (symAddr_str.charAt(i) >= 0x41 && symAddr_str.charAt(i) <= 0x46)
												mPacking = symAddr_str.charAt(i) - 0x37;

											mPacking <<= 4;
										} else {
											if (symAddr_str.charAt(i) >= 0x30 && symAddr_str.charAt(i) <= 0x39)
												mPacking += symAddr_str.charAt(i) - 0x30;

											else if (symAddr_str.charAt(i) >= 0x41 && symAddr_str.charAt(i) <= 0x46)
												mPacking += symAddr_str.charAt(i) - 0x37;

											rMgr.setMemory(index + i / 2, mPacking);
											mPacking = 0;
										}
									}
								}

								else if (operator.compareTo("-") == 0) // symbol 주소를 packing하여 수정할 메모리에서 뺀다.
									for (int i = 0; i < number; ++i) {
										if ((i + 1) % 2 == 1) {
											if (symAddr_str.charAt(i) >= 0x30 && symAddr_str.charAt(i) <= 0x39)
												mPacking = symAddr_str.charAt(i) - 0x30;

											else if (symAddr_str.charAt(i) >= 0x41 && symAddr_str.charAt(i) <= 0x46)
												mPacking = symAddr_str.charAt(i) - 0x37;

											mPacking <<= 4;
										} else {
											if (symAddr_str.charAt(i) >= 0x30 && symAddr_str.charAt(i) <= 0x39)
												mPacking += symAddr_str.charAt(i) - 0x30;

											else if (symAddr_str.charAt(i) >= 0x41 && symAddr_str.charAt(i) <= 0x46)
												mPacking += symAddr_str.charAt(i) - 0x37;

											char[] mn = rMgr.getMemory(index + i / 2, 1);
											rMgr.setMemory(index + i / 2, mn[0] - mPacking);
											mPacking = 0;
										}

									}

							} else { // 수정할 메모리 개수가 05인 경우
								String symAddr_str = String.format("%05X", symAddr);
								int mPacking = 0;
								for (int i = 0; i < number; ++i) {
									if (i == 0) { // 수저할 메모리 주소+0.5번째 값 수정
										char[] temp_memory = rMgr.getMemory(index, 1);
										char temp2 = temp_memory[0];
										if (symAddr_str.charAt(0) >= 0x30 && symAddr_str.charAt(0) <= 0x39) {
											temp2 += symAddr_str.charAt(0) - 0x30;
											rMgr.setMemory(index, temp2);
										} else if (symAddr_str.charAt(0) >= 0x41 && symAddr_str.charAt(0) <= 0x46) {
											temp2 += symAddr_str.charAt(0) - 0x37;
											rMgr.setMemory(index, temp2);
										}

									} else if (i % 2 == 1) {
										if (symAddr_str.charAt(i) >= 0x30 && symAddr_str.charAt(i) <= 0x39)
											mPacking = symAddr_str.charAt(i) - 0x30;

										else if (symAddr_str.charAt(i) >= 0x41 && symAddr_str.charAt(i) <= 0x46)
											mPacking = symAddr_str.charAt(i) - 0x37;

										mPacking <<= 4;
									} else if (i % 2 == 0) {
										if (symAddr_str.charAt(i) >= 0x30 && symAddr_str.charAt(i) <= 0x39)
											mPacking += symAddr_str.charAt(i) - 0x30;

										else if (symAddr_str.charAt(i) >= 0x41 && symAddr_str.charAt(i) <= 0x46)
											mPacking += symAddr_str.charAt(i) - 0x37;

										rMgr.setMemory(index + i / 2, mPacking); // packing하여 메모리에 저장한다.
										mPacking = 0;
									}
								}
							}
						} else
							temp.add(String.format("%06X", currPosition) + line); // 더해줘야 하는 symbol의 주소가 아직 정해지지 않은 경우
																					// temp ArrayList에 저장하고 나중에 수정한다.

					}

			}
			for (int i = 0; i < temp.size(); ++i) { // 수정을 미룬 것들을 다시 수정해준다.
				line = temp.get(i);

				String[] word = line.split("M");
				int position = Integer.decode("0X" + word[0]);
				int index = position + Integer.decode("0X" + word[1].substring(0, 6));
				int number = Integer.decode("0X" + word[1].substring(6, 8));
				String symbol = word[1].substring(9).trim();
				int symIndex = rMgr.symtabList.symbolList.indexOf(symbol);
				int symAddr = rMgr.symtabList.addressList.get(symIndex);
				String symAddr_str = String.format("%05X", symAddr);
				int mPacking = 0;
				for (int j = 0; j < number; ++j) {
					if (j == 0) {
						char[] temp_memory = rMgr.getMemory(index, 1);
						char temp2 = temp_memory[0];

						if (symAddr_str.charAt(0) >= 0x30 && symAddr_str.charAt(0) <= 0x39) {
							temp2 += symAddr_str.charAt(0) - 0x30;
							rMgr.setMemory(index, temp2);
						} else if (symAddr_str.charAt(0) >= 0x41 && symAddr_str.charAt(0) <= 0x46) {
							temp2 += symAddr_str.charAt(0) - 0x37;
							rMgr.setMemory(index, temp2);
						}

					} else if (j % 2 == 1) {
						if (symAddr_str.charAt(j) >= 0x30 && symAddr_str.charAt(j) <= 0x39)
							mPacking = symAddr_str.charAt(j) - 0x30;

						else if (symAddr_str.charAt(j) >= 0x41 && symAddr_str.charAt(j) <= 0x46)
							mPacking = symAddr_str.charAt(j) - 0x37;

						mPacking <<= 4;
					} else if (j % 2 == 0) {
						if (symAddr_str.charAt(j) >= 0x30 && symAddr_str.charAt(j) <= 0x39)
							mPacking += symAddr_str.charAt(j) - 0x30;

						else if (symAddr_str.charAt(j) >= 0x41 && symAddr_str.charAt(j) <= 0x46)
							mPacking += symAddr_str.charAt(j) - 0x37;

						rMgr.setMemory(index + j / 2, mPacking);
						mPacking = 0;
					}
				}

			}
			bufReader.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

}
