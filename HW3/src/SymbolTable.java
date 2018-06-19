import java.util.ArrayList;
import java.util.HashMap;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.

	

	public SymbolTable() {
		symbolList = new ArrayList<String>();
		locationList = new ArrayList<Integer>();

	}
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol
	 *            : ���� �߰��Ǵ� symbol�� label
	 * @param location
	 *            : �ش� symbol�� ������ �ּҰ� <br>
	 * 			<br>
	 *            ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����.
	 *            ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location) {
		if (search(symbol) == -1) {			// symbol ���̺� symbol�� ���ٸ� �߰��Ѵ�.
			symbolList.add(symbol);
			locationList.add(location);
		} else
			System.out.println("The current index exists in the symbolTable.");
	}

	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * 
	 * @param symbol
	 *            : ������ ���ϴ� symbol�� label
	 * @param newLocation
	 *            : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		int index;

		if ((index = search(symbol)) != -1) {
			locationList.set(index, newLocation);
		}
	}

	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * 
	 * @param symbol
	 *            : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;
		int index = 0;
		index = symbolList.indexOf(symbol);
		if (index != -1) {
			address = locationList.get(index);

			return address;
		} else
			return -1;
	}

	

}
