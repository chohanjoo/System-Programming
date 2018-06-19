import java.util.ArrayList;
import java.util.HashMap;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ�
 * �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit ������ �������� ���� ���� */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	InstTable instTab;
	LiteralTable litertab;

	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;

	Register reg = new Register();			// �������� �˻��� ���ؼ� ����
	
	ArrayList<String> extdef;
	ArrayList<String> extref;


	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * 
	 * @param symTab
	 *            : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab
	 *            : instruction ���� ���ǵ� instTable
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
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * 
	 * @param line
	 *            : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		Token token = new Token(line);
		calculNixbpe(token);			// �ش� token�� nixbpe�� ����Ѵ�.
		tokenList.add(token);
	}

	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 �������� ����Ѵ�. instruction table, symbol table ���� �����Ͽ� objectcode�� �����ϰ�, �̸�
	 * �����Ѵ�.
	 * 
	 * @param index
	 */
	public String makeObjectCode(int index) {
		Token tk = tokenList.get(index);
		if (tk.operator != null) {

			String inst = tk.operator;
			if (tk.operator.contains("+"))		// ��ɾ��� �˻��� ���� 4�����̸� +�� �����Ѵ�.
				inst = tk.operator.substring(1);

			if (instTab.instMap.containsKey(inst)) {		// instMap�� �ִ� ��ɾ��� ���
				String opcode = instTab.instMap.get(inst).opcode;
				char nixbpe = tk.nixbpe;

				tk.objectCode = tk.objectCode.concat(Character.toString(opcode.charAt(0)));	//opcode�� �պκ��� objectcode�� �����Ѵ�.
				String opcode2 = Character.toString(opcode.charAt(1));
				int sub_opcode;
				if (opcode2.compareTo("C") == 0)		// opcode�� �޺κ��� 16������ ������ ��� ��Ʈ ������ ���� int�� �ٲ��ش�.
					sub_opcode = 12;
				else
					sub_opcode = Integer.parseInt(opcode2);
				sub_opcode <<= 4;
				nixbpe |= (char) sub_opcode;			// ������ nixbpe �� opcode �޺κ��� | �����Ѵ�.

				if (instTab.instMap.get(inst).format == 2) {		// ��ɾ 2������ ���
					nixbpe >>= 4;
					String str_nixbpe = String.format("%X", (int) nixbpe);
					tk.objectCode = tk.objectCode.concat(str_nixbpe);
				} else if (instTab.instMap.get(inst).format == 3) {		// ��ɾ 3������ ���
					String str_nixbpe = String.format("%02X", (int) nixbpe);
					tk.objectCode = tk.objectCode.concat(str_nixbpe);	//objectcode�� ���������� nixbpe�� �����Ѵ�.
				}

				int address = 0;
				int target_address = 0;
				int pc = tokenList.get(index + 1).location;		// �ش� ��ɾ� ���� ������ �ּҸ� ������.

				String op = tk.operand[0];
				int reg_num = 0;
				if (op.contains("@"))		// symbol ���̺����� �˻��� ���� @���ڴ� �����Ѵ�.
					op = op.substring(1);
				if (tk.operator.compareTo("RSUB") == 0) {		// RSUB�� ��� 
					tk.objectCode = tk.objectCode.concat("000");
					
				} else if (tk.operator.contains("+")) {			// 4������ ��� �ܺ������̹Ƿ� �ּҴ� 0�̴�.
					tk.objectCode = tk.objectCode.concat("00000");
					
				} else if ((target_address = symTab.search(op)) != -1) { // operand�� symbol ���̺� �ִ� ���
					address = target_address - pc;		// Ÿ�� address �� pc�� ���� ���Ͽ� address�� �����Ѵ�.
					if (address < 0)			// ��� ��� ������ ��� ����ũ ������ ���� �ʿ���� 1�� �����.
						address &= 0x00000FFF;
					String ad = String.format("%03X", address);
					tk.objectCode = tk.objectCode.concat(ad);	// ���� �ּҸ� objectcode�� �����Ѵ�.
					
				} else if (tk.operand[0].contains("#")) {		// immediate addressing�� ���
					String ta = tk.operand[0].substring(1);
					tk.objectCode = tk.objectCode.concat("00" + ta);
					
				} else if (tk.operand[0].contains("=")) {		// ���ͷ����̺� symbol�� �ִ� ���
					target_address = litertab.searchLiteral(tk.operand[0]);
					address = target_address - pc;

					String ad = String.format("%03X", address);
					tk.objectCode = tk.objectCode.concat(ad);
					
				} else if ((reg_num = reg.searchRegister(tk.operand[0])) != -1) {		//�������� ���̺� operand�� �ִ� ���
					int numofOperand = instTab.instMap.get(inst).numberOfOperand;

					if (numofOperand == 1)		// ��ɾ��� �ʿ��� operand�� 1���� ���
						tk.objectCode = tk.objectCode.concat(Integer.toString(reg_num) + "0");
					else if (numofOperand == 2) {		// 2���� ���
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
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

	public void setLocation(int locat) {		// �ٷ� ���� �߰��� Token�� location�� �����Ѵ�.
		Token tk = tokenList.get(tokenList.size() - 1);
		tk.location = locat;
		tokenList.set(tokenList.size() - 1, tk);
	}
	

/**
 * �ش� ��ɾ��� nixbpe�� ����Ѵ�.
 */
	private void calculNixbpe(Token tk) {
		if (instTab.instMap.containsKey(tk.operator)) {
			if (instTab.instMap.get(tk.operator).format == 2)
				;
			else if (tk.operand[0].contains("#")) {		// immediate addressing�� ���
				tk.setFlag(iFlag, 1);
			} else if (tk.operand[0].contains("@")) {	// indirect addressing�� ���
				tk.setFlag(nFlag, 1);
				tk.setFlag(pFlag, 1);

			} else if (tk.operator.compareTo("RSUB") == 0) {	// RSUB�� ���
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
			} else {						// direct addressing �� ���
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
				tk.setFlag(pFlag, 1);
			}
		} else if (tk.operator != null && tk.operator.charAt(0) != '.' && tk.operator.contains("+")) {
			if (tk.operand.length == 2 && tk.operand[1].compareTo("X") == 0) {		// x bit�� ����ϴ� ���
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
				tk.setFlag(xFlag, 1);
				tk.setFlag(eFlag, 1);
			} else {							// 4������ ���
				tk.setFlag(nFlag, 1);
				tk.setFlag(iFlag, 1);
				tk.setFlag(eFlag, 1);
			}
		}

	}
	
	public void putExtdef(String symbol) {		//extdef arraylist�� �߰��Ѵ�.
		extdef.add(symbol);
	}
	
	public void putExtref(String symbol) {		//extref arraylist�� �߰��Ѵ�.
		extref.add(symbol);
	}


}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ �� �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. �ǹ� �ؼ��� ������ pass2����
 * object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token {
	// �ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location = 0;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe = 0x00;

	// object code ���� �ܰ迡�� ���Ǵ� ������
	String objectCode;
	int byteSize;

	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�.
	 * 
	 * @param line
	 *            ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		// initialize �߰�
		parsing(line);
		objectCode = new String();
	}

	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * 
	 * @param line
	 *            ��������� ����� ���α׷� �ڵ�.
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
	 * n,i,x,b,p,e flag�� �����Ѵ�. <br>
	 * <br>
	 * 
	 * ��� �� : setFlag(nFlag, 1); <br>
	 * �Ǵ� setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag
	 *            : ���ϴ� ��Ʈ ��ġ
	 * @param value
	 *            : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
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
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� <br>
	 * <br>
	 * 
	 * ��� �� : getFlag(nFlag) <br>
	 * �Ǵ� getFlag(nFlag|iFlag)
	 * 
	 * @param flags
	 *            : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
