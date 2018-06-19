'''
  사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
  pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
  이를 링크시킨다.<br>
  section 마다 인스턴스가 하나씩 할당된다.
'''

from LiteralTable import LiteralTable
from Register import Register


class TokenTable:
    MAX_OPERAND = 3;
    nFlag = 32
    iFlag = 16
    xFlag = 8
    bFlag = 4
    pFlag = 2
    eFlag = 1

    symTab = []
    instTab = []
    literTab = []

    tokenList=[] #각 LINE을 의미별로 분할하고 분석하는 공간

    reg = Register() #레지스터 검색을 위해서 선언

    extdef = []
    extref = []

    def __init__(self,instTab):
        self.symTab = []
        self.instTab = instTab
        self.literTab = LiteralTable()
        self.extdef = []
        self.extref = []
        self.tokenList = []

#일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
    def putToken(self,line):
        token = Token(line)
        self.calculNixbpe(token)        #해당 token의 nixbpe를 계산한다.
        self.tokenList.append(token)

#바로 전에 추가한 Token의 location을 변경한다.
    def setLocation(self,location):
        token = self.tokenList[len(self.tokenList)-1]
        token.location = location
        self.tokenList[len(self.tokenList)-1]=token

#symbolTable을 저장한다.
    def setSymTable(self,symtab):
        self.symTab = symtab

#instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
    def makeObjectCode(self,index):
        token = self.tokenList[index]

        if token.operator!="":
            inst = token.operator

            if token.operator[0] == '+':    #명령어 검색을 위해 4형식이면 '+'를 제거한다.
                inst = token.operator[1:]

            if inst in self.instTab.instMap:        # instMap에 있는 명령어인 경우
                opcode = self.instTab.instMap[inst].opcode
                nixbpe = token.nixbpe

                token.objectCode = token.objectCode + opcode[0]
                opcode2 = opcode[1]

                sub_opcode = 0
                if opcode2[0] =='C':    #opcode의 뒷부분이 16진수인 문자인 경우 비트 연산을 위해 int로 바꿔준다.
                    sub_opcode = 12
                else:
                    sub_opcode = int(opcode2)

                sub_opcode<<=4
                nixbpe |= sub_opcode    #기존의 nixbpe 와 opcode 뒷부분을 | 연산한다.

                if self.instTab.instMap[inst].format ==2:   #명령어가 2형식인 경우
                    nixbpe>>=4
                    token.objectCode = token.objectCode + "%X" % int(nixbpe)
                elif self.instTab.instMap[inst].format == 3:    #명령어가 3형식인 경우
                    token.objectCode = token.objectCode + "%02X" % int(nixbpe)  #objectcode에 최종연산한 nixbpe를 저장한다.

                address = 0
                target_address = 0
                if index != len(self.tokenList) -1:
                    pc = self.tokenList[index+1].location   #해당 명령어 다음 명령어의 주소를 가진다.

                operand = token.operand[0]
                reg_num = 0

                if operand!='' and operand[0] == '@':   #symbol 테이블에서의 검색을 위해 @문자는 제거한다.
                    operand = operand[1:]

                if token.operator == "RSUB":        #RSUB인 경우
                    token.objectCode = token.objectCode + "000"
                elif token.operator[0] == '+':      #4형식인
                    # 경우 외부참조이므로 주소는 0이다.
                    token.objectCode = token.objectCode + "00000"
                elif self.symTab.search(operand) != -1 :    #operand가 symbol 테이블에 있는 경우
                    target_address = self.symTab.search(operand)
                    address = target_address - pc   #타겟 address 와 pc의 차를 구하여 address에 저장한다>
                    if address<0:       # 계산 결과 음수인 경우 마스크 연산을 해줘 필요없는 1을 지운다.
                        address &= 0x00000FFF
                    token.objectCode = token.objectCode + "%03X" % address  #계산된 주소를 objectcode에 저장한다.
                elif token.operand[0][0] == '#':    #immediate addressing인 경우
                    token.objectCode = token.objectCode + "00" + token.operand[0][1:]
                elif token.operand[0][0] == '=':    #리터럴테이블에 symbol이 있는 경우
                    target_address = self.literTab.searchLiteral(token.operand[0])
                    address = target_address - pc

                    token.objectCode = token.objectCode + "%03X" % address
                elif self.reg.searchRegister(token.operand[0]) !=-1:    #레지스터 테이블에 operand가 있는 경우
                    reg_num = self.reg.searchRegister(token.operand[0])
                    numOfOperand = self.instTab.instMap[inst].numberOfOperand

                    if numOfOperand == 1:   #명령어의 필요한 operand가 1개인 경우
                        token.objectCode = token.objectCode + "%d"%reg_num + "0"
                    elif numOfOperand == 2: #2개인 경우
                        token.objectCode = token.objectCode + "%d"%reg_num
                        reg_num = self.reg.searchRegister(token.operand[1])
                        token.objectCode = token.objectCode + "%d"%reg_num

            elif token.operator == "BYTE" :
                token.objectCode = token.objectCode + self.literTab.getLiteral(token.operand[0])
            elif token.operator == "WORD" :
                token.objectCode = token.objectCode + "000000"

        return token.objectCode



    def putExtdef(self,symbol):     #extdef arraylist에 추가한다.
        self.extdef.append(symbol)

    def putExtref(self,symbol):     #extref arraylist에 추가한다.
        self.extref.append(symbol)

    def getObjectCode(self,index):  #index번호에 해당하는 object code를 리턴한다.
        return self.tokenList[index].objectCode

    def calculNixbpe(self,token):   #해당 명령어의 nixbpe를 계산한다.
        if token.operator in self.instTab.instMap:
            if self.instTab.instMap[token.operator].format == 2:
                pass
            elif token.operator == "RSUB":  # RSUB인 경우
                token.setFlag(self.nFlag,1)
                token.setFlag(self.iFlag,1)
            elif token.operand[0][0] == '#':    #immediate addressing인 경우
                token.setFlag(self.iFlag,1)
            elif token.operand[0][0] == '@':    #indirect addressing인 경우
                token.setFlag(self.nFlag,1)
                token.setFlag(self.pFlag,1)

            else:                               #direct addressing 인 경우
                token.setFlag(self.nFlag,1)
                token.setFlag(self.iFlag,1)
                token.setFlag(self.pFlag,1)

        elif token.operator != "" and token.operator[0] != '.' and token.operator[0] == '+':
            if len(token.operand) == 2 and token.operand[1][0] == 'X':      #x bit를 사용하는 경우
                token.setFlag(self.nFlag,1)
                token.setFlag(self.iFlag,1)
                token.setFlag(self.xFlag,1)
                token.setFlag(self.eFlag,1)
            else:                                                           #4형식인 경우
                token.setFlag(self.nFlag,1)
                token.setFlag(self.iFlag,1)
                token.setFlag(self.eFlag,1)




