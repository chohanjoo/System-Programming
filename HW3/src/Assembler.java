import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.text.MaskFormatter;
import javax.swing.text.html.HTMLDocument.Iterator;

import org.omg.IOP.Codec;

/**
 * Assembler : �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�. ���α׷��� ���� �۾��� ������
 * ����. <br>
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. <br>
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. <br>
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) <br>
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) <br>
 * 
 * <br>
 * <br>
 * �ۼ����� ���ǻ��� : <br>
 * 1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ����
 * �ȵȴ�.<br>
 * 2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 * 3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 * 4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br>
 * <br>
 * + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� ��
 * �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ���� */
	ArrayList<SymbolTable> symtabList;

	// ArrayList<LiteralTable> litertabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ���� */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. <br>
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;

	ArrayList<ArrayList<String>> sec_codeList; // codeList�� sect���� �����Ѵ�.

	int locator = 0;

	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile
	 *            : instruction ���� �ۼ��� ���� �̸�.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
		sec_codeList = new ArrayList<ArrayList<String>>();
	}

	/**
	 * ��U���� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");

		assembler.loadInputFile("input.txt");

		assembler.pass1();
		assembler.printSymbolTable("symtab_20160333");

		assembler.pass2();
		assembler.printObjectCode("output_20160333");

	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * 
	 * @param fileName
	 *            : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		int count = 0;
		String path = InstTable.class.getResource("").getPath(); // ���� Ŭ������ ���� ��θ� �����´�.

		try {
			File file = new File(path + "\\" + fileName); // ���� ������Ʈ�� bin ������ filename
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

			TokenTable tokenTable;
			ArrayList<Token> toklist;
			Token token;

			for (int i = 0; i < TokenList.size(); ++i) { // tokentable �� sect ���� �����Ѵ�.
				codeList = sec_codeList.get(i); // ���� sect�� codeList�� �����Ѵ�.
				tokenTable = TokenList.get(i); // ���� sect�� tokenTable�� �����Ѵ�.
				toklist = tokenTable.tokenList;
				int l = 0, m = 0;
				for (int j = 0; j < toklist.size(); ++j) { // ���� sect�� tokenlist�� �����Ѵ�.
					token = toklist.get(j);
					if (file.isFile() && file.canWrite()) { // ���Ͽ� �� �� �ִ� �����̸�.
						if (j == 0) { // H recode�� �����ϱ� ����

							int lengh = 0;
							if (toklist.get(toklist.size() - 1).operator.compareTo("EQU") == 0)
								lengh = toklist.get(toklist.size() - 2).location;
							else if (toklist.get(toklist.size() - 1).operator.compareTo("END") == 0)
								lengh = toklist.get(toklist.size() - 2).location + 4;
							else
								lengh = toklist.get(toklist.size() - 1).location + 3;
							String line = String.format("H%-6s%06X%06X", token.label, token.location, lengh);
							bufferedWriter.write(line);
							bufferedWriter.newLine();
						}

						else if (token.operator != null && token.operator.compareTo("EXTDEF") == 0) { // D recode�� ����Ѵ�.
							int address;
							String def = "";
							String temp = "D";
							
							for(int k=0;k<tokenTable.extdef.size();++k) {
								address = tokenTable.symTab.search(tokenTable.extdef.get(k));
								def = String.format("%-6s%06X", tokenTable.extdef.get(k), address);
								temp = temp.concat(def);
							}
							
							bufferedWriter.write(temp);
							bufferedWriter.newLine();
						} else if (token.operator != null && token.operator.compareTo("EXTREF") == 0) { // R recode�� ����Ѵ�.
																										
							String ref = "";
							String temp = "R";
							
							for(int k=0;k<tokenTable.extref.size();++k) {
								ref = String.format("%-6s", tokenTable.extref.get(k));
								temp = temp.concat(ref);
							}
							
							bufferedWriter.write(temp);
							bufferedWriter.newLine();
						}

						else if (token.label.compareTo(".") != 0 && count != codeList.size() - 1) { 	// codeList�� �ִ� objectcode�� T recode�� ����Ѵ�.
																									
							String temp = "T";
							String start_loc = String.format("%06X", token.location); 		// ���� �ּҸ� �����Ѵ�.
							temp = temp.concat(start_loc);
							int cnt = 0;

							for (; l < codeList.size(); ++l) { 		// T recode �� ���� objectcode ���̸� ���Ѵ�.
								cnt += codeList.get(l).length();
								if (cnt >= 60) // ���̰� 60�� ������ ������ ���� ���´�.
									break;
							}

							if (l == codeList.size()) 		// ���̰� 60���� ������ �ٷ� 2�� ������.
								cnt /= 2;
							else 							// ���̰� 60���� ũ�� �ٷ� �� objectcode�� ���̸� ���� �� 2�� ������.
								cnt = (cnt - codeList.get(l).length()) / 2;

							String len = String.format("%02X", cnt);	// �� line�� objectcode ���̸� len�� ������ �� temp ������ �ٿ��ش�.
							temp = temp.concat(len);

							for (; m < l; ++m)							// temp ������ objectcode���� �̾���δ�.
								temp = temp.concat(codeList.get(m));

							j += l - 1;					// j ������ ���� objectcode ���� ���� ���������ν� �ߺ����� �ʵ��� �Ѵ�.
							count = l - 1;				// count ������  codeList�� �ִ� objectcode�� �ߺ��Ǿ� ��µ��� �ʵ��� �Ѵ�.

							bufferedWriter.write(temp);
							bufferedWriter.newLine();
						}

					}
				}
				if (tokenTable.litertab.ltorg_flag) {		// RESW ������ �ִ� ���ͷ��� T recode�� ����Ѵ�.
					String string = "T";
					LiteralTable litertable = TokenList.get(i).litertab;

					java.util.Iterator<String> iterator = litertable.literalList.keySet().iterator();	// iterator �� hashmap�� �� ��ҿ� �����Ѵ�.
					while (iterator.hasNext()) {
						String key = (String) iterator.next();
						String literal = litertable.getLiteral(key);
						String tp = String.format("%06X%02X", litertable.literalList.get(key), literal.length());
						string = string.concat(tp);
						if (key.charAt(1) == 'X')
							codeList.add(literal);
						else if (key.charAt(1) == 'C') {
							String temp = new String();
							char[] ch = literal.toCharArray();

							for (int k = 0; k < ch.length; ++k) {				// ex) =C'EOF' �� ��� objectcode�� �ƽ�Ű�ڵ�� ����ǹǷ� ���ڸ� �ƽ�Ű�ڵ�� �ٲ��ش�.
								String tp2 = String.format("%X", (int) ch[k]);		
								temp = temp.concat(tp2);
							}

							string = string.concat(temp);
						}

					}
					bufferedWriter.write(string);
					bufferedWriter.newLine();
				}

				for (int j = 0; j < toklist.size(); ++j) {				// M recode�� ����Ѵ�.
					token = toklist.get(j);

					if (token.objectCode.length() == 8) {				// �ܺ��� ������ �����ϴ� ���
						String modi = "M";
						String line = String.format("%06X05+%-6s", token.location + 1, token.operand[0]);
						modi = modi.concat(line);
						if (file.isFile() && file.canWrite()) {
							bufferedWriter.write(modi);
							bufferedWriter.newLine();
						}
					} else if (token.label.compareTo("MAXLEN") == 0 && token.operator.compareTo("EQU") != 0) {		// MAXLEN ���� �ܺ� ������ ������ �ʿ��� ���
						String word[] = token.operand[0].split("-");

						String modi = "M";
						String line = String.format("%06X%02X+%-6s", token.location, word[0].length(), word[0]);
						modi = modi.concat(line);
						if (file.isFile() && file.canWrite()) {
							bufferedWriter.write(modi);
							bufferedWriter.newLine();
						}

						modi = "M";
						line = String.format("%06X%02X-%-6s", token.location, word[1].length(), word[1]);
						modi = modi.concat(line);
						if (file.isFile() && file.canWrite()) {
							bufferedWriter.write(modi);
							bufferedWriter.newLine();
						}

					}

				}

				String start_address = String.format("%06X", toklist.get(0).location);		// E recode�� ����Ѵ�.
				bufferedWriter.write("E" + start_address);
				bufferedWriter.newLine();
				bufferedWriter.newLine();
			}

			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * 
	 * @param fileName
	 *            : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub

		String path = InstTable.class.getResource("").getPath(); // ���� Ŭ������ ���� ��θ� �����´�.

		try {
			File file = new File(path + "\\" + fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			String[] word;
			while ((line = bufReader.readLine()) != null) {			// ���Ͽ��� �� ���� �д´�.
				word = line.split("\t");
				if (word.length == 2) {
					System.out.print(word[0] + "\t");				// symbol �� ����Ѵ�.
					System.out.printf("%4X\n", Integer.parseInt(word[1]));		// symbol�� �ּҸ� 16������ ����Ѵ�.
				} else
					System.out.println(line);
			}

			bufReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	/**
	 * pass1 ������ �����Ѵ�.<br>
	 * 1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����<br>
	 * 2) label�� symbolTable�� ����<br>
	 * <br>
	 * <br>
	 * ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		SymbolTable symTable = new SymbolTable();
		TokenTable tokenTable;
		Token tk;
		int pre_sect_index = 0;
		for (int i = 0; i < lineList.size(); ++i) {				// linelist�� �� ��Ҿ� �����Ѵ�.
			String line = lineList.get(i);
			String[] word = line.split("\t");

			if (word[0].compareTo("") != 0 && word[0].compareTo(".") != 0) {	// label�� ���� ���� .���� �ּ�ó�� �� ������ �ƴ� ���
				
				if (word[1].compareTo("CSECT") == 0) {					// operator �� CSECT �� ���
					symtabList.add(symTable);							// symtable�� list�� �����Ѵ�.
					tokenTable = new TokenTable(symTable, instTable);	// symtable�� ����� �����Ƿ� tokentable�� �ν��Ͻ��� �����Ѵ�.
					symTable = new SymbolTable();
					symTable.putSymbol(word[0], 0);						// ���ο� symtable�� sect�̸��� �����Ѵ�.

					locator = 0;
					for (int j = pre_sect_index; j < i; ++j) {			// �ش� sect�� line �� �پ� �Ľ��Ͽ� tokentable���� �����Ѵ�.
						tokenTable.putToken(lineList.get(j));
						tk = tokenTable.getToken(tokenTable.tokenList.size() - 1);

						if (tk.operand != null && tk.operand[0].contains("=")) {	// ���ͷ��� ��� tokentable �ȿ� �ִ� litertab���� �����Ѵ�.
							tokenTable.litertab.putLiteral(tk.operand[0], 0);
						}
						
						if(tk.operator!=null && tk.operator.compareTo("EXTDEF")==0) {	// EXTDEF�� ���� ��� tokenTable�� �ִ� extdef arraylist�� �߰��Ѵ�.
							for(int k=0;k<tk.operand.length;++k) 
							tokenTable.putExtdef(tk.operand[k]);
							
						}
						else if(tk.operator!=null && tk.operator.compareTo("EXTREF")==0) {	// EXTREF�� ���� ��� tokenTable�� �ִ� extref arraylist�� �߰��Ѵ�.
							for(int k=0;k<tk.operand.length;++k) 
								tokenTable.putExtref(tk.operand[k]);
								
						}
						
						instLocation(tokenTable, tk);		// �ش� ��ɾ��� location�� ����Ѵ�.

					}

					TokenList.add(tokenTable);			// tokentable�� sect���� tokenlist�� �����Ѵ�.

					pre_sect_index = i;

				} else													// operator�� CSECT�� �ƴ� ��� symbol ���̺� �����Ѵ�.
					symTable.putSymbol(word[0], 0);
			}

			if (i == lineList.size() - 1) {					// ������ sect�� ��� (������ sect�� ���� if�� �ɸ��� �����Ƿ�)
				
				symtabList.add(symTable);

				tokenTable = new TokenTable(symTable, instTable);

				locator = 0;

				for (int j = pre_sect_index; j <= i; ++j) {
					tokenTable.putToken(lineList.get(j));

					tk = tokenTable.getToken(tokenTable.tokenList.size() - 1);
					if (tk.operand != null && tk.operand[0].contains("=")) {
						tokenTable.litertab.putLiteral(tk.operand[0], 0);
					}
					
					if(tk.operator!=null && tk.operator.compareTo("EXTDEF")==0) {
						for(int k=0;k<tk.operand.length;++k) 
						tokenTable.putExtdef(tk.operand[k]);
						
					}
					else if(tk.operator!=null && tk.operator.compareTo("EXTREF")==0) {
						for(int k=0;k<tk.operand.length;++k) 
							tokenTable.putExtref(tk.operand[k]);
							
					}
					instLocation(tokenTable, tk);

				}

				TokenList.add(tokenTable);

				pre_sect_index = i;
			}

		}

		outputSymbolTable("symtab_20160333");			// symbol ���̺��� ���Ͽ� �����Ѵ�.

	}

	private void instLocation(TokenTable tb, Token t) {			// �ش� ��ɾ��� location�� ����Ѵ�.
		TokenTable tokenTable = tb;
		Token tk = t;
		SymbolTable symTable = symtabList.get(symtabList.size() - 1);
		LiteralTable literalTable = tokenTable.litertab;
		if (tk.operator != null && tk.operator.compareTo("LTORG") == 0				// ���ͷ��� ��� 
				|| tk.operator != null && tk.operator.compareTo("END") == 0) {
			
			tokenTable.setLocation(0);

			if (tk.operator.compareTo("LTORG") == 0)			// LTORG�� ������ ��� flag�� �����Ѵ�.
				literalTable.ltorg_flag = true;

			java.util.Iterator<String> iterator = literalTable.literalList.keySet().iterator();
			while (iterator.hasNext()) {							// ���ͷ��� �ּҸ� �� �����Ѵ�.
				String key = (String) iterator.next();
				literalTable.literalList.put(key, locator);
				locator += literalTable.getLiteral(key).length();		//���� ��ɾ��� �ּҸ� �����Ѵ�.
			}

		} else if (tk.operator != null && tk.operator.compareTo("EQU") == 0) {		// operator �� EQU �� ���
			int result = 0;
			int index = 0;
			if (tk.operand[0].compareTo("*") != 0) {			// ex) MAXLEN�� �����ּҰ��� ����Ѵ�.
				String[] word = tk.operand[0].split("-");

				index = symTable.symbolList.indexOf(word[0]);
				result += symTable.locationList.get(index);
				index = symTable.symbolList.indexOf(word[1]);
				result -= symTable.locationList.get(index);

				tokenTable.setLocation(result);
			} else
				tokenTable.setLocation(locator);
		} else
			tokenTable.setLocation(locator);

		if (tk.label != null && tk.label.compareTo("") != 0 && tk.label.compareTo(".") != 0) {		// symbol�� ��� �ּҸ� �缳���Ѵ�.
			int index = symTable.symbolList.indexOf(tk.label);
			Token tk_2 = tokenTable.tokenList.get(tokenTable.tokenList.size() - 1);
			symTable.locationList.set(index, tk_2.location);
		}
		if (tk.operator != null) {
			if (instTable.search(tk.operator)) {		// ��ɾ instTable�� �ִ� ���
				Instruction inst = instTable.instMap.get(tk.operator);
				if (tk.operator.contains("+"))			// 4���� ��ɾ��� ���
					locator += 4;
				else if (inst.format == 3)				// 3���� ��ɾ��� ���
					locator += 3;
				else if (inst.format == 2)				// 2���� ��ɾ��� ���
					locator += 2;
			} else if (tk.operator.compareTo("WORD") == 0)
				locator += 3;
			else if (tk.operator.compareTo("RESW") == 0)
				locator += 3 * Integer.parseInt(tk.operand[0]);		
			else if (tk.operator.compareTo("RESB") == 0)
				locator += Integer.parseInt(tk.operand[0]);
			else if (tk.operator.compareTo("BYTE") == 0) {
				if (tk.operand[0].charAt(0) == 'X')
					locator += 1;
			}
		}

	}

	/**
	 * pass2 ������ �����Ѵ�.<br>
	 * 1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub

		LiteralTable litertable;
		for (int i = 0; i < TokenList.size(); ++i) {						// tokentable�� sect���� �����Ѵ�.
			for (int j = 0; j < TokenList.get(i).tokenList.size(); ++j) {
				String objectcode = TokenList.get(i).makeObjectCode(j);
				if (objectcode.compareTo("") != 0)
					codeList.add(objectcode);								//codeList�� objectcode�� �����Ѵ�.
			}

			litertable = TokenList.get(i).litertab;

			if (litertable.literalList.size() != 0 && !litertable.ltorg_flag) {		// objectcode ������ resw �� resb�� ���� ���ͷ��� ��� codelist�� �����Ѵ�.
				java.util.Iterator<String> iterator = litertable.literalList.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					String literal = litertable.getLiteral(key);

					if (key.charAt(1) == 'X')
						codeList.add(literal);
					else if (key.charAt(1) == 'C') {
						String temp = new String();
						char[] ch = literal.toCharArray();

						for (int k = 0; k < ch.length; ++k) {
							String tp = String.format("%X", (int) ch[k]);		// char�� �ƽ�Ű �ڵ�� ��ȯ�Ͽ� string���� �����Ѵ�.
							temp = temp.concat(tp);
						}

						codeList.add(temp);
					}

				}
			}

			sec_codeList.add(codeList);
			codeList = new ArrayList<String>();
		}
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.<br>
	 * 
	 * @param inputFile
	 *            : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub

		String path = InstTable.class.getResource("").getPath(); // ���� Ŭ������ ���� ��θ� �����´�.

		try {
			File file = new File(path + "\\" + inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			while ((line = bufReader.readLine()) != null) {
				lineList.add(line);				// ���Ͽ��� �� �� �� �о� linelist�� �����Ѵ�.
			}

			bufReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	private void outputSymbolTable(String fileName) {		// symbol ���̺��� ���Ͽ� �����Ѵ�.

		String path = InstTable.class.getResource("").getPath(); // ���� Ŭ������ ���� ��θ� �����´�.

		try {
			File file = new File(path + "\\" + fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < symtabList.size(); ++i) {
				SymbolTable st = symtabList.get(i);

				for (int j = 0; j < st.symbolList.size(); ++j) {
					if (file.isFile() && file.canWrite()) {
						bufferedWriter.write(st.symbolList.get(j) + "\t" + st.locationList.get(j));	// ���Ͽ� <symbol	�ּ�> �������� �����Ѵ�.
						bufferedWriter.newLine();
					}
				}
				if (file.isFile() && file.canWrite())
					bufferedWriter.newLine();

			}

			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
