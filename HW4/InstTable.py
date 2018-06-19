'''
  모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다. <br>
  또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
'''

class InstTable:
    instMap = dict()

    def __init__(self,instFile):
        self.openFile(instFile)


#입력받은 이름의 파일을 열고 해당 내용을 파싱히여 instMap에 저장한다.
    def openFile(self,fileName):
        file = open(fileName,'r')
        while True:
            line = file.readline()
            if not line: break
            inst = Instruction(line)
            self.instMap[inst.instruction] = inst
        file.close()


#inst가 instMap에 있는 명령어인지 검사한다.
#instMap에 있는 경우 true, 없는 경우 false를 리턴한다.
    def search(self,inst):
        temp = inst
        if temp[0]=='+' :
            temp = temp[1:]

        if temp in self.instMap:
            return True
        else :
            return False





#명령어 하나하나의 구체적인 정보는 instruction 클래스에 담긴다.
#instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
class Instruction:
    instruction = ""
    numberOfOperand = 0
    opcode = ""

    format = 0

    def __init__(self,line):
        self.parsing(line)

#일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
    def parsing(self,line):
        word = line.split()
        self.instruction = word[0]
        self.numberOfOperand = int(word[1])
        self.format = int(word[2])
        self.opcode = word[3]
