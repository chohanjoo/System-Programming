package SP18_simulator;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	HashMap<String, Integer> extref;
	

	public SymbolTable() {
		symbolList = new ArrayList<>();
		addressList = new ArrayList<>();
		extref = new HashMap<>();
	}
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param address : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int address) {
		symbolList.add(symbol);
		addressList.add(address);
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newaddress) {
		
		int index = -1;
		for(int i=0;i<symbolList.size();++i)
			if(symbolList.get(i).compareTo(symbol)==0)
				index = i;
		
		if(index!=-1)
			addressList.set(index, newaddress);
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = -1;
		if(symbolList.contains(symbol)) {
			int index = symbolList.indexOf(symbol);
			address = addressList.get(index);
		}
		return address;
	}
	
	
	
}
