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
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다. <br>
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/**
	 * inst.data 파일을 불러와 저장하는 공간. 명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * 
	 * @param instFile
	 *            : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}

	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {

		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line = "";
			while ((line = bufReader.readLine()) != null) {
				Instruction inst = new Instruction(line); // 헤당 라인을 파싱한다.
				instMap.put(inst.instruction, inst); // 파싱한 라인을 instMap에 넣어 관리한다.
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

	// get, set, search 등의 함수는 자유 구현

	/*
	 * inst가 instMap에 있는 명령어인지 검사한다. instMap에 있는 경우 true, 없는 경우 false를 리턴한다.
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
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다. instruction과 관련된 정보들을 저장하고 기초적인 연산을
 * 수행한다.
 */
class Instruction {
	/*
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 * 
	 * ex) String instruction; int opcode; int numberOfOperand; String comment;
	 */

	String instruction;
	int numberOfOperand;
	String opcode;

	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	int format;

	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * 
	 * @param line
	 *            : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * 
	 * @param line
	 *            : instruction 명세파일로부터 한줄씩 가져온 문자열
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
