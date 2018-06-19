package SP18_simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * deviceManager��  ����̽��� �̸��� �Է¹޾��� �� �ش� ����̽��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	ArrayList<String> deviceManager =new ArrayList<String>();
	char[] memory = new char[65536]; // String���� �����ؼ� ����Ͽ��� ������.
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList;
	// �̿ܿ��� �ʿ��� ���� �����ؼ� ����� ��.

	int length;
	String progName = "";
	String startAddress;
	
	InstTable instTab;
	
	boolean flag = false;
	int read_location = 0;		//����̽����� ���� ��ġ�� ����Ѵ�.
	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	
	public ResourceManager() {
		symtabList = new SymbolTable();
		instTab = new InstTable("C:\\Users\\����\\Downloads\\SP18_simulator\\src\\SP18_simulator\\inst.data");
		initializeResource();
	}
	public void initializeResource(){
		for(int i=0;i<65536;++i)
			memory[i] = '*';
		
		for(int i=0;i<10;++i)
			register[i] = 0;
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 */
	public void closeDevice() {
		
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		try {
		File file = new File("C:\\Users\\����\\Downloads\\SP18_simulator\\src\\SP18_simulator\\" + devName + ".txt");
		FileReader filereader = new FileReader(file);
		
		filereader.close();
		
		flag = true;
		deviceManager.add(devName);
		}catch(FileNotFoundException e) {
			flag = false;
		}catch(IOException e) {
			flag = false;
		}
		
	}

	/**
	 * ����̽��κ��� 1���� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @return ������ ������
	 */
	public char readDevice(String devName){
		try {
			File file = new File("C:\\Users\\����\\Downloads\\SP18_simulator\\src\\SP18_simulator\\" + devName + ".txt");
			FileReader filereader = new FileReader(file);
			int ch = 0;
			int i=0;
			while((ch=filereader.read())!=-1) {
				if(i == read_location) {
					read_location++;
					filereader.close();
					if(ch == '0')
						ch = '0' - 0x30;
					return (char)ch;
				}
				else
				++i;
			}
			filereader.close();
		}catch(FileNotFoundException e) {
			System.out.println("error");
		}catch(IOException e) {
			System.out.println("error");

		}
		
		return (char)0;
		
	}

	/**
	 * ����̽��� 1���� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 */
	public void writeDevice(String devName, char data){
		try {
			File file = new File("C:\\Users\\����\\Downloads\\SP18_simulator\\src\\SP18_simulator\\" + devName + ".txt");
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true));
			PrintWriter pw = new PrintWriter(bufferedWriter,true);
			
			pw.write(data);
			pw.close();
		}catch(IOException e) {
			
		}
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public char[] getMemory(int location, int num){
		String temp = "";
		for(int i=0;i<num;++i)
			temp = temp.concat(String.format("%c",memory[location+i]));
		return temp.toCharArray();
		
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, char[] data, int num){

		for(int i=0;i<num;++i)
			memory[locate + i] = data[i];	
	}
	
	public void setMemory(int locate, int data) {
		memory[locate] = (char)data;
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
	
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		String temp= String.format("%06X", data);		// data�� 6�ڸ� String���� �ٲ۴�.
		int packing = 0;
		char[] result = new char[3];
		for(int i=0;i<6;++i) {
			if((i+1)%2==1) {
				if(temp.charAt(i)>=0x30 && temp.charAt(i)<=0x39) 
					packing = temp.charAt(i) - 0x30;
				
				else if(temp.charAt(i)>=0x41 && temp.charAt(i)<=0x46) 
					packing = temp.charAt(i) - 0x37;
				
				packing<<=4;
				}
				else {
					if(temp.charAt(i)>=0x30 && temp.charAt(i)<=0x39) 
						packing += temp.charAt(i) - 0x30;
					
					else if(temp.charAt(i)>=0x41 && temp.charAt(i)<=0x46) 
						packing += temp.charAt(i) - 0x37;
					
					result[i/2] = (char)packing;	// char 2byte�� �о� packing�Ͽ� result �迭�� �����Ѵ�.
					packing = 0;
				}
		}
		return result;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int charToInt(char[] data){
		char[] mem = data;
		String mem_str = "";
		for(int j=0;j<3;++j) {		// data�� �ִ� ���� unpacking�Ͽ� mem_str�� �����Ѵ�. �̸� �����Ѵ�.
			mem_str = mem_str.concat(String.format("%X", (mem[j] &0xF0)>>4));
			mem_str = mem_str.concat(String.format("%X", mem[j] & 0x0F));
		}
		
		
		return  Integer.decode("0X" + mem_str);
	}
}