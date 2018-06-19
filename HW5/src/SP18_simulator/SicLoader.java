package SP18_simulator;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. <br>
 * <br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;

	public SicLoader(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * 
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr = resourceManager;
	}

	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵���
	 * �Ѵ�. load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * 
	 * @param objectCode
	 *            �о���� ����
	 */
	public void load(File objectCode) {

		try {
			FileReader filereader = new FileReader(objectCode);

			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			ArrayList<String> temp = new ArrayList<>();
			int currPosition = 0;

			while ((line = bufReader.readLine()) != null) { // �� ���ξ� �д´�.

				if (line.compareTo("") != 0)
					switch (line.charAt(0)) {
					case 'H': // H record�� ���
						String progName = line.substring(1, 7).trim();

						if (rMgr.symtabList.extref.containsKey(progName))
							rMgr.symtabList.putSymbol(progName, rMgr.length); // RDREC, WRREC �� ��� symbol ���̺� �����Ѵ�
						else {
							rMgr.progName = line.substring(1, 7).trim(); // ���α׷� �̸� copy ����
							rMgr.startAddress = line.substring(7, 13); // ���α׷� �����ּ� ����
						}

						rMgr.length += Integer.decode("0X" + line.substring(13, 19)); // section ���� ���α׷� ���̸� ������ ��ü ���α׷�
																						// ���̸� ���Ѵ�.
						currPosition = rMgr.length - Integer.decode("0X" + line.substring(13, 19)); // ���� section�� �����ּ�
						break;
					case 'D': // D record �� ���
						for (int i = 0; i < line.length() / 12; ++i) {
							String symbol = line.substring(i * 12 + 1, i * 12 + 7).trim();
							int address = Integer.decode("0X" + line.substring(i * 12 + 7, i * 12 + 13).trim());
							rMgr.symtabList.putSymbol(symbol, address); // �ɺ����̺� ����
						}
						break;
					case 'R': // R record�� ���
						for (int i = 0; i < line.length() / 6; ++i) {
							String symbol = line.substring(1 + i * 6, 7 + i * 6).trim();
							if (rMgr.symtabList.search(symbol) == -1) // �ɺ����̺� ������ ����
								rMgr.symtabList.extref.put(symbol, -1);
						}
						break;
					case 'T': // T record�� ���
						int start = currPosition + Integer.decode("0X" + line.substring(1, 7));
						String tline = line.substring(9).trim(); // tline�� �������� objectcode ������ �ǹ��Ѵ�.
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

								rMgr.setMemory(start + k / 2, packing); // char 2byte�� �о� packing�Ͽ� �޸𸮿� �����Ѵ�.
								packing = 0;
							}
							++k;

						}

						break;
					case 'M': // M record�� ���
						int index = currPosition + Integer.decode("0X" + line.substring(1, 7)); // ������ �޸� �ּ�
						int number = Integer.decode("0X" + line.substring(7, 9)); // ������ �޸� ����
						String symbol = line.substring(10).trim();
						int symIndex = rMgr.symtabList.symbolList.indexOf(symbol);
						if (symIndex != -1) {
							int symAddr = rMgr.symtabList.addressList.get(symIndex); // symbol �ּ�

							if (number % 2 == 0) { // ������ �޸� ������ 06�� ���
								String operator = line.substring(9, 10);
								String symAddr_str = String.format("%06X", symAddr);
								int mPacking = 0;
								if (operator.compareTo("+") == 0) { // symbol �ּҸ� packing�Ͽ� ������ �޸𸮿� �����ش�.
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

								else if (operator.compareTo("-") == 0) // symbol �ּҸ� packing�Ͽ� ������ �޸𸮿��� ����.
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

							} else { // ������ �޸� ������ 05�� ���
								String symAddr_str = String.format("%05X", symAddr);
								int mPacking = 0;
								for (int i = 0; i < number; ++i) {
									if (i == 0) { // ������ �޸� �ּ�+0.5��° �� ����
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

										rMgr.setMemory(index + i / 2, mPacking); // packing�Ͽ� �޸𸮿� �����Ѵ�.
										mPacking = 0;
									}
								}
							}
						} else
							temp.add(String.format("%06X", currPosition) + line); // ������� �ϴ� symbol�� �ּҰ� ���� �������� ���� ���
																					// temp ArrayList�� �����ϰ� ���߿� �����Ѵ�.

					}

			}
			for (int i = 0; i < temp.size(); ++i) { // ������ �̷� �͵��� �ٽ� �������ش�.
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
