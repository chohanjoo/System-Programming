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
 * Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 프로그램의 수행 작업은 다음과
 * 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br>
 * <br>
 * 작성중의 유의사항 : <br>
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 안된다.<br>
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br>
 * <br>
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<SymbolTable> symtabList;

	// ArrayList<LiteralTable> litertabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	ArrayList<ArrayList<String>> sec_codeList; // codeList를 sect별로 관리한다.

	int locator = 0;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile
	 *            : instruction 명세를 작성한 파일 이름.
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
	 * 어셐블러의 메인 루틴
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName
	 *            : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		int count = 0;
		String path = InstTable.class.getResource("").getPath(); // 현재 클래스의 절대 경로를 가져온다.

		try {
			File file = new File(path + "\\" + fileName); // 현재 프로젝트의 bin 폴더의 filename
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

			TokenTable tokenTable;
			ArrayList<Token> toklist;
			Token token;

			for (int i = 0; i < TokenList.size(); ++i) { // tokentable 을 sect 별로 접근한다.
				codeList = sec_codeList.get(i); // 현재 sect의 codeList를 저장한다.
				tokenTable = TokenList.get(i); // 현재 sect의 tokenTable을 저장한다.
				toklist = tokenTable.tokenList;
				int l = 0, m = 0;
				for (int j = 0; j < toklist.size(); ++j) { // 현재 sect의 tokenlist에 접근한다.
					token = toklist.get(j);
					if (file.isFile() && file.canWrite()) { // 파일에 쓸 수 있는 상태이면.
						if (j == 0) { // H recode를 저장하기 위해

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

						else if (token.operator != null && token.operator.compareTo("EXTDEF") == 0) { // D recode를 출력한다.
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
						} else if (token.operator != null && token.operator.compareTo("EXTREF") == 0) { // R recode를 출력한다.
																										
							String ref = "";
							String temp = "R";
							
							for(int k=0;k<tokenTable.extref.size();++k) {
								ref = String.format("%-6s", tokenTable.extref.get(k));
								temp = temp.concat(ref);
							}
							
							bufferedWriter.write(temp);
							bufferedWriter.newLine();
						}

						else if (token.label.compareTo(".") != 0 && count != codeList.size() - 1) { 	// codeList에 있는 objectcode를 T recode에 출력한다.
																									
							String temp = "T";
							String start_loc = String.format("%06X", token.location); 		// 시작 주소를 저장한다.
							temp = temp.concat(start_loc);
							int cnt = 0;

							for (; l < codeList.size(); ++l) { 		// T recode 한 줄의 objectcode 길이를 구한다.
								cnt += codeList.get(l).length();
								if (cnt >= 60) // 길이가 60을 넘으면 루프를 빠져 나온다.
									break;
							}

							if (l == codeList.size()) 		// 길이가 60보다 작으면 바로 2로 나눈다.
								cnt /= 2;
							else 							// 길이가 60보다 크면 바로 전 objectcode의 길이를 빼준 뒤 2로 나눈다.
								cnt = (cnt - codeList.get(l).length()) / 2;

							String len = String.format("%02X", cnt);	// 한 line의 objectcode 길이를 len에 저장한 뒤 temp 변수에 붙여준다.
							temp = temp.concat(len);

							for (; m < l; ++m)							// temp 변수에 objectcode들을 이어붙인다.
								temp = temp.concat(codeList.get(m));

							j += l - 1;					// j 변수에 읽은 objectcode 라인 수를 저장함으로써 중복되지 않도록 한다.
							count = l - 1;				// count 변수로  codeList에 있는 objectcode가 중복되어 출력되지 않도록 한다.

							bufferedWriter.write(temp);
							bufferedWriter.newLine();
						}

					}
				}
				if (tokenTable.litertab.ltorg_flag) {		// RESW 다음에 있는 리터럴을 T recode에 출력한다.
					String string = "T";
					LiteralTable litertable = TokenList.get(i).litertab;

					java.util.Iterator<String> iterator = litertable.literalList.keySet().iterator();	// iterator 로 hashmap의 각 요소에 접근한다.
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

							for (int k = 0; k < ch.length; ++k) {				// ex) =C'EOF' 의 경우 objectcode는 아스키코드로 저장되므로 문자를 아스키코드로 바꿔준다.
								String tp2 = String.format("%X", (int) ch[k]);		
								temp = temp.concat(tp2);
							}

							string = string.concat(temp);
						}

					}
					bufferedWriter.write(string);
					bufferedWriter.newLine();
				}

				for (int j = 0; j < toklist.size(); ++j) {				// M recode를 출력한다.
					token = toklist.get(j);

					if (token.objectCode.length() == 8) {				// 외부의 변수를 참조하는 경우
						String modi = "M";
						String line = String.format("%06X05+%-6s", token.location + 1, token.operand[0]);
						modi = modi.concat(line);
						if (file.isFile() && file.canWrite()) {
							bufferedWriter.write(modi);
							bufferedWriter.newLine();
						}
					} else if (token.label.compareTo("MAXLEN") == 0 && token.operator.compareTo("EQU") != 0) {		// MAXLEN 같이 외부 변수의 연산이 필요한 경우
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

				String start_address = String.format("%06X", toklist.get(0).location);		// E recode를 출력한다.
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName
	 *            : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub

		String path = InstTable.class.getResource("").getPath(); // 현재 클래스의 절대 경로를 가져온다.

		try {
			File file = new File(path + "\\" + fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			String[] word;
			while ((line = bufReader.readLine()) != null) {			// 파일에서 한 줄을 읽는다.
				word = line.split("\t");
				if (word.length == 2) {
					System.out.print(word[0] + "\t");				// symbol 을 출력한다.
					System.out.printf("%4X\n", Integer.parseInt(word[1]));		// symbol의 주소를 16진수로 출력한다.
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
	 * pass1 과정을 수행한다.<br>
	 * 1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 * 2) label을 symbolTable에 정리<br>
	 * <br>
	 * <br>
	 * 주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		SymbolTable symTable = new SymbolTable();
		TokenTable tokenTable;
		Token tk;
		int pre_sect_index = 0;
		for (int i = 0; i < lineList.size(); ++i) {				// linelist의 한 요소씩 접근한다.
			String line = lineList.get(i);
			String[] word = line.split("\t");

			if (word[0].compareTo("") != 0 && word[0].compareTo(".") != 0) {	// label이 없는 경우와 .으로 주석처리 된 라인이 아닐 경우
				
				if (word[1].compareTo("CSECT") == 0) {					// operator 가 CSECT 일 경우
					symtabList.add(symTable);							// symtable을 list에 저장한다.
					tokenTable = new TokenTable(symTable, instTable);	// symtable이 만들어 졌으므로 tokentable의 인스턴스를 생성한다.
					symTable = new SymbolTable();
					symTable.putSymbol(word[0], 0);						// 새로운 symtable에 sect이름을 저장한다.

					locator = 0;
					for (int j = pre_sect_index; j < i; ++j) {			// 해당 sect의 line 한 줄씩 파싱하여 tokentable에서 관리한다.
						tokenTable.putToken(lineList.get(j));
						tk = tokenTable.getToken(tokenTable.tokenList.size() - 1);

						if (tk.operand != null && tk.operand[0].contains("=")) {	// 리터럴일 경우 tokentable 안에 있는 litertab에서 관리한다.
							tokenTable.litertab.putLiteral(tk.operand[0], 0);
						}
						
						if(tk.operator!=null && tk.operator.compareTo("EXTDEF")==0) {	// EXTDEF를 만날 경우 tokenTable에 있는 extdef arraylist에 추가한다.
							for(int k=0;k<tk.operand.length;++k) 
							tokenTable.putExtdef(tk.operand[k]);
							
						}
						else if(tk.operator!=null && tk.operator.compareTo("EXTREF")==0) {	// EXTREF를 만날 경우 tokenTable에 있는 extref arraylist에 추가한다.
							for(int k=0;k<tk.operand.length;++k) 
								tokenTable.putExtref(tk.operand[k]);
								
						}
						
						instLocation(tokenTable, tk);		// 해당 명령어의 location을 계산한다.

					}

					TokenList.add(tokenTable);			// tokentable을 sect별로 tokenlist에 저장한다.

					pre_sect_index = i;

				} else													// operator가 CSECT가 아닐 경우 symbol 테이블에 저장한다.
					symTable.putSymbol(word[0], 0);
			}

			if (i == lineList.size() - 1) {					// 마지막 sect일 경우 (마지막 sect는 위의 if에 걸리지 않으므로)
				
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

		outputSymbolTable("symtab_20160333");			// symbol 테이블을 파일에 저장한다.

	}

	private void instLocation(TokenTable tb, Token t) {			// 해당 명령어의 location을 계산한다.
		TokenTable tokenTable = tb;
		Token tk = t;
		SymbolTable symTable = symtabList.get(symtabList.size() - 1);
		LiteralTable literalTable = tokenTable.litertab;
		if (tk.operator != null && tk.operator.compareTo("LTORG") == 0				// 리터럴일 경우 
				|| tk.operator != null && tk.operator.compareTo("END") == 0) {
			
			tokenTable.setLocation(0);

			if (tk.operator.compareTo("LTORG") == 0)			// LTORG를 만났을 경우 flag을 설정한다.
				literalTable.ltorg_flag = true;

			java.util.Iterator<String> iterator = literalTable.literalList.keySet().iterator();
			while (iterator.hasNext()) {							// 리터럴의 주소를 재 설정한다.
				String key = (String) iterator.next();
				literalTable.literalList.put(key, locator);
				locator += literalTable.getLiteral(key).length();		//다음 명령어의 주소를 저장한다.
			}

		} else if (tk.operator != null && tk.operator.compareTo("EQU") == 0) {		// operator 가 EQU 인 경우
			int result = 0;
			int index = 0;
			if (tk.operand[0].compareTo("*") != 0) {			// ex) MAXLEN의 절대주소값을 계산한다.
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

		if (tk.label != null && tk.label.compareTo("") != 0 && tk.label.compareTo(".") != 0) {		// symbol인 경우 주소를 재설정한다.
			int index = symTable.symbolList.indexOf(tk.label);
			Token tk_2 = tokenTable.tokenList.get(tokenTable.tokenList.size() - 1);
			symTable.locationList.set(index, tk_2.location);
		}
		if (tk.operator != null) {
			if (instTable.search(tk.operator)) {		// 명령어가 instTable에 있는 경우
				Instruction inst = instTable.instMap.get(tk.operator);
				if (tk.operator.contains("+"))			// 4형식 명령어인 경우
					locator += 4;
				else if (inst.format == 3)				// 3형식 명령어인 경우
					locator += 3;
				else if (inst.format == 2)				// 2형식 명령어인 경우
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
	 * pass2 과정을 수행한다.<br>
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub

		LiteralTable litertable;
		for (int i = 0; i < TokenList.size(); ++i) {						// tokentable을 sect별로 접근한다.
			for (int j = 0; j < TokenList.get(i).tokenList.size(); ++j) {
				String objectcode = TokenList.get(i).makeObjectCode(j);
				if (objectcode.compareTo("") != 0)
					codeList.add(objectcode);								//codeList에 objectcode를 저장한다.
			}

			litertable = TokenList.get(i).litertab;

			if (litertable.literalList.size() != 0 && !litertable.ltorg_flag) {		// objectcode 다음에 resw 나 resb이 없는 리터럴인 경우 codelist에 저장한다.
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
							String tp = String.format("%X", (int) ch[k]);		// char를 아스키 코드로 변환하여 string으로 저장한다.
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
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * 
	 * @param inputFile
	 *            : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub

		String path = InstTable.class.getResource("").getPath(); // 현재 클래스의 절대 경로를 가져온다.

		try {
			File file = new File(path + "\\" + inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			while ((line = bufReader.readLine()) != null) {
				lineList.add(line);				// 파일에서 한 줄 씩 읽어 linelist에 저장한다.
			}

			bufReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	private void outputSymbolTable(String fileName) {		// symbol 테이블을 파일에 저장한다.

		String path = InstTable.class.getResource("").getPath(); // 현재 클래스의 절대 경로를 가져온다.

		try {
			File file = new File(path + "\\" + fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < symtabList.size(); ++i) {
				SymbolTable st = symtabList.get(i);

				for (int j = 0; j < st.symbolList.size(); ++j) {
					if (file.isFile() && file.canWrite()) {
						bufferedWriter.write(st.symbolList.get(j) + "\t" + st.locationList.get(j));	// 파일에 <symbol	주소> 형식으로 저장한다.
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
