import java.util.HashMap;

public class LiteralTable {

	HashMap<String, Integer> literalList;

	boolean ltorg_flag;

	public LiteralTable() {
		literalList = new HashMap<String, Integer>();
		ltorg_flag = false;

	}

	/**
	 * literalList에 리터럴이 없는 경우 리터럴을 넣어준다.
	 * @param symbol
	 * @param location
	 */
	public void putLiteral(String symbol, int location) {
		if (searchLiteral(symbol) == -1) {
			literalList.put(symbol, location);
		}
	}

	/**
	 * 해당 literalList에 리터럴이 있는지 검색한다.
	 * 리터럴이 존재하면주소값을 리턴하고, 없으면 -1을 리턴한다.
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
	 * 리터럴에서 실질적인 string을 리턴하는 메소드이다.
	 * ex) =C'EOF' 의 경우 EOF를 리턴한다.
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
