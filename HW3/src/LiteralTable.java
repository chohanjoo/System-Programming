import java.util.HashMap;

public class LiteralTable {

	HashMap<String, Integer> literalList;

	boolean ltorg_flag;

	public LiteralTable() {
		literalList = new HashMap<String, Integer>();
		ltorg_flag = false;

	}

	/**
	 * literalList�� ���ͷ��� ���� ��� ���ͷ��� �־��ش�.
	 * @param symbol
	 * @param location
	 */
	public void putLiteral(String symbol, int location) {
		if (searchLiteral(symbol) == -1) {
			literalList.put(symbol, location);
		}
	}

	/**
	 * �ش� literalList�� ���ͷ��� �ִ��� �˻��Ѵ�.
	 * ���ͷ��� �����ϸ��ּҰ��� �����ϰ�, ������ -1�� �����Ѵ�.
	 * @param symbol
	 * @return
	 */
	public int searchLiteral(String symbol) {
		Integer address = 0;

		address = literalList.get(symbol);

		if (address == null)
			address = -1;

		return address;
	}

	/**
	 * ���ͷ����� �������� string�� �����ϴ� �޼ҵ��̴�.
	 * ex) =C'EOF' �� ��� EOF�� �����Ѵ�.
	 * @param literal
	 * @return
	 */
	public String getLiteral(String literal) {
		String temp = new String(literal);
		if (literal.charAt(0) == '=')
			temp = literal.substring(3, literal.length() - 1);	
		else if (literal.charAt(0) == 'X')
			temp = literal.substring(2, literal.length() - 1);

		return temp;
	}
}
