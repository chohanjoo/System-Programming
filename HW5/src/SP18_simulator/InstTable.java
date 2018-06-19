package SP18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�. <br>
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/**
	 * inst.data ������ �ҷ��� �����ϴ� ����. ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * 
	 * @param instFile
	 *            : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}

	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {

		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			while ((line = bufReader.readLine()) != null) {
				Instruction inst = new Instruction(line); // ��� ������ �Ľ��Ѵ�.
				instMap.put(inst.instruction, inst); // �Ľ��� ������ instMap�� �־� �����Ѵ�.
			}

			bufReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		// ...
	}

	// get, set, search ���� �Լ��� ���� ����

	/*
	 * inst�� instMap�� �ִ� ��ɾ����� �˻��Ѵ�. instMap�� �ִ� ��� true, ���� ��� false�� �����Ѵ�.
	 */
	public String search(String opcode) {

		Set key = instMap.keySet();

		for (Iterator iterator = key.iterator(); iterator.hasNext();) {
			String keyName = (String) iterator.next();
			Instruction instruction = instMap.get(keyName);

			if (instruction.opcode.compareTo(opcode) == 0)
				return instruction.instruction;
		}
		return "";

	}

}

/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����. instruction�� ���õ� �������� �����ϰ� �������� ������
 * �����Ѵ�.
 */
class Instruction {
	/*
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 * 
	 * ex) String instruction; int opcode; int numberOfOperand; String comment;
	 */

	String instruction;
	int numberOfOperand;
	String opcode;

	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;

	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * 
	 * @param line
	 *            : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * 
	 * @param line
	 *            : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub

		String[] word = line.split("\t");

		instruction = new String(word[0]);
		numberOfOperand = Integer.parseInt(word[1]);
		format = Integer.parseInt(word[2]);
		opcode = new String(word[3]);
	}

}
