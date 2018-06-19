package SP18_simulator;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.server.RMIClassLoader;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator extends JFrame {
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager);

	private JPanel contentPane;
	private JTextField fileNameText;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField aDecText;
	private JTextField xDecText;
	private JTextField lDecText;
	private JTextField pcDecText;
	private JTextField programNameText;
	private JTextField startObjectText;
	private JTextField lenProgText;
	private JTextField aHexText;
	private JTextField xHexText;
	private JTextField lHexText;
	private JTextField pcHexText;
	private JTextField swText;
	private JTextField bDecText;
	private JTextField bHexText;
	private JTextField sDecText;
	private JTextField sHexText;
	private JTextField tDecText;
	private JTextField tHexText;
	private JTextField fHexText;
	private JTextField addrFirstInstText;
	private JTextField startMemoryText;
	private JTextField targetAddrText;
	private JTextField deviceText;
	JList<String> list;
	JScrollPane jlist;
	JList list_1;
	JScrollPane jlist_1;
	DefaultListModel<String> model = new DefaultListModel<>(); // instruction jlist 요소 리스트
	DefaultListModel<String> model_2 = new DefaultListModel<>(); // log jlist 요소 리스트
	File file = null;

	int count = 0;
	boolean all_step = true;

	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program) {
		// ...
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep() {
		sicSimulator.oneStep();

	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep() {

		count = 0;
		while (true) {
			oneStep();
			update();
			count++;
			if (count != 0 && resourceManager.getRegister(8) == 0x00)
				break;
		}

	};

	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update() {
		ObjectCode inst = sicSimulator.codeList.get(sicSimulator.codeList.size() - 1);
		String objectcode = "";
		for (int i = 0; i < inst.objectCode.length; ++i)
			objectcode = objectcode.concat(String.format("%02X", (int) inst.objectCode[i])); // objectcode를 string으로
																								// 만든다.

		model.addElement(objectcode); // instruction list 에 objectcode를 추가한다.
		list.setSelectedIndex(model.size()-1);
		model_2.addElement(inst.inst_name); // log list에 명령어 이름을 추가한다.
		list_1.setSelectedIndex(model_2.size()-1);

		aDecText.setText(String.format("%d", resourceManager.getRegister(0)));
		aHexText.setText(String.format("%06X", resourceManager.getRegister(0)));
		xDecText.setText(String.format("%d", resourceManager.getRegister(1)));
		xHexText.setText(String.format("%06X", resourceManager.getRegister(1)));
		lDecText.setText(String.format("%d", resourceManager.getRegister(2)));
		lHexText.setText(String.format("%06X", resourceManager.getRegister(2)));
		bHexText.setText(String.format("%06X", resourceManager.getRegister(3)));
		bDecText.setText(String.format("%d", resourceManager.getRegister(3)));
		sDecText.setText(String.format("%d", resourceManager.getRegister(4)));
		sHexText.setText(String.format("%06X", resourceManager.getRegister(4)));
		tDecText.setText(String.format("%d", resourceManager.getRegister(5)));
		tHexText.setText(String.format("%06X", resourceManager.getRegister(5)));
		fHexText.setText(String.format("%06X", resourceManager.getRegister(6)));
		pcDecText.setText(String.format("%d", resourceManager.getRegister(8)));
		pcHexText.setText(String.format("%06X", resourceManager.getRegister(8)));
		swText.setText(String.format("%06X", resourceManager.getRegister(9)));
		targetAddrText.setText(String.format("%06X", inst.targetAddress));

		if (resourceManager.deviceManager.size() != 0)
			deviceText.setText(resourceManager.deviceManager.get(resourceManager.deviceManager.size() - 1)); // device
																												// 이름을
																												// 출력한다.

	};

	public void initalize() {
		programNameText.setText(resourceManager.progName);
		startObjectText.setText(resourceManager.startAddress);
		lenProgText.setText(Integer.toHexString(resourceManager.length));

		aDecText.setText("0");
		aHexText.setText("0");
		xDecText.setText("0");
		xHexText.setText("0");
		lDecText.setText("0");
		lHexText.setText("0");
		bHexText.setText("0");
		bDecText.setText("0");
		sDecText.setText("0");
		sHexText.setText("0");
		tDecText.setText("0");
		tHexText.setText("0");
		fHexText.setText("0");
		pcDecText.setText("0");
		pcHexText.setText("0");
		swText.setText("0");
		startMemoryText.setText("0");
		addrFirstInstText.setText("0");
	}

	public static void main(String[] args) {
		VisualSimulator vs = new VisualSimulator();

		vs.gui();
		vs.setVisible(true);

	}

	public void gui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 677, 800);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenu mnNewMenu_1 = new JMenu("About");
		menuBar.add(mnNewMenu_1);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		fileNameText = new JTextField();
		fileNameText.setBounds(90, 24, 161, 24);
		contentPane.add(fileNameText);
		fileNameText.setColumns(10);

		JLabel lblNewLabel = new JLabel("FileName :");
		lblNewLabel.setBounds(14, 27, 80, 18);
		contentPane.add(lblNewLabel);

		JButton openButton = new JButton("open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();

				int ret = chooser.showOpenDialog(null);
				if (ret != JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(null, "No File");
					return;
				}

				file = chooser.getSelectedFile();
				load(file); // 선택한 파일을 메모리에 load한다.
				initalize();
				fileNameText.setText(chooser.getName(file));
			}
		});
		openButton.setBounds(265, 23, 80, 27);
		contentPane.add(openButton);

		JPanel panel = new JPanel();
		panel.setBounds(14, 62, 313, 117);
		panel.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "H(Header Record)"));
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel_1 = new JLabel("Program Name :");
		lblNewLabel_1.setBounds(14, 28, 122, 18);
		panel.add(lblNewLabel_1);

		JLabel lblDdd = new JLabel("Start Address of");
		lblDdd.setBounds(14, 43, 155, 21);
		panel.add(lblDdd);

		JLabel lblNewLabel_14 = new JLabel("Object Program :");
		lblNewLabel_14.setBounds(42, 62, 112, 18);
		panel.add(lblNewLabel_14);

		JLabel label = new JLabel("Length of Program :");
		label.setBounds(14, 87, 155, 18);
		panel.add(label);

		programNameText = new JTextField();
		programNameText.setBounds(162, 25, 116, 24);
		panel.add(programNameText);
		programNameText.setColumns(10);

		startObjectText = new JTextField();
		startObjectText.setColumns(10);
		startObjectText.setBounds(162, 55, 116, 24);
		panel.add(startObjectText);

		lenProgText = new JTextField();
		lenProgText.setColumns(10);
		lenProgText.setBounds(162, 84, 116, 24);
		panel.add(lenProgText);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(14, 191, 313, 180);
		panel_1.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "Register"));
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JLabel lblNewLabel_4 = new JLabel("Dec");
		lblNewLabel_4.setBounds(109, 12, 62, 18);
		panel_1.add(lblNewLabel_4);

		JLabel lblNewLabel_5 = new JLabel("Hex");
		lblNewLabel_5.setBounds(220, 12, 62, 18);
		panel_1.add(lblNewLabel_5);

		JLabel lblNewLabel_6 = new JLabel("A (#0)");
		lblNewLabel_6.setBounds(14, 36, 62, 18);
		panel_1.add(lblNewLabel_6);

		JLabel lblNewLabel_7 = new JLabel("X (#1)");
		lblNewLabel_7.setBounds(14, 61, 62, 18);
		panel_1.add(lblNewLabel_7);

		JLabel lblNewLabel_8 = new JLabel("L (#2)");
		lblNewLabel_8.setBounds(14, 88, 62, 18);
		panel_1.add(lblNewLabel_8);

		JLabel lblNewLabel_9 = new JLabel("PC(#8)");
		lblNewLabel_9.setBounds(14, 115, 62, 18);
		panel_1.add(lblNewLabel_9);

		JLabel lblNewLabel_10 = new JLabel("SW(#9)");
		lblNewLabel_10.setBounds(14, 144, 62, 18);
		panel_1.add(lblNewLabel_10);

		aDecText = new JTextField();
		aDecText.setBounds(83, 36, 82, 24);
		panel_1.add(aDecText);
		aDecText.setColumns(10);

		xDecText = new JTextField();
		xDecText.setBounds(83, 61, 82, 24);
		panel_1.add(xDecText);
		xDecText.setColumns(10);

		lDecText = new JTextField();
		lDecText.setBounds(83, 88, 82, 24);
		panel_1.add(lDecText);
		lDecText.setColumns(10);

		pcDecText = new JTextField();
		pcDecText.setBounds(83, 115, 82, 24);
		panel_1.add(pcDecText);
		pcDecText.setColumns(10);

		aHexText = new JTextField();
		aHexText.setColumns(10);
		aHexText.setBounds(200, 36, 82, 24);
		panel_1.add(aHexText);

		xHexText = new JTextField();
		xHexText.setColumns(10);
		xHexText.setBounds(200, 61, 82, 24);
		panel_1.add(xHexText);

		lHexText = new JTextField();
		lHexText.setColumns(10);
		lHexText.setBounds(200, 88, 82, 24);
		panel_1.add(lHexText);

		pcHexText = new JTextField();
		pcHexText.setColumns(10);
		pcHexText.setBounds(200, 115, 82, 24);
		panel_1.add(pcHexText);

		swText = new JTextField();
		swText.setBounds(83, 144, 199, 24);
		panel_1.add(swText);
		swText.setColumns(10);

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(14, 383, 313, 152);
		panel_2.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "Register(for XE)"));
		contentPane.add(panel_2);
		panel_2.setLayout(null);

		JLabel label_1 = new JLabel("Dec");
		label_1.setBounds(109, 15, 62, 18);
		panel_2.add(label_1);

		JLabel label_2 = new JLabel("Hex");
		label_2.setBounds(220, 15, 62, 18);
		panel_2.add(label_2);

		JLabel label_3 = new JLabel("B (#3)");
		label_3.setBounds(14, 36, 62, 18);
		panel_2.add(label_3);

		bDecText = new JTextField();
		bDecText.setColumns(10);
		bDecText.setBounds(83, 36, 82, 24);
		panel_2.add(bDecText);

		bHexText = new JTextField();
		bHexText.setColumns(10);
		bHexText.setBounds(200, 36, 82, 24);
		panel_2.add(bHexText);

		JLabel label_4 = new JLabel("S (#4)");
		label_4.setBounds(14, 61, 62, 18);
		panel_2.add(label_4);

		sDecText = new JTextField();
		sDecText.setColumns(10);
		sDecText.setBounds(83, 61, 82, 24);
		panel_2.add(sDecText);

		sHexText = new JTextField();
		sHexText.setColumns(10);
		sHexText.setBounds(200, 61, 82, 24);
		panel_2.add(sHexText);

		JLabel label_5 = new JLabel("T (#5)");
		label_5.setBounds(14, 88, 62, 18);
		panel_2.add(label_5);

		tDecText = new JTextField();
		tDecText.setColumns(10);
		tDecText.setBounds(83, 88, 82, 24);
		panel_2.add(tDecText);

		tHexText = new JTextField();
		tHexText.setColumns(10);
		tHexText.setBounds(200, 88, 82, 24);
		panel_2.add(tHexText);

		JLabel label_6 = new JLabel("F (#6)");
		label_6.setBounds(14, 115, 62, 18);
		panel_2.add(label_6);

		fHexText = new JTextField();
		fHexText.setColumns(10);
		fHexText.setBounds(83, 115, 199, 24);
		panel_2.add(fHexText);

		JPanel panel_3 = new JPanel();
		panel_3.setBounds(344, 62, 301, 79);
		panel_3.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "E (End Record)"));
		contentPane.add(panel_3);
		panel_3.setLayout(null);

		JLabel lblNewLabel_2 = new JLabel("Address of First Instruction");
		lblNewLabel_2.setBounds(14, 23, 187, 18);
		panel_3.add(lblNewLabel_2);

		JLabel label_7 = new JLabel("in Object Program :");
		label_7.setBounds(24, 47, 130, 18);
		panel_3.add(label_7);

		addrFirstInstText = new JTextField();
		addrFirstInstText.setBounds(157, 47, 116, 24);
		panel_3.add(addrFirstInstText);
		addrFirstInstText.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("Start Address in Memory :");
		lblNewLabel_3.setBounds(341, 172, 175, 18);
		contentPane.add(lblNewLabel_3);

		startMemoryText = new JTextField();
		startMemoryText.setBounds(518, 169, 116, 24);
		contentPane.add(startMemoryText);
		startMemoryText.setColumns(10);

		JLabel lblNewLabel_11 = new JLabel("Target Address :");
		lblNewLabel_11.setBounds(341, 199, 124, 18);
		contentPane.add(lblNewLabel_11);

		targetAddrText = new JTextField();
		targetAddrText.setBounds(455, 196, 116, 24);
		contentPane.add(targetAddrText);
		targetAddrText.setColumns(10);

		JLabel lblNewLabel_12 = new JLabel("Instructions :");
		lblNewLabel_12.setBounds(341, 243, 90, 18);
		contentPane.add(lblNewLabel_12);

		JLabel lblNewLabel_13 = new JLabel("사용중인 장치");
		lblNewLabel_13.setBounds(503, 300, 90, 18);
		contentPane.add(lblNewLabel_13);

		deviceText = new JTextField();
		deviceText.setBounds(503, 320, 90, 24);
		contentPane.add(deviceText);
		deviceText.setColumns(10);

		JButton oneButton = new JButton("1 Step");
		oneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (file != null) {
					if (count == 0 && resourceManager.getRegister(8) == 0x00) { // 첫 objectcode
						oneStep();
						update();
						count++;
					} else if (count != 0 && resourceManager.getRegister(8) == 0x00) { // 프로그램이 모두 종료된 후

					} else if (resourceManager.getRegister(8) != 0x00) {
						oneStep();
						update();
						count++;
					}
				}
			}
		});
		oneButton.setBounds(503, 418, 115, 27);
		contentPane.add(oneButton);

		JButton allButton = new JButton(" All Step");
		allButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (all_step) { // all step 버튼이 1번 눌렀을때
					allStep();
					all_step = false; // 그 외는 무시한다.
				}
			}
		});
		allButton.setBounds(503, 457, 115, 27);
		contentPane.add(allButton);

		JButton endButton = new JButton("End");
		endButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		endButton.setBounds(503, 496, 115, 27);
		contentPane.add(endButton);

		JLabel lblNewLabel_15 = new JLabel("Log (About Instruction) :");
		lblNewLabel_15.setBounds(14, 547, 182, 18);
		contentPane.add(lblNewLabel_15);

		list = new JList<String>(model);
		list.setBounds(341, 273, 140, 256);

		jlist = new JScrollPane();
		jlist.setBounds(341, 273, 140, 256);
		contentPane.add(jlist);
		jlist.setViewportView(list);

		list_1 = new JList<String>(model_2);
		list_1.setBounds(14, 577, 604, 127);

		jlist_1 = new JScrollPane();
		jlist_1.setBounds(14, 577, 604, 127);
		contentPane.add(jlist_1);
		jlist_1.setViewportView(list_1);

	}
}