#각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다.
#의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
class Token:
    location = 0
    label = ""
    operator = ""
    operand = []
    comment = ""
    nixbpe = 0

    objectCode = ""
    byteSize = 0

#클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
    def __init__(self,line):
        self.parsing(line)

# line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
    def parsing(self,line):
        line = line[:-1]
        word = line.split('\t')

        if len(word)==1:
            self.label = word[0]
        elif len(word) ==2:
            self.label = word[0]
            self.operator = word[1]
        elif len(word) == 3:
            self.label = word[0]
            self.operator = word[1]
            self.operand = word[2].split(',')
        elif len(word) == 4:
            self.label = word[0]
            self.operator = word[1]
            self.operand = word[2].split(',')
            self.comment = word[3]

#n,i,x,b,p,e flag를 설정한다.
    def setFlag(self,flag,value):

        if value == 1:
            self.nixbpe |= flag
        elif value ==0:
            if flag == 1:
                self.nixbpe &= 0xfe
            elif flag == 2:
                self.nixbpe &= 0xfd
            elif flag == 4:
                self.nixbpe &= 0xfb
            elif flag == 8:
                self.nixbpe &= 0xf7
            elif flag == 16:
                self.nixbpe &= 0xef
            elif flag == 32:
                self.nixbpe &= 0xdf

#원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다
    def getFlag(self,flags):
        return self.nixbpe & flags

