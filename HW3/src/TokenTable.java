import java.util.ArrayList;
import java.util.HashMap;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	InstTable instTab;
	LiteralTable litertab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	Register reg = new Register();			// 레지스터 검색을 위해서 선언
	
	ArrayList<String> extdef;
	ArrayList<String> extref;


	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab
	 *            : 해당 section과 연결되어있는 symbol table
	 * @param instTab
	 *            : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		this.symTab = symTab;
		this.instTab = instTab;
		litertab = new LiteralTable();
		tokenList = new ArrayList<Token>();
		extdef = new ArrayList<String>();
		extref = new ArrayList<String>();

		
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line
	 *            : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		Token token = new Token(line);
		calculNixbpe(token);			// 해당 token의 nixbpe를 계산한다.
		tokenList.add(token);
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public String makeObjectCode(int index) {
		Token tk = tokenList.get(index);
		if (tk.operator != null) {

			String inst = tk.operator;
			if (tk.operator.contains("+"))		// 명령어의 검색을 위해 4형식이면 +를 제거한다.
				inst = tk.operator.substring(1);

			if (instTab.instMap.containsKey(inst)) {		// instMap에 있는 명령어인 경우
				String opcode = instTab.instMap.get(inst).opcode;
				char nixbpe = tk.nixbpe;

				tk.objectCode = tk.objectCode.concat(Character.toString(opcode.charAt(0)));	//opcode의 앞부분을 objectcode에 저장한다.
				String opcode2 = Character.toString(opcode.charAt(1));
				int sub_opcode;
				if (opcode2.compareTo("C") == 0)		// opcode의 뒷부분이 16진수인 문자인 경우 비트 연산을 위해 int로 바꿔준다.
					sub_opcode = 12;
				else
					sub_opcode = Integer.parseInt(opcode2);
				sub_opcode <<= 4;
				nixbpe |= (char) sub_opcode;			// 기존의 nixbpe 와 opcode 뒷부분을 | 연산한다.

				if (instTab.instMap.get(inst).format == 2) {		// 명령어가 2형식인 경우
					nixbpe >>= 4;
					String str_nixbpe = String.format("%X", (int) nixbpe);
					tk.objectCode = tk.objectCode.concat(str_nixbpe);
				} else if (instTab.instMap.get(inst).format == 3) {		// 명령어가 3형식인 경우
					String str_nixbpe = String.format("%02X", (int) nixbpe);
					tk.objectCode = tk.objectCode.concat(str_nixbpe);	//objectcode에 최종연산한 nixbpe를 저장한다.
				}

				int address = 0;
				int target_address = 0;
				int pc = tokenList.get(index + 1).location;		// 해당 명령어 다음 명렁어의 주소를 가진다.

				String op = tk.operand[0];
				int reg_num = 0;
				if (op.contains("@"))		// symbol 테이블에서의 검색을 위해 @문자는 제거한다.
					op = op.substring(1);
				if (tk.operator.compareTo("RSUB") == 0) {		// RSUB인 경우 
					tk.objectCode = tk.objectCode.concat("000");
					
				} else if (tk.operator.contains("+")) {			// 4형식인 경우 외부참조이므로 주소는 0이다.
					tk.objectCode = tk.objectCode.concat("00000");
					
				} else if ((target_address = symTab.search(op)) != -1) { // operand가 symbol 테이블에 있는 경우
					address = target_address - pc;		// 타겟 address 와 pc의 차를 구하여 address에 저장한다.
					if (address < 0)			// 계산 결과 음수인 경우 마스크 연산을 해줘 필요없는 1을 지운다.
						address &= 0x00000FFF;
					String ad = String.format("%03X", address);
					tk.objectCode = tk.objectCode.concat(ad);	// 계산된 주소를 objectcode에 저장한다.
					
				} else if (tk.operand[0].contains("#")) {		// immediate addressing인 경우
					String ta = tk.operand[0].substring(1);
					tk.objectCode = tk.objectCode.concat("00" + ta);
					
				} else if (tk.operand[0].contains("=")) {		// 리터럴테이블에 symbol이 있는 경우
					target_address = litertab.searchLiteral(tk.operand[0]);
					address = target_address - pc;

					String ad = String.format("%03X", address);
					tk.objectCode = tk.objectCode.concat(ad);
					
				} else if ((reg_num = reg.searchRegister(tk.operand[0])) != -1) {		//레지스터 테이블에 operand가 있는 경우
					int numofOperand = instTab.instMap.get(inst).numberOfOperand;

					if (numofOperand == 1)		// 명령어의 필요한 operand가 1개인 경우
						tk.objectCode = tk.objectCode.concat(Integer.toString(reg_num) + "0");
					else if (numofOperand == 2) {		// 2개인 경우
						tk.objectCode = tk.objectCode.concat(Integer.toString(reg_num));
						reg_num = reg.searchRegister(tk.operand[1]);
						tk.objectCode = tk.objectCode.concat(Integer.toString(reg_num));
					}

				}

			} else if (tk.operator.compareTo("BYTE") == 0) {
				tk.objectCode = tk.objectCode.concat(litertab.getLiteral(tk.operand[0]));
			} else if (tk.operator.compareTo("WORD") == 0) {
				tk.objectCode = tk.objectCode.concat("000000");
			}

		}

		return tk.objectCode;
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

	public void setLocation(int locat) {		// 바로 전에 추가한 Token의 location을 변경한다.
		Token tk = tokenList.get(tokenList.size() - 1);
		tk.location = locat;
		tokenList.set(tokenList.size() - 1, tk);
	}
	

/**
 * 해당 명령어의 nixbpe를 계산한다.
 */
	private void calculNixbpe(Token tk) {
		if (instTab.instMap.containsKey(tk.operator)) {
			if (instTab.instMap.get(tk.operator).format == 2)
				;
			else if (tk.operand[0].contains("#")) {		// immediate addressing인 경우
				tk.setFlag(iFlag, 1);
			} else if (tk.operand[0].contains("@")) {	// indirect addressing인 경우
				tk.setFlag(nFlag, 1);
				tk.setFlag(pFlag, 1);

			} else if (tk.operator.compareTo("RSUB") == 0) {	// RSUB인 경우
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
			} else {						// direct addressing 인 경우
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
				tk.setFlag(pFlag, 1);
			}
		} else if (tk.operator != null && tk.operator.charAt(0) != '.' && tk.operator.contains("+")) {
			if (tk.operand.length == 2 && tk.operand[1].compareTo("X") == 0) {		// x bit를 사용하는 경우
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
				tk.setFlag(xFlag, 1);
				tk.setFlag(eFlag, 1);
			} else {							// 4형식인 경우
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
				tk.setFlag(eFlag, 1);
			}
		}

	}
	
	public void putExtdef(String symbol) {		//extdef arraylist에 추가한다.
		extdef.add(symbol);
	}
	
	public void putExtref(String symbol) {		//extref arraylist에 추가한다.
		extref.add(symbol);
	}


}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	// 의미 분석 단계에서 사용되는 변수들
	int location = 0;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe = 0x00;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		// initialize 추가
		parsing(line);
		objectCode = new String();
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[] word = line.split("\t");
		switch (word.length) {
		case 1:
			label = new String(word[0]);
			break;
		case 2:
			label = new String(word[0]);
			operator = new String(word[1]);
			break;
		case 3:
			label = new String(word[0]);
			operator = new String(word[1]);
			operand = word[2].split(",");
			break;
		case 4:
			label = new String(word[0]);
			operator = new String(word[1]);
			operand = word[2].split(",");
			comment = new String(word[3]);
			break;
		}

	}

	/**
	 * n,i,x,b,p,e flag를 설정한다. <br>
	 * <br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 * 또는 setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag
	 *            : 원하는 비트 위치
	 * @param value
	 *            : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		char cflag = (char) flag;

		if (value == 1)
			nixbpe |= cflag;
		else if (value == 0) {
			switch (flag) {
			case 1:
				nixbpe &= 0xfe;
				break;
			case 2:
				nixbpe &= 0xfd;
				break;
			case 4:
				nixbpe &= 0xfb;
				break;
			case 8:
				nixbpe &= 0xf7;
				break;
			case 16:
				nixbpe &= 0xef;
				break;
			case 32:
				nixbpe &= 0xdf;
			}
		}

	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br>
	 * <br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 * 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags
	 *            : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
